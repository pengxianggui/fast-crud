package io.github.pengxianggui.service.demo.impl;

import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.domain.demo.Test;
import io.github.pengxianggui.mapper.demo.TestMapper;
import io.github.pengxianggui.service.demo.TestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TestServiceImpl extends BaseServiceImpl<Test, TestMapper> implements TestService {

    @Resource
    private TestMapper testMapper;


    @Override
    public void init() {
        this.baseMapper = testMapper;
        this.clazz = Test.class;
    }
}
