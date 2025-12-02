package io.github.pengxianggui.crud.join;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * APT: 根据@JoinMan注解生成方法引用注册的类文件(MethodReferenceLoader子类)。
 * <p>
 * 改动说明：
 * 采用原子化生成策略。
 * 1. 扫描阶段：收集所有 DTO、主 Entity、关联 Entity 的 TypeElement，并去重。
 * 2. 生成阶段：遍历去重后的 TypeElement，为每一个类单独生成一个 Loader。
 *
 * @author pengxg
 * @date 2025/5/24 11:44
 * @see MethodReferenceLoader
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MethodReferenceScanProcessor extends AbstractProcessor {
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Sets.newHashSet(JoinMain.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(JoinMain.class);

        // 【核心改动 1】准备一个 Map 用于收集所有涉及的类 (Key=全限定名, Value=TypeElement)
        // 作用：去重。无论多少个 DTO 引用了同一个 Entity，这里只保留一份。
        Map<String, TypeElement> targetsToGenerate = new HashMap<>();

        for (Element element : elements) {
            if (!(element instanceof TypeElement)) {
                continue;
            }
            TypeElement dtoType = (TypeElement) element;

            // 1. 收集 DTO 自己
            addTypeToTargets(dtoType, targetsToGenerate);

            // 2. 收集 @JoinMain 的主表 Entity
            for (AnnotationMirror am : dtoType.getAnnotationMirrors()) {
                if (JoinMain.class.getName().equals(am.getAnnotationType().toString())) {
                    TypeMirror mainType = getClassValueFromAnnotation(am, "value");
                    addTypeToTargets(mainType, targetsToGenerate);
                }
            }

            // 3. 收集 @InnerJoin, @LeftJoin, @RightJoin 的关联 Entity
            // 这里我们不再直接处理 registry，而是只提取 TypeElement
            collectJoinTypes(dtoType, InnerJoin.class.getName(), targetsToGenerate);
            collectJoinTypes(dtoType, LeftJoin.class.getName(), targetsToGenerate);
            collectJoinTypes(dtoType, RightJoin.class.getName(), targetsToGenerate);
        }

        // 【核心改动 2】遍历去重后的类集合，生成 Loader
        // 这里的 targetsToGenerate 包含了所有 DTO 和 Entity
        for (TypeElement targetType : targetsToGenerate.values()) {
            generateLoaderForClass(targetType);
        }

        return true;
    }

    // 【新增辅助方法】安全添加 Type 到 Map
    private void addTypeToTargets(TypeMirror typeMirror, Map<String, TypeElement> map) {
        if (typeMirror == null) return;
        Element element = processingEnv.getTypeUtils().asElement(typeMirror);
        if (element instanceof TypeElement) {
            addTypeToTargets((TypeElement) element, map);
        }
    }

    private void addTypeToTargets(TypeElement typeElement, Map<String, TypeElement> map) {
        if (typeElement == null) return;
        String binaryName = processingEnv.getElementUtils().getBinaryName(typeElement).toString();
        map.putIfAbsent(binaryName, typeElement);
    }

    // 【核心改动 3】重构生成逻辑：只为当前传入的 type 生成 Loader
    private void generateLoaderForClass(TypeElement type) {
        String originalBinaryName = processingEnv.getElementUtils().getBinaryName(type).toString();

        // 这里的 className 已经是 全限定名转换过来的 (com_example_User)，不会冲突
        String generatedClassName = originalBinaryName.replace(".", "_") + "_MethodReferenceLoader";

        String currentPackage = this.getClass().getPackage().getName();
        String generatePackageName = currentPackage + ".generate"; // 建议用 generated

        try {
            // 尝试创建文件
            JavaFileObject fileObject = filer.createSourceFile(generatePackageName + "." + generatedClassName, type);

            try (PrintWriter out = new PrintWriter(fileObject.openWriter())) {
                // 【延迟执行】只有确定要写文件了，才去扫描字段，节省性能
                Map<String, String> registry = new HashMap<>();
                collectFields(type, originalBinaryName, registry);

                out.println("package " + generatePackageName + ";");
                out.println("import com.google.auto.service.AutoService;");
                out.println("import com.baomidou.mybatisplus.core.toolkit.support.SFunction;");
                out.println("import " + currentPackage + ".MethodReferenceLoader;");
                out.println("import " + currentPackage + ".MethodReferenceRegistry;");
                out.println();
                out.println("@AutoService(MethodReferenceLoader.class)");
                out.println("public class " + generatedClassName + " implements MethodReferenceLoader {");
                out.println("    @Override");
                out.println("    public void load() {");

                for (Map.Entry<String, String> entry : registry.entrySet()) {
                    // entry.getKey() = com.example.User#name
                    // entry.getValue() = com.example.User::getName
                    // 这里的 split("#")[0] 是为了获取 Class 类型
                    out.printf("        MethodReferenceRegistry.register(\"%s\", (SFunction<%s, ?>) %s);\n",
                            entry.getKey(), entry.getKey().split("#")[0], entry.getValue());
                }

                out.println("    }");
                out.println("}");
            }
        } catch (FilerException e) {
            // 【重要】文件已存在，说明别的 DTO 已经触发生成了这个类的 Loader
            // 这是原子化生成的正常现象，直接忽略
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Unable to generate loader file for " + originalBinaryName + ": " + e.getMessage());
        }
    }

    // 【核心改动 4】重构提取逻辑：只提取 Type，不生成 Registry
    private void collectJoinTypes(TypeElement dtoType, String annotationClassName, Map<String, TypeElement> targets) {
        for (AnnotationMirror mirror : dtoType.getAnnotationMirrors()) {
            String mirrorType = mirror.getAnnotationType().toString();

            // 逻辑保持不变，支持单个和容器注解
            if (!annotationClassName.equals(mirrorType) && !mirrorType.equals(annotationClassName + "s")) {
                continue;
            }

            if (mirrorType.endsWith("s")) { // 容器注解
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                    if ("value".equals(entry.getKey().getSimpleName().toString())) {
                        Object value = entry.getValue().getValue();
                        if (value instanceof Iterable) {
                            for (Object o : (Iterable<?>) value) {
                                if (o instanceof AnnotationMirror) {
                                    extractTypeFromSingleAnnotation((AnnotationMirror) o, targets);
                                }
                            }
                        }
                    }
                }
            } else { // 单个注解
                extractTypeFromSingleAnnotation(mirror, targets);
            }
        }
    }

    // 从单个 Join 注解中提取 value 属性的 Type
    private void extractTypeFromSingleAnnotation(AnnotationMirror mirror, Map<String, TypeElement> targets) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
            if ("value".equals(entry.getKey().getSimpleName().toString())) {
                Object value = entry.getValue().getValue();
                if (value instanceof TypeMirror) {
                    addTypeToTargets((TypeMirror) value, targets);
                }
            }
        }
    }

    // --- 下面的辅助方法基本保持不变，只是调用时机变了 ---

    private TypeMirror getClassValueFromAnnotation(AnnotationMirror annotationMirror, String attributeName) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(attributeName)) {
                Object value = entry.getValue().getValue();
                if (value instanceof TypeMirror) {
                    return (TypeMirror) value;
                }
            }
        }
        return null;
    }

    /**
     * 收集字段映射
     *
     * @param typeEle       当前正在扫描的类（可能是子类，也可能是递归到的父类）
     * @param leafClassName 最底层的子类全限定名（例如 Orders），用于生成 Key 和 Lambda。即使递归到父类，这个参数也不变。
     * @param registry      注册表
     */
    private void collectFields(TypeElement typeEle, String leafClassName, Map<String, String> registry) {
        // 1. 处理当前正在扫描的类（typeEle）的字段
        for (Element enclosed : typeEle.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD
                    && !enclosed.getModifiers().contains(Modifier.STATIC)) { // 排除静态属性

                String fieldName = enclosed.getSimpleName().toString();
                String getter = getGetterMethodName(enclosed);

                // 核心修正：
                // 无论 typeEle 是 Orders 还是 BaseEntity，我们都使用传入的 leafClassName (即 Orders)
                // 这样生成的代码就是: Orders::getCreateTime，而不是 BaseEntity::getCreateTime
                // Java 语法允许 子类::父类方法
                String key = MethodReferenceRegistry.getKey(leafClassName, fieldName);
                String value = leafClassName + "::" + getter;

                // 使用 putIfAbsent 还是 put？
                // 通常建议使用 putIfAbsent。
                // 因为递归是从子类 -> 父类。如果子类重写了父类字段/方法，应该保留子类的（也就是先访问到的）。
                registry.putIfAbsent(key, value);
            }
        }

        // 2. 递归处理父类字段
        TypeMirror superMirror = typeEle.getSuperclass();
        if (superMirror != null && !superMirror.toString().equals("java.lang.Object")) {
            Element superElement = processingEnv.getTypeUtils().asElement(superMirror);
            if (superElement instanceof TypeElement) {
                // 核心修正：
                // 递归调用时，第二个参数继续传 leafClassName，不要传 superElement 的名字！
                collectFields((TypeElement) superElement, leafClassName, registry);
            }
        }
    }

//    // 这里只保留了针对 TypeElement 的 collectFields，因为现在我们直接对 TypeElement 操作
//    private void collectFields(TypeElement typeEle, String className, Map<String, String> registry) {
//        // 处理当前类字段
//        for (Element enclosed : typeEle.getEnclosedElements()) {
//            if (enclosed.getKind() == ElementKind.FIELD
//                    && !enclosed.getModifiers().contains(Modifier.STATIC)) { // 排除静态属性
//                String fieldName = enclosed.getSimpleName().toString();
//                String getter = getGetterMethodName(enclosed);
//                registry.put(MethodReferenceRegistry.getKey(className, fieldName), className + "::" + getter);
//            }
//        }
//
//        // 递归处理父类字段
//        TypeMirror superMirror = typeEle.getSuperclass();
//        if (superMirror != null && !superMirror.toString().equals("java.lang.Object")) {
//            Element superElement = processingEnv.getTypeUtils().asElement(superMirror);
//            if (superElement instanceof TypeElement) {
//                // 递归调用
//                String superClassName = processingEnv.getElementUtils().getBinaryName((TypeElement) superElement).toString();
//                collectFields((TypeElement) superElement, superClassName, registry);
//            }
//        }
//    }

    private String getGetterMethodName(Element field) {
        String fieldName = field.getSimpleName().toString();
        // 修复：Getter 生成逻辑保持你最新的修改
        boolean isPrimitiveBoolean = field.asType().getKind() == TypeKind.BOOLEAN;

        if (isPrimitiveBoolean) {
            if (fieldName.startsWith("is") && fieldName.length() > 2 && Character.isUpperCase(fieldName.charAt(2))) {
                return fieldName;
            }
            return "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}