package io.github.pengxianggui.crud.dynamic;

import io.github.pengxianggui.crud.BaseController;
import io.github.pengxianggui.crud.BaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    public DynamicCrudGenerator(RequestMappingHandlerMapping requestMappingHandlerMapping, Validator validator) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.validator = validator;
        this.init();
    }

    public void init() {
        Map<String, Object> beansWithAnnotation = requestMappingHandlerMapping.getApplicationContext().getBeansWithAnnotation(Crud.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            Crud annotation = entry.getValue().getClass().getAnnotation(Crud.class);
            List<CrudMethod> crudMethods = new ArrayList<>();
            if (annotation.value().length > 0) {
                crudMethods.addAll(Arrays.asList(annotation.value().clone()));
            } else {
                crudMethods.addAll(Arrays.asList(CrudMethod.values().clone()));
            }
            if (annotation.exclude().length > 0) {
                crudMethods.removeAll(Arrays.asList(annotation.exclude().clone()));
            }
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                field.setAccessible(true);
                CrudService crudServiceAnnotation = field.getAnnotation(CrudService.class);
                if (crudServiceAnnotation != null) {
                    Object service = ReflectionUtils.getField(field, entry.getValue());
                    if (service instanceof BaseService) {
                        register(entry.getValue().getClass(), (BaseService) service, crudMethods.toArray(new CrudMethod[]{}));
                    } else {
                        throw new RuntimeException("无效的BaseService");
                    }
                    break;
                }
            }
        }
    }

    public <T> void register(Class modClass, BaseService<T> baseService, CrudMethod... crudMethods) {
        RequestMapping requestMappingAnnotation = (RequestMapping) modClass.getAnnotation(RequestMapping.class);
        if (requestMappingAnnotation == null) {
            throw new RuntimeException("无效的模块类");
        }
        Api apiAnnotation = (Api) modClass.getAnnotation(Api.class);
        register(requestMappingAnnotation.value(), apiAnnotation.tags(), baseService, crudMethods);
    }

    private volatile int num;

    @SneakyThrows
    public <T> void register(String[] basePaths, String[] tags, BaseService<T> baseService, CrudMethod... crudMethods) {
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
        CtConstructor ctConstructor = new CtConstructor(new CtClass[]{classPool.get(BaseService.class.getName()), classPool.get("javax.validation.Validator")}, ctClass);
        ctConstructor.setBody("{super($1,$2);}");
        ctClass.addConstructor(ctConstructor);

        ClassFile classFile = ctClass.getClassFile();
        ConstPool constPool = classFile.getConstPool();

        //添加注解
        Annotation apiAnnotation = new Annotation(Api.class.getCanonicalName(), constPool);
        this.addAnnotationArrayMemberValue(apiAnnotation, "tags", tags, constPool);
        Annotation requestMappingAnnotation = new Annotation(RequestMapping.class.getCanonicalName(), constPool);
        this.addAnnotationArrayMemberValue(requestMappingAnnotation, "value", basePaths, constPool);
        Annotation restControllerAnnotation = new Annotation(RestController.class.getCanonicalName(), constPool);

        AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        annotationsAttribute.addAnnotation(apiAnnotation);
        annotationsAttribute.addAnnotation(requestMappingAnnotation);
        annotationsAttribute.addAnnotation(restControllerAnnotation);

        classFile.addAttribute(annotationsAttribute);
        ctClass = classPool.makeClass(classFile);
        Class ctlClass = ctClass.toClass();

        Constructor constructor = ctlClass.getConstructor(BaseService.class, Validator.class);

        BaseController<T> ctl = (BaseController<T>) constructor.newInstance(baseService, this.validator);

        Method[] methods = ctl.getClass().getMethods();

        Method getMappingForMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class, Class.class);
        getMappingForMethod.setAccessible(true);

        for (Method m : methods) {
            boolean doRegister = false;
            for (CrudMethod crudMethod : crudMethods) {
                if (crudMethod.getName().equals(m.getName())) {
                    doRegister = true;
                    break;
                }
            }

            if (doRegister) {
                // TODO 1.0 不根据ApiOperation来识别是否是接口方法，而是通过GetMapper、PostMapper、DeleteMapper、PutMapper、RequestMapper等来识别
                ApiOperation annotation = m.getAnnotation(ApiOperation.class);
                if (annotation != null) {
                    RequestMappingInfo mapping_info = (RequestMappingInfo) getMappingForMethod.invoke(requestMappingHandlerMapping, m, ctlClass);
                    if (!isMappingRegistered(mapping_info)) {
                        requestMappingHandlerMapping.registerMapping(mapping_info, ctl, m);
                        log.debug("{} 已经完成动态注册!", mapping_info.getPatternsCondition());
                    } else {
                        log.debug("{} 已经注册，不再自动注册", mapping_info);
                    }
                }
            }
        }
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
