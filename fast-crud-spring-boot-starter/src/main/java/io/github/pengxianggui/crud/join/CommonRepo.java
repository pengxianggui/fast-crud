package io.github.pengxianggui.crud.join;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.pengxianggui.crud.config.MapperResolver;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 提供一个通用的repo类, 提供通用db操作
 *
 * @author pengxg
 * @date 2025/7/5 15:47
 */
public class CommonRepo {

    /**
     * 批量保存或更新。不限定类型
     *
     * @param map
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public <E> int saveOrUpdateBatch(Map<Class<E>, List<E>> map) {
        int count = 0;
        for (Map.Entry<Class<E>, List<E>> entry : map.entrySet()) {
            Class clazz = entry.getKey();
            List entities = entry.getValue();
            count += saveOrUpdateBatch(clazz, entities);
        }
        return count;
    }

    public int saveOrUpdate(Class<?> clazz, Object entity) {
        BaseMapper mapper = MapperResolver.getMapperByEntityClass(clazz);
        return mapper.insertOrUpdate(entity) ? 1 : 0;
    }

    /**
     * 批量保存或更新
     *
     * @param clazz
     * @param entities
     * @param <E>
     * @return
     */
    public <E> int saveOrUpdateBatch(Class<E> clazz, List<E> entities) {
        if (CollectionUtil.isEmpty(entities)) {
            return 0;
        }
        BaseMapper mapper = MapperResolver.getMapperByEntityClass(clazz);
//            if (mapper == null) {
//                // 降级采用beanName拼接的方式? NO 舍确定而逐不确定
//            }
        List<BatchResult> results = mapper.insertOrUpdate(entities);
        return results.stream().flatMapToInt(r -> Arrays.stream(r.getUpdateCounts())).sum();
    }
}
