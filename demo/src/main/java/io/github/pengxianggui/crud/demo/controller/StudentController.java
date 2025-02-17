package io.github.pengxianggui.crud.demo.controller;

import io.github.pengxianggui.crud.CrudExclude;
import io.github.pengxianggui.crud.CrudMethod;
import io.github.pengxianggui.crud.demo.service.StudentService;
import io.github.pengxianggui.crud.demo.domain.Student;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.pengxianggui.crud.BaseController;

import javax.annotation.Resource;

@Api(tags="学生")
@CrudExclude(CrudMethod.EXISTS)
@RestController
@RequestMapping("student")
public class StudentController extends BaseController<Student>{

    @Resource
    private StudentService studentService;

    public StudentController(StudentService studentService) {
        super(studentService);
    }

}
