package io.github.pengxianggui.crud.demo.service;

import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.query.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author pengxg
 * @date 2025/5/22 10:40
 */
@SpringBootTest
public class StudentServiceTest {
    @Resource
    private StudentService studentService;

    /**
     * 查询 属国="蜀" and (性别 = "男" or 年龄 >= 20) 的学生
     */
    @Test
    public void testPage() {
        PagerQuery pagerQuery = new PagerQuery();
        Cond nestCond = Cond.of(Rel.OR, Lists.list(Cond.of("sex", Opt.EQ, "1"), Cond.of("age", Opt.GE, 20)));
        pagerQuery.setConds(Lists.list(Cond.of("state", Opt.EQ, "2"), nestCond));
        Pager<Student> pager = studentService.queryPage(pagerQuery);
        Assertions.assertTrue(pager.getRecords().size() > 0);
    }
}
