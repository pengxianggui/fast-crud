package io.github.pengxianggui.crud.demo.controller;

import io.github.pengxianggui.crud.dynamic.Crud;
import io.github.pengxianggui.crud.dynamic.CrudMethod;
import io.github.pengxianggui.crud.dynamic.CrudService;
import io.github.pengxianggui.crud.demo.service.StudentService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags="学生")
@RestController
@RequestMapping("student")
@Crud(exclude = {CrudMethod.DELETE_BATCH, CrudMethod.INSERT_BATCH})
public class StudentController {

    @Resource
    @CrudService
    private StudentService studentService;

}
