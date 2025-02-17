package io.github.pengxianggui.crud.apidoc;

import io.github.pengxianggui.crud.CrudExclude;
import io.github.pengxianggui.crud.CrudMethod;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.RequestHandler;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * CRUD接口文档自动检测是否从接口文档中移除。参考 {@link CrudExclude}
 *
 * @author pengxg
 * @date 2025-02-17 11:18
 */
public class CrudApiDocAutoRemovePredicate implements Predicate<RequestHandler> {
    @Override
    public boolean test(RequestHandler requestHandler) {
        HandlerMethod handlerMethod = requestHandler.getHandlerMethod();
        Class<?> beanType = handlerMethod.getBeanType();
        if (!beanType.isAnnotationPresent(CrudExclude.class)) {
            return true;
        }
        CrudExclude crudExclude = beanType.getAnnotation(CrudExclude.class);
        CrudMethod[] excludeMethods = crudExclude.value();
        String methodName = handlerMethod.getMethod().getName();
        return !Arrays.stream(excludeMethods).anyMatch(crudMethod -> methodName.equals(crudMethod.getName()));
    }
}
