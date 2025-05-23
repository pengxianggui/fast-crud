package io.github.pengxianggui.crud.demo.service.order.impl;

import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.demo.domain.order.OrderItem;
import io.github.pengxianggui.crud.demo.mapper.order.OrderItemMapper;
import io.github.pengxianggui.crud.demo.service.order.OrderItemService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OrderItemServiceImpl extends BaseServiceImpl<OrderItem, OrderItemMapper> implements OrderItemService {

    @Resource
    private OrderItemMapper orderItemMapper;

    @Override
    public void init() {
        this.baseMapper = orderItemMapper;
        this.clazz = OrderItem.class;
    }
}
