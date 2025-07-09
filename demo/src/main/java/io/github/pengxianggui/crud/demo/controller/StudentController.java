package io.github.pengxianggui.crud.demo.controller;

import io.github.pengxianggui.crud.BaseController;
import io.github.pengxianggui.crud.CrudExclude;
import io.github.pengxianggui.crud.CrudMethod;
import io.github.pengxianggui.crud.demo.controller.vo.StudentDetailVO;
import io.github.pengxianggui.crud.demo.controller.vo.StudentPageVO;
import io.github.pengxianggui.crud.demo.service.StudentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "学生")
//@CrudExclude(CrudMethod.UPDATE_BATCH)
@RestController
@RequestMapping("student")
public class StudentController extends BaseController<StudentPageVO> {

    @Resource
    private StudentService studentService;

    public StudentController(StudentService studentService) {
        super(studentService, StudentPageVO.class);
    }

    @ApiOperation("详情-自定义")
    @GetMapping("{id}/detail-vo")
    public StudentDetailVO detailVO(@PathVariable Integer id) {
        return studentService.getById(id, StudentDetailVO.class);
    }

    @Override
    protected int insert(StudentPageVO model) {
        return studentService.insert(model);
    }

    @Override
    protected int insertBatch(List<StudentPageVO> models) {
        return models.stream().mapToInt(m -> insert(m)).sum();
    }

    @Override
    public int update(StudentPageVO model, Boolean updateNull) {
        return studentService.update(model, updateNull);
    }

    @Override
    public int updateBatch(List<StudentPageVO> models) {
        return models.stream().mapToInt(m -> update(m, null)).sum();
    }
}
