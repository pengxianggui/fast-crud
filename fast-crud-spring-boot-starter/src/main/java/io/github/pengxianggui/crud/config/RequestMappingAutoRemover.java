package io.github.pengxianggui.crud.config;

import io.github.pengxianggui.crud.CrudExclude;
import io.github.pengxianggui.crud.CrudMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.Map;

/**
 * 在接口中兑现{@link CrudExclude}, 经此注解修饰的接口将不会被注册到spring mvc接口中，或者准确来说是会在注册之后移除掉。
 *
 * @author pengxg
 * @date 2025/2/7 21:54
 */
@Slf4j
public class RequestMappingAutoRemover implements ApplicationListener<ContextRefreshedEvent> {
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public RequestMappingAutoRemover(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 遍历所有注册的 Controller 方法
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();

        // 记录要删除的映射
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            Class<?> beanType = handlerMethod.getBeanType();

            if (!beanType.isAnnotationPresent(CrudExclude.class)) {
                continue;
            }

            CrudExclude crudExclude = beanType.getAnnotation(CrudExclude.class);
            CrudMethod[] excludeMethods = crudExclude.value();
            String methodName = handlerMethod.getMethod().getName();
            if (Arrays.stream(excludeMethods).anyMatch(crudMethod -> methodName.equals(crudMethod.getName()))) {
                RequestMappingInfo mappingInfo = entry.getKey();
                requestMappingHandlerMapping.unregisterMapping(mappingInfo);
                log.debug("Fast crud api: {} has been removed!", mappingInfo.getName());
            }
        }
    }
}
