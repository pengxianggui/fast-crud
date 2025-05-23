package io.github.pengxianggui.crud.demo.service;

import io.github.pengxianggui.crud.demo.service.order.OrdersService;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author pengxg
 * @date 2025/5/23 11:31
 */
@SpringBootTest
public class OrderServiceTest {
    @Resource
    private OrdersService ordersService;
}
