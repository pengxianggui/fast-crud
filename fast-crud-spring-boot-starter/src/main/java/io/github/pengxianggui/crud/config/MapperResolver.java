package io.github.pengxianggui.crud.config;

import io.github.pengxianggui.crud.dao.BaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/7/5 14:08
 */
@Slf4j
public class MapperResolver implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private final Map<Class<?>, BaseMapper> entityClassToMapper = new HashMap<>();

    @PostConstruct
    public void init() {
        Map<String, BaseMapper> mappers = applicationContext.getBeansOfType(BaseMapper.class);
        for (BaseMapper mapper : mappers.values()) {
            Class<?> mapperClass = findMapperInterface(mapper.getClass()); // 避免是代理类
            Type[] interfaces = mapperClass.getGenericInterfaces();

            for (Type type : interfaces) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) type;
                    if (BaseMapper.class.getName().equals(pt.getRawType().getTypeName())) {
                        Type actualType = pt.getActualTypeArguments()[0];
                        if (actualType instanceof Class) {
                            log.debug("Put a mapping from entity to mapper into the registry!  {} -> {}", actualType, mapperClass);
                            entityClassToMapper.put((Class<?>) actualType, mapper);
                        }
                    }
                }
            }
        }
    }

    private Class<?> findMapperInterface(Class<?> proxyClass) {
        for (Class<?> iface : proxyClass.getInterfaces()) {
            for (Type type : iface.getGenericInterfaces()) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) type;
                    if (pt.getRawType() instanceof Class && BaseMapper.class.equals(pt.getRawType())) {
                        return iface;
                    }
                }
            }
        }
        throw new IllegalStateException("Can't find interface of " + proxyClass.getName() + " that should be implemented to " + BaseMapper.class.getName());
    }

    public BaseMapper getMapperByEntityClass(Class<?> entityClass) {
        return entityClassToMapper.get(entityClass);
    }
}
