package io.github.pengxianggui.crud.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.demo.controller.vo.StudentPageVO;
import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.demo.domain.StudentSensitive;
import io.github.pengxianggui.crud.demo.mapper.StudentMapper;
import io.github.pengxianggui.crud.demo.mapper.StudentSensitiveMapper;
import io.github.pengxianggui.crud.demo.service.StudentService;
import io.github.pengxianggui.crud.join.MPJLambdaWrapperBuilder;
import io.github.pengxianggui.crud.query.PagerQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class StudentServiceImpl extends BaseServiceImpl<Student, StudentMapper> implements StudentService {
    @Resource
    private StudentSensitiveMapper studentSensitiveMapper;

    @Override
    public IPage<StudentPageVO> pageVO(PagerQuery query) {
        Page<StudentPageVO> pager = new Page<>(query.getCurrent(), query.getSize());
        Map<String, Object> extra = query.getExtra();
        String keyword = (CollectionUtil.isNotEmpty(extra) && extra.containsKey("keyword"))
                ? StrUtil.toStringOrNull(extra.get("keyword"))
                : null;
        MPJLambdaWrapper<Student> wrapper = new MPJLambdaWrapperBuilder(StudentPageVO.class)
                .query(query)
                .build();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("name", keyword).or().like("love_name", keyword));
        }
        return getBaseMapper().selectJoinPage(pager, StudentPageVO.class, wrapper);
    }

    @Override
    public int insert(StudentPageVO model) {
        Student student = BeanUtil.copyProperties(model, Student.class);
        int count = insert(student);
        StudentSensitive studentSensitive = new StudentSensitive();
        studentSensitive.setStudentId(student.getId());
        studentSensitive.setIdCard(model.getIdCard());
        studentSensitive.setAddress(model.getAddress());
        studentSensitive.setPhone(model.getPhone());
        count += studentSensitiveMapper.insertOrUpdate(studentSensitive) ? 1 : 0;
        return count;
    }

    @Override
    public int update(StudentPageVO model, Boolean updateNull) {
        Student student = BeanUtil.copyProperties(model, Student.class);
        int count = updateById(student, updateNull);
        StudentSensitive studentSensitive = studentSensitiveMapper.selectJoinOne(new MPJLambdaWrapper<>(StudentSensitive.class)
                .eq(StudentSensitive::getStudentId, student.getId()));
        if (studentSensitive == null) {
            studentSensitive = new StudentSensitive();
            studentSensitive.setStudentId(student.getId());
        }
        studentSensitive.setIdCard(model.getIdCard());
        studentSensitive.setAddress(model.getAddress());
        studentSensitive.setPhone(model.getPhone());
        count += studentSensitiveMapper.insertOrUpdate(studentSensitive) ? 1 : 0;
        return count;
    }
}
