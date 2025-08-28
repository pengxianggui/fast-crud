package io.github.pengxianggui.crud.config;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pengxg
 * @date 2025/7/5 14:08
 */
@Slf4j
public class MapperResolver implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private SqlSessionFactory sqlSessionFactory;

    public MapperResolver(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static final Map<Class<?>, BaseMapper> entityClassToMapperRegistry = new ConcurrentHashMap<>();

    private static TypeHandlerRegistry typeHandlerRegistry = null;

    @PostConstruct
    public void init() {
        try {
            initEntityClassToMapperRegistry();
            initTypeHandlerRegistry();
        } catch (Exception e) {
            log.error("MapperResolver init error", e);
        }
    }

    private void initEntityClassToMapperRegistry() {
        log.debug("Init entityClassToMapperRegistry...");
        String[] mapperBeanNames = applicationContext.getBeanNamesForType(BaseMapper.class);
        for (String mapperBeanName : mapperBeanNames) {
            BaseMapper mapper = applicationContext.getBean(mapperBeanName, BaseMapper.class);
            Class mapperClass = applicationContext.getType(mapperBeanName);

            Class<?> entityClass = resolveEntityClass(mapperClass);
            if (entityClass != null) {
                log.debug("Put a mapping from entity to mapper into the registry! {} -> {}", entityClass, mapperClass);
                entityClassToMapperRegistry.put(entityClass, mapper);
            } else {
                log.warn("Cannot resolve entity class for mapper: {}", mapperClass);
            }
        }
    }

    /**
     * 递归解析某个类/接口的泛型，找到 BaseMapper<Entity, ?> 的 Entity 类型
     */
    private Class<?> resolveEntityClass(Class<?> mapperClass) {
        // 先检查当前类实现的接口
        for (Type type : mapperClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                if (pt.getRawType() instanceof Class && BaseMapper.class.isAssignableFrom((Class<?>) pt.getRawType())) {
                    Type actualType = pt.getActualTypeArguments()[0];
                    if (actualType instanceof Class<?>) {
                        return (Class<?>) actualType;
                    }
                }
            } else if (type instanceof Class) {
                // 如果是普通接口，递归继续解析
                Class<?> result = resolveEntityClass((Class<?>) type);
                if (result != null) {
                    return result;
                }
            }
        }

        // 再检查父类
        Type superType = mapperClass.getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) superType;
            if (pt.getRawType() instanceof Class && BaseMapper.class.isAssignableFrom((Class<?>) pt.getRawType())) {
                Type actualType = pt.getActualTypeArguments()[0];
                if (actualType instanceof Class<?>) {
                    return (Class<?>) actualType;
                }
            }
        } else if (superType instanceof Class) {
            return resolveEntityClass((Class<?>) superType);
        }

        return null;
    }

    private void initTypeHandlerRegistry() {
        if (MapperResolver.typeHandlerRegistry == null) {
            log.debug("Init typeHandlerRegistry...");
            MapperResolver.typeHandlerRegistry = this.sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
        }
    }

    /**
     * 提供一个实体类型，获取其对应的Mapper
     *
     * @param entityClass
     * @return
     */
    public static BaseMapper getMapperByEntityClass(Class<?> entityClass) {
        return entityClassToMapperRegistry.get(entityClass);
    }

    public static String getMapping(Field field) {
        Class<? extends TypeHandler> typeHandleClazz = getTypeHandler(field);
        if (typeHandleClazz == null) {
            return null;
        }
        JdbcType jdbcType = JdbcType.UNDEFINED;
        if (field.isAnnotationPresent(TableField.class)) {
            TableField tableField = field.getAnnotation(TableField.class);
            jdbcType = tableField.jdbcType();
        }
        return StrUtil.format("javaType={},jdbcType={},typeHandler={}",
                field.getType().getName(), jdbcType, typeHandleClazz.getName());
    }

    /**
     * 获取某个字段上可能声明的typehandler类型, 先找字段上{@link TableField#typeHandler()}, 若有则直接返回，否则再找全局，若有直接返回。
     *
     * @param field
     * @return 无则返回null
     */
    public static Class<? extends TypeHandler> getTypeHandler(Field field) {
        JdbcType jdbcType = JdbcType.UNDEFINED;
        if (field.isAnnotationPresent(TableField.class)) {
            TableField tableField = field.getAnnotation(TableField.class);
            Class<? extends TypeHandler> typeHandlerClazz = tableField.typeHandler();
            if (typeHandlerClazz != UnknownTypeHandler.class) {
                return typeHandlerClazz;
            }
            jdbcType = tableField.jdbcType();
        }

        if (typeHandlerRegistry != null) {
            TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(field.getType(), jdbcType);
            return typeHandler.getClass();
        }
        return null;
    }
}
