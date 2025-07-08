package io.github.pengxianggui.crud.demo.mybatisjoin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.demo.mapper.StudentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author pengxg
 * @date 2025/5/22 09:10
 */
@SpringBootTest
public class MpJoinTest {
    @Resource
    private StudentMapper studentMapper;

    @Test
    public void testJoinPage() {
        MPJLambdaWrapper<Student> wrapper = new MPJLambdaWrapper<>(Student.class)
                .selectAll()
                .eq(Student::getName, "刘备");
        IPage<Student> page = studentMapper.selectJoinPage(new Page<>(1, 10), Student.class, wrapper);
        Assertions.assertTrue(page.getRecords().size() == 1);
    }

}
