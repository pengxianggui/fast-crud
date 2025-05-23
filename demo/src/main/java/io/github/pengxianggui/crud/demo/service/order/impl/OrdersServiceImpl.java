package io.github.pengxianggui.crud.demo.service.order.impl;

import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.demo.domain.order.Orders;
import io.github.pengxianggui.crud.demo.mapper.order.OrdersMapper;
import io.github.pengxianggui.crud.demo.service.order.OrdersService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OrdersServiceImpl extends BaseServiceImpl<Orders, OrdersMapper> implements OrdersService {

    @Resource
    private OrdersMapper ordersMapper;

    @Override
    public void init() {
        this.baseMapper = ordersMapper;
        this.clazz = Orders.class;
    }
}
