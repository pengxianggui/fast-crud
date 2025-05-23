package io.github.pengxianggui.crud.demo.controller.order;

import io.github.pengxianggui.crud.demo.service.order.OrderItemService;
import io.github.pengxianggui.crud.demo.domain.order.OrderItem;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.pengxianggui.crud.BaseController;

import javax.annotation.Resource;

@Api(tags="订单行")
@RestController
@RequestMapping("order_item")
public class OrderItemController extends BaseController<OrderItem>{

    @Resource
    private OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        super(orderItemService);
    }

}
