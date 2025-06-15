package io.github.pengxianggui.crud.demo.service;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import io.github.pengxianggui.crud.demo.controller.order.dto.OrdersDTO;
import io.github.pengxianggui.crud.demo.service.order.OrdersService;
import io.github.pengxianggui.crud.query.*;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
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
        Field field = OrdersDTO.class.getDeclaredField("address");
//        Type type = TypeUtil.getTypeArgument(field.getGenericType(), 0);
//        Type type = TypeUtil.getTypeArgument(field.getGenericType());
        Type type = field.getType();
        System.out.println(type);
        System.out.println(ClassUtil.isSimpleValueType(TypeUtil.getClass(type)));
    }

    @Test
    public void testQueryPage() {
        PagerQuery query = new PagerQuery();
        List<Cond> condList = Lists.newArrayList(Cond.of("orderNo", Opt.EQ, "MD202312040001"));
        query.setConds(condList);
        query.setOrders(Lists.newArrayList(new Order("createTime", true)));
        Pager<OrdersDTO> page = ordersService.queryPage(query, OrdersDTO.class);
        System.out.println(JSONUtil.toJsonStr(page.getRecords()));
        Assertions.assertTrue(page.getRecords().size() == 1);
    }

    @Test
    public void testGetOne() {
        Query query = new Query("orderNo", "MD202312040001");
        OrdersDTO orderDTO = ordersService.getOne(query, OrdersDTO.class);
        System.out.println(JSONUtil.toJsonStr(orderDTO));
        Assertions.assertTrue(orderDTO != null);
    }

    @Test
    public void testExists() {
        List<Cond> condList = Lists.newArrayList(Cond.of("orderNo", Opt.EQ, "MD202312040001"));
        boolean exited = ordersService.exists(condList);
        System.out.println(exited);
        Assertions.assertTrue(exited);
    }

    @Test
    public void testExists1() {
        List<Cond> condList = Lists.newArrayList(Cond.of("orderNo", Opt.EQ, "MD202312040001"));
        boolean exited = ordersService.exists(condList, OrdersDTO.class);
        System.out.println(exited);
        Assertions.assertTrue(exited);
    }

    @Test
    public void testUpdateJoin() {
        Query query = new Query("orderNo", "MD202312040001");
        OrdersDTO orderDTO = ordersService.getOne(query, OrdersDTO.class);
        System.out.println("old remark: " + orderDTO.getRemark());
        System.out.println("old provinceName: " + orderDTO.getProvinceName());
        System.out.println("old provinceName in address: " + orderDTO.getAddress().getProvinceName());
        orderDTO.setRemark("我是更新后的备注值");
        orderDTO.setProvinceName("浙江省1");
        // 更新
        int updateCount = ordersService.updateById(new UpdateModelWrapper<>(orderDTO), OrdersDTO.class);
        Assertions.assertTrue(updateCount == 2);
        orderDTO = ordersService.getOne(query, OrdersDTO.class);
        System.out.println("new remark: " + orderDTO.getRemark());
        System.out.println("new provinceName: " + orderDTO.getProvinceName());
        System.out.println("new provinceName in address: " + orderDTO.getAddress().getProvinceName());
    }
}
