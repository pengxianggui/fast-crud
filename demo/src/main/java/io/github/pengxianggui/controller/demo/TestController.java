package io.github.pengxianggui.controller.demo;

import io.github.pengxianggui.crud.BaseController;
import io.github.pengxianggui.domain.demo.Test;
import io.github.pengxianggui.service.demo.TestService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

@Api(tags="")
@RestController
@RequestMapping("test")
public class TestController extends BaseController<Test> {

    public TestController(TestService testService, Validator validator) {
        super(testService, validator);
    }
}
