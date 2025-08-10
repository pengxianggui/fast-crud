package io.github.pengxianggui.crud.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.pengxianggui.crud.BaseController;
import io.github.pengxianggui.crud.demo.controller.vo.StudentDetailVO;
import io.github.pengxianggui.crud.demo.controller.vo.StudentPageVO;
import io.github.pengxianggui.crud.demo.service.StudentService;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.PagerView;
import io.github.pengxianggui.crud.valid.CrudInsert;
import io.github.pengxianggui.crud.valid.CrudUpdate;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation("插入")
    @PostMapping("insert")
    @Override
    public int insert(@RequestBody @Validated(CrudInsert.class) StudentPageVO model) {
        return studentService.insert(model);
    }

    @ApiOperation("批量插入")
    @PostMapping("insert/batch")
    @Override
    public int insertBatch(@Validated(CrudInsert.class) @RequestBody List<StudentPageVO> models) {
        return models.stream().mapToInt(m -> studentService.insert(m)).sum();
    }

    @ApiOperation("编辑")
    @PostMapping("update")
    @Override
    public int update(@Validated(CrudUpdate.class) @RequestBody UpdateModelWrapper<StudentPageVO> modelWrapper) {
        return studentService.update(modelWrapper.getModel(), modelWrapper.get_updateNull());
    }

    @ApiOperation(value = "批量编辑", notes = "不支持个性化选择_updateNull")
    @PostMapping("update/batch")
    @Override
    public int updateBatch(@Validated(CrudUpdate.class) @RequestBody List<StudentPageVO> models) {
        return models.stream().mapToInt(m -> studentService.update(m, null)).sum();
    }

    @ApiOperation("分页查询")
    @PostMapping("page")
    @Override
    public PagerView<StudentPageVO> page(@Validated @RequestBody PagerQuery query) {
        IPage<StudentPageVO> page = studentService.pageVO(query);
        return new PagerView<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }
}
