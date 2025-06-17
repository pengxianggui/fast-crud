package io.github.pengxianggui.crud.demo.mybatisjoin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.github.pengxianggui.crud.demo.controller.order.dto.OrdersDTO;
import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.demo.domain.order.OrderAddress;
import io.github.pengxianggui.crud.demo.domain.order.Orders;
import io.github.pengxianggui.crud.demo.mapper.StudentMapper;
import io.github.pengxianggui.crud.demo.mapper.order.OrdersMapper;
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
    @Resource
    private OrdersMapper ordersMapper;

    @Test
    public void testJoinPage() {
        MPJLambdaWrapper<Student> wrapper = new MPJLambdaWrapper<>(Student.class)
                .selectAll()
                .eq(Student::getName, "刘备");
        IPage<Student> page = studentMapper.selectJoinPage(new Page<>(1, 10), Student.class, wrapper);
        Assertions.assertTrue(page.getRecords().size() == 1);
    }

    @Test
    public void testJoinPage1() {
        MPJLambdaWrapper<Orders> wrapper = new MPJLambdaWrapper<>(Orders.class)
                .selectAll()
                .select(OrderAddress::getProvinceNo, OrderAddress::getProvinceName,
                        OrderAddress::getCityNo, OrderAddress::getCityName,
                        OrderAddress::getCountyNo, OrderAddress::getCountyName,
                        OrderAddress::getStreet, OrderAddress::getReceiverName, OrderAddress::getReceiverMobi)
//                .leftJoin(OrderAddress.class, OrderAddress::getOrderNo, Orders::getOrderNo)
                .leftJoin(OrderAddress.class, on -> on.eq(OrderAddress::getOrderNo, Orders::getOrderNo))
                .eq(Orders::getOrderStatus, "UNDEFINE")
                .like(OrderAddress::getCityName, "上海")
                .orderByAsc(OrderAddress::getCityName);
        IPage<OrdersDTO> page = ordersMapper.selectJoinPage(new Page<>(1, 10), OrdersDTO.class, wrapper);
        Assertions.assertTrue(page.getRecords().size() == 1);
    }


}
