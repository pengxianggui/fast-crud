package io.github.pengxianggui.crud.demo.service;

import io.github.pengxianggui.crud.BaseService;
import io.github.pengxianggui.crud.demo.controller.vo.StudentPageVO;
import io.github.pengxianggui.crud.demo.domain.Student;

public interface StudentService extends BaseService<Student> {

    int update(StudentPageVO model, Boolean updateNull);

    int insert(StudentPageVO model);
}