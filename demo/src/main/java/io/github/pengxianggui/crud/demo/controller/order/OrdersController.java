package io.github.pengxianggui.crud.demo.controller.order;

import io.github.pengxianggui.crud.demo.controller.order.dto.OrdersPageDTO;
import io.github.pengxianggui.crud.demo.service.order.OrdersService;
import io.github.pengxianggui.crud.demo.domain.order.Orders;
import io.github.pengxianggui.crud.query.Pager;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.PagerView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.pengxianggui.crud.BaseController;

import javax.annotation.Resource;

@Api(tags = "订单表")
@RestController
@RequestMapping("orders")
public class OrdersController extends BaseController<Orders> {

    @Resource
    private OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        super(ordersService);
    }

    @ApiOperation("分页查询")
    @PostMapping("page/v1")
    public PagerView<OrdersPageDTO> pageVO(@RequestBody @Validated PagerQuery query) {
        Pager<OrdersPageDTO> pager = ordersService.queryPage(query, OrdersPageDTO.class);
        return new PagerView<>(pager.getCurrent(), pager.getSize(), pager.getTotal(), pager.getRecords());
    }

}
