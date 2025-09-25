package io.github.pengxianggui.crud.demo.util;

import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.util.EntityUtil;
import io.github.pengxianggui.crud.util.TableFieldInfoWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author pengxg
 * @date 2025/9/24 17:04
 */
@SpringBootTest
public class EntityUtilTest {
    @Test
    public void testGetTableFieldInfo() {
        TableFieldInfoWrapper tableFieldInfo = EntityUtil.getTableFieldInfo(Student.class, "id");
        Assertions.assertNotNull(tableFieldInfo);
    }
}
