package io.github.pengxianggui.crud.config;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import io.github.pengxianggui.crud.dao.BaseMapper;
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
        initEntityClassToMapperRegistry();
        initTypeHandlerRegistry();
    }

    private void initEntityClassToMapperRegistry() {
        log.debug("Init entityClassToMapperRegistry...");
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
                            entityClassToMapperRegistry.put((Class<?>) actualType, mapper);
                        }
                    }
                }
            }
        }
    }

    private void initTypeHandlerRegistry() {
        if (MapperResolver.typeHandlerRegistry == null) {
            log.debug("Init typeHandlerRegistry...");
            MapperResolver.typeHandlerRegistry = this.sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
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
