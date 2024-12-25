package io.github.pengxianggui.crud.dynamic;

import io.github.pengxianggui.crud.BaseController;
import io.github.pengxianggui.crud.BaseService;
import io.swagger.annotations.Api;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.LoaderClassPath;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.validation.Validator;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class DynamicCrudGenerator {

    public RequestMappingHandlerMapping requestMappingHandlerMapping;

    private Validator validator;

    private volatile int num;

    public DynamicCrudGenerator(RequestMappingHandlerMapping requestMappingHandlerMapping, Validator validator) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.validator = validator;
        this.init();
    }

    public void init() {
        Map<String, Object> beansWithAnnotation = requestMappingHandlerMapping.getApplicationContext().getBeansWithAnnotation(Crud.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            Object ctrlBean = entry.getValue();
            RequestMapping requestMappingAnnotation = ctrlBean.getClass().getAnnotation(RequestMapping.class);
            if (requestMappingAnnotation == null) {
                throw new RuntimeException("无效的模块类:" + ctrlBean.getClass().getName() + ",必须声明@RequestMapping注解");
            }
            Crud annotation = ctrlBean.getClass().getAnnotation(Crud.class);
            List<CrudMethod> crudMethods = new ArrayList<>();
            if (annotation.value().length > 0) {
                crudMethods.addAll(Arrays.asList(annotation.value().clone()));
            } else {
                crudMethods.addAll(Arrays.asList(CrudMethod.values().clone()));
            }
            if (annotation.exclude().length > 0) {
                crudMethods.removeAll(Arrays.asList(annotation.exclude().clone()));
            }
            for (Field field : ctrlBean.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                CrudService crudServiceAnnotation = field.getAnnotation(CrudService.class);
                if (crudServiceAnnotation == null) {
                    continue;
                }
                Object service = ReflectionUtils.getField(field, entry.getValue());
                if (!(service instanceof BaseService)) {
                    throw new RuntimeException("无效的Service类:" + service.getClass().getName() + ", @CrudService修饰的service bean必须implement BaseService");
                }
                register(ctrlBean.getClass(), (BaseService) service, crudMethods.toArray(new CrudMethod[]{}));
            }
        }
    }

    @SneakyThrows
    public <T> void register(Class ctrlClass, BaseService<T> baseService, CrudMethod... crudMethods) {
        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));

        ClassFile classfile = new ClassFile(false, BaseController.class.getName() + "$" + num++, BaseController.class.getName());
        CtClass ctClass = classPool.makeClass(classfile, false);
        ctClass.setModifiers(Modifier.PUBLIC);

        String typeName = ((ParameterizedType) baseService.getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
        SignatureAttribute.ClassSignature cs = new SignatureAttribute.ClassSignature(null, new SignatureAttribute.ClassType(BaseController.class.getName(),
                new SignatureAttribute.TypeArgument[]{new SignatureAttribute.TypeArgument(new SignatureAttribute.ClassType(typeName))}),
                null);

        ctClass.setGenericSignature(cs.encode());
        CtConstructor ctConstructor = new CtConstructor(new CtClass[]{
                classPool.get(BaseService.class.getName()),
                classPool.get("javax.validation.Validator"),
                classPool.get("java.lang.String")
        }, ctClass);
        ctConstructor.setBody("{super($1,$2,$3);}");
        ctClass.addConstructor(ctConstructor);

        ClassFile classFile = ctClass.getClassFile();
        ConstPool constPool = classFile.getConstPool();

        //添加注解
        AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation requestMappingAnnotation = new Annotation(RequestMapping.class.getCanonicalName(), constPool);
        String[] basePaths = ((RequestMapping) ctrlClass.getAnnotation(RequestMapping.class)).value();
        this.addAnnotationArrayMemberValue(requestMappingAnnotation, "value", basePaths, constPool);
        annotationsAttribute.addAnnotation(requestMappingAnnotation);

        Annotation restControllerAnnotation = new Annotation(RestController.class.getCanonicalName(), constPool);
        annotationsAttribute.addAnnotation(restControllerAnnotation);

        if (ClassUtils.isPresent("io.swagger.annotations.Api", Thread.currentThread().getContextClassLoader())) {
            Annotation apiAnnotation = new Annotation(Api.class.getCanonicalName(), constPool);
            String[] tags = new String[]{ctrlClass.getSimpleName()};
            Api ctrlApiAnnotation = (Api) ctrlClass.getAnnotation(Api.class);
            if (ctrlApiAnnotation != null) {
                tags = ctrlApiAnnotation.tags();
            }
            this.addAnnotationArrayMemberValue(apiAnnotation, "tags", tags, constPool);
            annotationsAttribute.addAnnotation(apiAnnotation);
        }

        classFile.addAttribute(annotationsAttribute);
        ctClass = classPool.makeClass(classFile);
        Class ctlClass = ctClass.toClass();

        Constructor constructor = ctlClass.getConstructor(BaseService.class, Validator.class, String.class);

        String basePath = basePaths.length > 0 ? basePaths[0] : "";
        BaseController<T> ctl = (BaseController<T>) constructor.newInstance(baseService, this.validator, basePath);

        Method[] methods = ctl.getClass().getMethods();

        Method getMappingForMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class, Class.class);
        getMappingForMethod.setAccessible(true);

        for (Method m : methods) {
            if (!isApiMethod(m)) {
                continue;
            }
            for (CrudMethod crudMethod : crudMethods) {
                if (crudMethod.getName().equals(m.getName())) {
                    RequestMappingInfo mapping_info = (RequestMappingInfo) getMappingForMethod.invoke(requestMappingHandlerMapping, m, ctlClass);
                    if (!isMappingRegistered(mapping_info)) {
                        requestMappingHandlerMapping.registerMapping(mapping_info, ctl, m);
                        log.debug("{} 已经完成动态注册!", mapping_info.getPatternsCondition());
                    } else {
                        log.debug("{} 已经注册，不再自动注册", mapping_info);
                    }
                    break;
                }
            }
        }
    }

    /**
     * 判断method是否是一个api method。存在注解: @GetMapping、@PutMapper、@PostMapper、@DeleteMapping、@RequestMapping等
     *
     * @param method
     * @return
     */
    private boolean isApiMethod(Method method) {
        List<Class> apiAnnotationClasses = Arrays.asList(
                GetMapping.class, PutMapping.class, PostMapping.class,
                DeleteMapping.class, PatchMapping.class, RequestMapping.class);
        return Arrays.stream(method.getAnnotations())
                .anyMatch(annotation -> apiAnnotationClasses.contains(annotation.annotationType()));
    }

    private boolean isMappingRegistered(RequestMappingInfo mappingInfo) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (RequestMappingInfo existingMappingInfo : handlerMethods.keySet()) {
            if (existingMappingInfo.equals(mappingInfo)) {
                return true;
            }
        }
        return false;
    }

    private Annotation addAnnotationArrayMemberValue(Annotation annotation, String field, String[] values, ConstPool constPool) {
        MemberValue[] memberValues = new MemberValue[values.length];
        for (int i = 0; i < values.length; i++) {
            memberValues[i] = new StringMemberValue(values[i], constPool);
        }
        ArrayMemberValue memberValue = new ArrayMemberValue(constPool);
        memberValue.setValue(memberValues);
        annotation.addMemberValue(field, memberValue);
        return annotation;
    }

}
