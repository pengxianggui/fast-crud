package io.github.pengxianggui.crud.export;

import cn.hutool.extra.spring.SpringUtil;
import io.github.pengxianggui.crud.FastCrudProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 下拉列(含switch)的取值转义测试
 *
 * @author pengxg
 */
class SelectColumnHandlerTest {

    @BeforeAll
    static void setUpSpringUtil() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(FastCrudProperty.class, FastCrudProperty::new);
        context.refresh();
        new SpringUtil().setApplicationContext(context);
    }

    @Test
    void switchShouldEscapeWithConfiguredText() {
        // 对应: <fast-table-column-switch prop="graduated" active-text="Y" inactive-text="N"/>
        SelectColumnHandler handler = switchHandler(props("activeText", "Y", "inactiveText", "N"));

        assertEquals("Y", handler.labelOf(true));
        assertEquals("N", handler.labelOf(false));
        // 兼容类型/写法差异
        assertEquals("Y", handler.labelOf("true"));

        assertEquals(Boolean.TRUE, handler.parseImportValue("Y", Boolean.class));
        assertEquals(Boolean.FALSE, handler.parseImportValue("N", Boolean.class));
        assertEquals(Boolean.TRUE, handler.parseImportValue("true", Boolean.class));
    }

    @Test
    void switchShouldUseDefaultYesNoWhenTextNotConfigured() {
        SelectColumnHandler handler = switchHandler(new HashMap<>());

        assertEquals("是", handler.labelOf(true));
        assertEquals("否", handler.labelOf(false));
        assertEquals(Boolean.TRUE, handler.parseImportValue("是", Boolean.class));
        assertEquals(Boolean.FALSE, handler.parseImportValue("否", Boolean.class));
    }

    @Test
    void switchShouldHonorCustomActiveInactiveValue() {
        Map<String, Object> props = new HashMap<>();
        props.put("activeValue", 1);
        props.put("inactiveValue", 0);
        props.put("activeText", "启用");
        props.put("inactiveText", "停用");
        SelectColumnHandler handler = switchHandler(props);

        assertEquals("启用", handler.labelOf(1));
        assertEquals("停用", handler.labelOf(0));
        assertEquals(1, handler.parseImportValue("启用", Integer.class));
        assertEquals(0, handler.parseImportValue("0", Integer.class));
    }

    @Test
    void selectShouldEscapeByOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put("valKey", "value");
        props.put("labelKey", "label");
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(option("男", "1"));
        options.add(option("女", "0"));
        props.put("options", options);

        Map<String, Object> column = column("fast-table-column-select", "sex", props);
        SelectColumnHandler handler = new SelectColumnHandler("fast-table-column-select", column);

        assertEquals("男", handler.labelOf("1"));
        assertEquals("0", handler.parseImportValue("女", String.class));
    }

    private static SelectColumnHandler switchHandler(Map<String, Object> props) {
        Map<String, Object> column = column("FastTableColumnSwitch", "graduated", props);
        return new SelectColumnHandler("FastTableColumnSwitch", column);
    }

    private static Map<String, Object> column(String component, String col, Map<String, Object> props) {
        Map<String, Object> column = new HashMap<>();
        column.put("col", col);
        column.put("label", col);
        column.put("tableColumnComponentName", component);
        column.put("props", props);
        return column;
    }

    private static Map<String, Object> props(Object... kv) {
        Map<String, Object> props = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            props.put((String) kv[i], kv[i + 1]);
        }
        return props;
    }

    private static Map<String, Object> option(String label, String value) {
        Map<String, Object> option = new HashMap<>();
        option.put("label", label);
        option.put("value", value);
        return option;
    }
}
