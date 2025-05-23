package io.github.pengxianggui.crud.demo.controller.order;

import io.github.pengxianggui.crud.demo.service.order.OrderAddressService;
import io.github.pengxianggui.crud.demo.domain.order.OrderAddress;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.pengxianggui.crud.BaseController;

import javax.annotation.Resource;

@Api(tags="订单地址信息表")
@RestController
@RequestMapping("order_address")
public class OrderAddressController extends BaseController<OrderAddress>{

    @Resource
    private OrderAddressService orderAddressService;

    public OrderAddressController(OrderAddressService orderAddressService) {
        super(orderAddressService);
    }

}
