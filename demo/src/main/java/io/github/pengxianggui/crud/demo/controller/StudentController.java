package io.github.pengxianggui.crud.demo.controller;

import io.github.pengxianggui.crud.BaseController;
import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.demo.service.StudentService;
import io.github.pengxianggui.crud.dynamic.CrudExclude;
import io.github.pengxianggui.crud.dynamic.CrudMethod;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags="学生")
@CrudExclude({CrudMethod.EXISTS, CrudMethod.EXPORT})
@RestController
@RequestMapping("student")
public class StudentController extends BaseController<Student> {

    public StudentController(StudentService studentService) {
        super(studentService);
    }
}
