package io.github.pengxianggui.crud.demo.service.impl;

import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.demo.mapper.StudentMapper;
import io.github.pengxianggui.crud.demo.service.StudentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class StudentServiceImpl extends BaseServiceImpl<Student, StudentMapper> implements StudentService {

    @Resource
    private StudentMapper studentMapper;

    @Override
    public void init() {
        this.baseMapper = studentMapper;
        this.clazz = Student.class;
    }
}
