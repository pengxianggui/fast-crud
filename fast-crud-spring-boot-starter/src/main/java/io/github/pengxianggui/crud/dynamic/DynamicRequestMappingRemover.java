package io.github.pengxianggui.crud.dynamic;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pengxg
 * @date 2025/2/7 21:54
 */
@Slf4j
public class DynamicRequestMappingRemover implements ApplicationListener<ContextRefreshedEvent> {
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private List<Docket> docketList = null;

    public DynamicRequestMappingRemover(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 遍历所有注册的 Controller 方法
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();

        Map<String, Docket> docketMap = SpringUtil.getBeansOfType(Docket.class);
        docketList = docketMap.values().stream().collect(Collectors.toList());

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

                removeFromSwagger(mappingInfo);
            }
        }
    }

    /**
     * 从 Swagger 中移除接口
     */
    private void removeFromSwagger(RequestMappingInfo mappingInfo) {
        // FIXME 无效
        if (!CollectionUtils.isEmpty(docketList)) {
            log.debug("Removing API from Springfox Swagger: {}", mappingInfo.getName());
            docketList.forEach(docket -> {
                docket.select()
                        .apis(requestHandler -> {
                            if (requestHandler != null && requestHandler.getHandlerMethod() != null) {
                                return !mappingInfo.getName().equals(requestHandler.getHandlerMethod().getMethod().getName());
                            }
                            return true;
                        });
            });
        }
    }

}
