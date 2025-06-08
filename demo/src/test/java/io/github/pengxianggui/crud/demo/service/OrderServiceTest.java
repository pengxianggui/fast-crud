package io.github.pengxianggui.crud.demo.service;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import io.github.pengxianggui.crud.demo.controller.order.dto.OrdersPageDTO;
import io.github.pengxianggui.crud.demo.service.order.OrdersService;
import io.github.pengxianggui.crud.query.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author pengxg
 * @date 2025/5/23 11:31
 */
@SpringBootTest
public class OrderServiceTest {
    @Resource
    private OrdersService ordersService;

    public static void main(String[] args) throws NoSuchFieldException {
        Field field = OrdersPageDTO.class.getDeclaredField("address");
//        Type type = TypeUtil.getTypeArgument(field.getGenericType(), 0);
//        Type type = TypeUtil.getTypeArgument(field.getGenericType());
        Type type = field.getType();
        System.out.println(type);
        System.out.println(ClassUtil.isSimpleValueType(TypeUtil.getClass(type)));
    }

    @Test
    public void testPageDto() {
        PagerQuery query = new PagerQuery();
        List<Cond> condList = Lists.newArrayList(Cond.of("orderNo", Opt.EQ, "MD202312040001"));
        query.setConds(condList);
        query.setOrders(Lists.newArrayList(new Order("createTime", true)));
        Pager<OrdersPageDTO> page = ordersService.queryPage(query, OrdersPageDTO.class);
        System.out.println(JSONUtil.toJsonStr(page.getRecords()));
        Assertions.assertTrue(page.getRecords().size() == 1);
    }
}
