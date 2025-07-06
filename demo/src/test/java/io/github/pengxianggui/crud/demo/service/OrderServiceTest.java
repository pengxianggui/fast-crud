package io.github.pengxianggui.crud.demo.service;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import com.google.common.collect.Lists;
import io.github.pengxianggui.crud.demo.controller.order.dto.OrdersDetailVO;
import io.github.pengxianggui.crud.demo.controller.order.dto.OrdersPageVO;
import io.github.pengxianggui.crud.demo.domain.order.OrderAddress;
import io.github.pengxianggui.crud.demo.domain.order.Orders;
import io.github.pengxianggui.crud.demo.mapper.order.OrdersMapper;
import io.github.pengxianggui.crud.demo.service.order.OrdersService;
import io.github.pengxianggui.crud.join.UpdateJoinWrapperBuilder;
import io.github.pengxianggui.crud.query.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author pengxg
 * @date 2025/5/23 11:31
 */
@Rollback
@Transactional
@SpringBootTest
public class OrderServiceTest {
    @Resource
    private OrdersService ordersService;
    @Resource
    private OrdersMapper ordersMapper;

    public static void main(String[] args) throws NoSuchFieldException {
        Field field = OrdersDetailVO.class.getDeclaredField("address");
//        Type type = TypeUtil.getTypeArgument(field.getGenericType(), 0);
//        Type type = TypeUtil.getTypeArgument(field.getGenericType());
        Type type = field.getType();
        System.out.println(type);
        System.out.println(ClassUtil.isSimpleValueType(TypeUtil.getClass(type)));
    }

    @Test
    public void testQueryPage() {
        PagerQuery query = new PagerQuery();
        List<Cond> condList = Lists.newArrayList(Cond.of("orderNo", Opt.EQ, "MD202312040001"));
        query.setConds(condList);
        query.setOrders(Lists.newArrayList(new Order("createTime", true)));
        IPage<OrdersDetailVO> page = ordersService.queryPage(query, OrdersDetailVO.class);
        System.out.println(JSONUtil.toJsonStr(page.getRecords()));
        Assertions.assertTrue(page.getRecords().size() == 1);
    }

    @Test
    public void testGetOne() {
        Query query = new Query("orderNo", "MD202312040001");
        OrdersDetailVO orderDTO = ordersService.queryOne(query, OrdersDetailVO.class);
        System.out.println(JSONUtil.toJsonStr(orderDTO));
        Assertions.assertTrue(orderDTO != null);
    }

    @Test
    public void testExists() {
        List<Cond> condList = Lists.newArrayList(Cond.of("orderNo", Opt.EQ, "MD202312040001"));
        boolean exited = ordersService.exists(condList);
        System.out.println(exited);
        Assertions.assertTrue(exited);
    }

    @Test
    public void testExists1() {
        List<Cond> condList = Lists.newArrayList(Cond.of("orderNo", Opt.EQ, "MD202312040001"));
        boolean exited = ordersService.exists(condList, OrdersDetailVO.class);
        System.out.println(exited);
        Assertions.assertTrue(exited);
    }

    /**
     * 直接基于dto更新
     */
    @Test
    public void testUpdateJoin() {
        Query query = new Query("orderNo", "MD202312040001");
        OrdersDetailVO orderDTO = ordersService.queryOne(query, OrdersDetailVO.class);
        System.out.println("old remark: " + orderDTO.getRemark());
        System.out.println("old provinceNo: " + orderDTO.getProvinceNo());
        System.out.println("old provinceName: " + orderDTO.getProvinceName());
        System.out.println("old provinceName in address: " + orderDTO.getAddress().getProvinceName());
        orderDTO.setRemark("我是更新后的备注值");
        orderDTO.setProvinceNo(null);
        orderDTO.setProvinceName("浙江省1");
        // 更新
        boolean updateNull = false;
        int updateCount = ordersService.updateById(orderDTO, OrdersDetailVO.class, updateNull);
        Assertions.assertTrue(updateCount == 2);
        orderDTO = ordersService.queryOne(query, OrdersDetailVO.class);
        System.out.println("new remark: " + orderDTO.getRemark());
        System.out.println("new provinceNo: " + orderDTO.getProvinceNo());
        System.out.println("new provinceName: " + orderDTO.getProvinceName());
        System.out.println("new provinceName in address: " + orderDTO.getAddress().getProvinceName());
    }

    /**
     * 自定义更新时set哪些值
     */
    @Test
    public void testUpdateJoin1() {
        Query query = new Query("orderNo", "MD202312040001");
        OrdersPageVO ordersPageVO = ordersService.queryOne(query, OrdersPageVO.class);
        System.out.println("old remark: " + ordersPageVO.getRemark());
        System.out.println("old provinceNo: " + ordersPageVO.getProvinceNo());
        System.out.println("old provinceName: " + ordersPageVO.getProvinceName());
        // 更新
        UpdateJoinWrapper<Orders> wrapper = new UpdateJoinWrapperBuilder<>(Orders.class, OrdersPageVO.class)
                .where(w -> w.eq(Orders::getOrderNo, "MD202312040001"))
                .build(w -> w.set(Orders::getRemark, "我是更新后的备注值")
                        .set(OrderAddress::getProvinceNo, null)
                        .set(OrderAddress::getProvinceName, "浙江省1"));
        int updateCount = ordersMapper.updateJoin(null, wrapper);
        Assertions.assertTrue(updateCount == 2);
        ordersPageVO = ordersService.queryOne(query, OrdersPageVO.class);
        System.out.println("new remark: " + ordersPageVO.getRemark());
        System.out.println("new provinceNo: " + ordersPageVO.getProvinceNo());
        System.out.println("new provinceName: " + ordersPageVO.getProvinceName());
    }
}
