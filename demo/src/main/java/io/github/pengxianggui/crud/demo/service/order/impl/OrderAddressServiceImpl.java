package io.github.pengxianggui.crud.demo.service.order.impl;

import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.demo.domain.order.OrderAddress;
import io.github.pengxianggui.crud.demo.mapper.order.OrderAddressMapper;
import io.github.pengxianggui.crud.demo.service.order.OrderAddressService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OrderAddressServiceImpl extends BaseServiceImpl<OrderAddress, OrderAddressMapper> implements OrderAddressService {

    @Resource
    private OrderAddressMapper orderAddressMapper;

    @Override
    public void init() {
        this.baseMapper = orderAddressMapper;
        this.clazz = OrderAddress.class;
    }
}
