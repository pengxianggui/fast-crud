package io.github.pengxianggui.crud.demo.service;

import com.google.common.collect.Lists;
import io.github.pengxianggui.crud.demo.controller.order.dto.OrdersPageDTO;
import io.github.pengxianggui.crud.demo.service.order.OrdersService;
import io.github.pengxianggui.crud.query.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author pengxg
 * @date 2025/5/23 11:31
 */
@SpringBootTest
public class OrderServiceTest {
    @Resource
    private OrdersService ordersService;

    @Test
    public void testPageDto() {
        PagerQuery query = new PagerQuery();
        List<Cond> condList = Lists.newArrayList(Cond.of("orderNo", Opt.EQ, "MD202312040001"));
        query.setConds(condList);
        query.setOrders(Lists.newArrayList(new Order("createTime", true)));
        Pager<OrdersPageDTO> page = ordersService.queryPage(query, OrdersPageDTO.class);
        Assertions.assertTrue(page.getRecords().size() == 1);
    }
}
