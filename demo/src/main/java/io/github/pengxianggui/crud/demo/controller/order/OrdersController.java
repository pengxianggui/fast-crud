package io.github.pengxianggui.crud.demo.controller.order;

import io.github.pengxianggui.crud.BaseController;
import io.github.pengxianggui.crud.demo.controller.order.dto.OrdersDetailVO;
import io.github.pengxianggui.crud.demo.controller.order.dto.OrdersPageVO;
import io.github.pengxianggui.crud.demo.service.order.OrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.Serializable;

@Api(tags = "订单表")
@RestController
@RequestMapping("orders")
public class OrdersController extends BaseController<OrdersPageVO> {

    @Resource
    private OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        super(ordersService, OrdersPageVO.class);
    }

    @ApiOperation("详情-自定义")
    @GetMapping("{id}/detail-vo")
    public OrdersDetailVO detailVO(@PathVariable Serializable id) {
        return ordersService.getById(id, OrdersDetailVO.class);
    }
}
