package io.github.pengxianggui.crud.join;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 方法引用自动扫描处理器
 *
 * @author pengxg
 * @date 2025/5/24 11:44
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MethodReferenceScanProcessor extends AbstractProcessor {
    private Filer filer;
    private boolean generated = false;

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
        if (generated) { // 只处理一次
            return false;
        }
        Map<String, String> registry = new HashMap<>();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(JoinMain.class);
        for (Element element : elements) {
            if (!(element instanceof TypeElement)) {
                continue;
            }

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
        }

        try {
            // 生成自动注册类
            String autoConfigurationClassName = generateRegistryClass(registry);
            // 将自动注册类注册到spring.factories
            writeSpringFactories(autoConfigurationClassName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.generated = true;
        return true;
    }

    private String generateRegistryClass(Map<String, String> registry) throws IOException {
        try {
            String packagePath = this.getClass().getPackage().getName();
            String autoConfigurationClassName = packagePath + ".MethodReferenceRegistrarAutoConfiguration";
            JavaFileObject fileObject = filer.createSourceFile(autoConfigurationClassName);
            try (PrintWriter out = new PrintWriter(fileObject.openWriter())) {
                out.println("package " + packagePath + ";");
                out.println("import " + packagePath + ".MethodReferenceRegistry;");
                out.println("import org.springframework.context.annotation.Configuration;");
                out.println("import javax.annotation.PostConstruct;");
                out.println("import com.baomidou.mybatisplus.core.toolkit.support.SFunction;");
                out.println("@Configuration");
                out.println("public class MethodReferenceRegistrarAutoConfiguration {");
                out.println("    @PostConstruct");
                out.println("    public void init() {");

                for (Map.Entry<String, String> entry : registry.entrySet()) {
                    String className = entry.getKey().split("#")[0];
                    out.printf("        MethodReferenceRegistry.register(\"%s\", (SFunction<%s, ?>) %s);\n", entry.getKey(), className, entry.getValue());
                }

                out.println("    }");
                out.println("}");
            }
            return autoConfigurationClassName;
        } catch (IOException e) {
            throw e;
        }
    }

    private void writeSpringFactories(String autoConfigurationClassName) throws IOException {
        try {
            Filer filer = processingEnv.getFiler();
            FileObject resource = filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "META-INF/spring.factories"
            );

            try (Writer writer = new BufferedWriter(resource.openWriter())) {
                writer.write("org.springframework.boot.autoconfigure.EnableAutoConfiguration=\\\n");
                writer.write(autoConfigurationClassName + "\n");
            }
        } catch (IOException e) {
            throw e;
        }
    }


    private void processRepeatableJoin(TypeElement dtoType, String annotationClassName, Map<String, String> registry) {
        for (AnnotationMirror mirror : dtoType.getAnnotationMirrors()) {
            if (!annotationClassName.equals(mirror.getAnnotationType().toString())) {
                continue;
            }
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
        String fieldName = field.getSimpleName().toString();
        boolean isbool = field.asType().toString().equals("boolean"); // boolean 为is开头, 其它(包括Boolean)为get开头
        return (isbool ? "is" : "get") + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}
