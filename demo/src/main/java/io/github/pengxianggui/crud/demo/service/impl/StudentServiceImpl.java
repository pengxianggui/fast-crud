package io.github.pengxianggui.crud.demo.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.demo.mapper.StudentMapper;
import io.github.pengxianggui.crud.demo.service.StudentService;
import io.github.pengxianggui.crud.query.Pager;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.QueryWrapperUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class StudentServiceImpl extends BaseServiceImpl<Student, StudentMapper> implements StudentService {

    @Resource
    private StudentMapper studentMapper;

    @Override
    public void init() {
        this.baseMapper = studentMapper;
        this.clazz = Student.class;
    }

    @Override
    public Pager<Student> queryPage(PagerQuery query) {
        Pager<Student> pager = new Pager<>(query.getCurrent(), query.getSize());
        QueryWrapper<Student> wrapper = QueryWrapperUtil.build(query, clazz);
        String keyword;
        Map<String, Object> extra = query.getExtra();
        if (CollectionUtil.isNotEmpty(extra) && extra.containsKey("keyword")) {
            keyword = StrUtil.toStringOrNull(extra.get("keyword"));
        } else {
            keyword = null;
        }
        wrapper.and(StrUtil.isNotBlank(keyword), w -> w.like("name", keyword).or().like("love_name", keyword));
        return page(pager, wrapper);
    }
}
