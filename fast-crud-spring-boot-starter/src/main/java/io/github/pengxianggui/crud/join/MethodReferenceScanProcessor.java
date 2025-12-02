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

        for (Element element : elements) {
            if (!(element instanceof TypeElement)) {
                continue;
            }

            Map<String, String> registry = new HashMap<>();
            TypeElement dtoType = (TypeElement) element;
            final String dtoClassName = processingEnv.getElementUtils().getBinaryName(dtoType).toString();
            // 加入dto本身的字段
            collectFields(dtoType, dtoClassName, registry);
            // 加入主entity字段
            for (AnnotationMirror am : dtoType.getAnnotationMirrors()) {
                if (JoinMain.class.getName().equals(am.getAnnotationType().toString())) {
                    TypeMirror mainType = getClassValueFromAnnotation(am, "value");
                    collectFields(mainType, registry);
                }
            }

            // 加入关联类字段
            // 3. 处理 InnerJoin、LeftJoin、RightJoin
            processRepeatableJoin(dtoType, InnerJoin.class.getName(), registry);
            processRepeatableJoin(dtoType, LeftJoin.class.getName(), registry);
            processRepeatableJoin(dtoType, RightJoin.class.getName(), registry);

            generateRegistryClass(dtoType, registry);
        }
        return true;
    }

    private void generateRegistryClass(TypeElement dtoType, Map<String, String> registry) {
        String dtoName = processingEnv.getElementUtils().getBinaryName(dtoType).toString();
        // 保证类名唯一
        String className = dtoName.replace(".", "_") + "_MethodReferenceLoader";
        String currentPackage = this.getClass().getPackage().getName();
        String generateClassOfPackage = currentPackage + ".generate";
        try {
            JavaFileObject fileObject = filer.createSourceFile(generateClassOfPackage + "." + className);
            try (PrintWriter out = new PrintWriter(fileObject.openWriter())) {
                out.println("package " + generateClassOfPackage + ";");
                out.println("import com.google.auto.service.AutoService;");
                out.println("import com.baomidou.mybatisplus.core.toolkit.support.SFunction;");
                out.println("import " + currentPackage + ".MethodReferenceLoader;");
                out.println("import " + currentPackage + ".MethodReferenceRegistry;");
                out.println("@AutoService(MethodReferenceLoader.class)");
                out.println("public class " + className + " implements MethodReferenceLoader {");
                out.println("    @Override");
                out.println("    public void load() {");
                for (Map.Entry<String, String> entry : registry.entrySet()) {
                    out.printf("        MethodReferenceRegistry.register(\"%s\", (SFunction<%s, ?>) %s);\n",
                            entry.getKey(), entry.getKey().split("#")[0], entry.getValue());
                }
                out.println("    }");
                out.println("}");
            }
        } catch (FilerException e) {
            // 文件已经生成, 忽略..
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Unable to generate loader file for " + dtoName + ": " + e.getMessage());
        }
    }

    private void processRepeatableJoin(TypeElement dtoType, String annotationClassName, Map<String, String> registry) {
        for (AnnotationMirror mirror : dtoType.getAnnotationMirrors()) {
            String mirrorType = mirror.getAnnotationType().toString();
            if (!annotationClassName.equals(mirrorType)) {
                // 新增：同时支持容器注解（例如 InnerJoins 包含多个 InnerJoin）
                // 如果 annotationClassName = InnerJoin.class.getName()，则容器类名是 InnerJoins
                if (!mirrorType.equals(annotationClassName + "s")) {
                    continue;
                }
            }

            // 如果是容器注解（InnerJoins），则取出其 value 数组中的每一个 InnerJoin
            if (mirrorType.endsWith("s")) { // 例如 InnerJoins、LeftJoins、RightJoins
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                    if ("value".equals(entry.getKey().getSimpleName().toString())) {
                        Object value = entry.getValue().getValue();
                        if (value instanceof Iterable) {
                            for (Object o : (Iterable<?>) value) {
                                if (o instanceof AnnotationMirror) {
                                    processSingleJoinAnnotation((AnnotationMirror) o, registry);
                                }
                            }
                        }
                    }
                }
            } else {
                // 单个 InnerJoin、LeftJoin、RightJoin
                processSingleJoinAnnotation(mirror, registry);
            }
        }
    }

    /**
     * 处理单个 InnerJoin/LeftJoin/RightJoin 注解，提取其 value 属性
     */
    private void processSingleJoinAnnotation(AnnotationMirror mirror, Map<String, String> registry) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
            if ("value".equals(entry.getKey().getSimpleName().toString())) {
                Object value = entry.getValue().getValue();
                if (value instanceof TypeMirror) {
                    collectFields((TypeMirror) value, registry);
                }
            }
        }
    }

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

    private void collectFields(TypeMirror mirror, Map<String, String> registry) {
        if (mirror == null) return;
        Element element = processingEnv.getTypeUtils().asElement(mirror);
        if (!(element instanceof TypeElement)) return;
        String fullName = processingEnv.getElementUtils().getBinaryName((TypeElement) element).toString();
        collectFields((TypeElement) element, fullName, registry);
    }

    private void collectFields(TypeElement typeEle, String className, Map<String, String> registry) {
        // 处理当前类字段
        for (Element enclosed : typeEle.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD
                    && !enclosed.getModifiers().contains(Modifier.STATIC)) { // 排除静态属性
                String fieldName = enclosed.getSimpleName().toString();
                String getter = getGetterMethodName(enclosed);
                registry.put(MethodReferenceRegistry.getKey(className, fieldName), className + "::" + getter);
            }
        }

        // 递归处理父类字段
        TypeMirror superMirror = typeEle.getSuperclass();
        if (superMirror != null && !superMirror.toString().equals("java.lang.Object")) {
            Element superElement = processingEnv.getTypeUtils().asElement(superMirror);
            if (superElement instanceof TypeElement) {
                collectFields((TypeElement) superElement, className, registry);
            }
        }
    }

    private String getGetterMethodName(Element field) {
//        String fieldName = field.getSimpleName().toString();
//        boolean isbool = field.asType().toString().equals("boolean"); // boolean 为is开头, 其它(包括Boolean)为get开头
//        return (isbool ? "is" : "get") + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        
        String fieldName = field.getSimpleName().toString();
        // 注意：这里要处理 TypeKind.BOOLEAN (基本类型 boolean)
        // 包装类型 Boolean 依然遵循 getXxx 规范
        boolean isPrimitiveBoolean = field.asType().getKind() == TypeKind.BOOLEAN;

        if (isPrimitiveBoolean) {
            // 如果已经是 is 开头，比如 isDeleted，并且第三个字符是大写（如果有），则直接返回原名
            // 规范：boolean isSuccess -> isSuccess()
            if (fieldName.startsWith("is") && fieldName.length() > 2 && Character.isUpperCase(fieldName.charAt(2))) {
                return fieldName;
            }
            // 否则：boolean success -> isSuccess()
            return "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }

        // 其他类型 (包括 Boolean)：name -> getName()
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}
