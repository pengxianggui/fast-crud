package io.github.pengxianggui.crud.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.pengxianggui.crud.BaseService;
import io.github.pengxianggui.crud.demo.controller.vo.StudentPageVO;
import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.query.PagerQuery;

public interface StudentService extends BaseService<Student> {

    int update(StudentPageVO model, Boolean updateNull);

    int insert(StudentPageVO model);

    IPage<StudentPageVO> pageVO(PagerQuery query);
}