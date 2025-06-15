package io.github.pengxianggui.crud.demo.controller.order.dto;

import io.github.pengxianggui.crud.demo.domain.order.OrderAddress;
import io.github.pengxianggui.crud.demo.domain.order.OrderItem;
import io.github.pengxianggui.crud.demo.domain.order.Orders;
import io.github.pengxianggui.crud.join.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author pengxg
 * @date 2025/5/23 09:34
 */
@JoinMain(Orders.class)
@InnerJoin(value = OrderAddress.class, on = {@OnCond(field = "orderNo", targetField = "orderNo")})
@LeftJoin(value = OrderItem.class, on = {@OnCond(field = "orderNo", targetField = "orderNo")})
@Data
@ApiModel(value = "Orders分页对象")
public class OrdersDTO {
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("履约类型 1 门店履约 2 总部履约")
    private String performType;

    @ApiModelProperty("sap订单号")
    private String sapNo;

    @ApiModelProperty("第三方平台订单号")
    private String thirdNo;

    @ApiModelProperty("订单来源")
    private String orderFrom;

    @ApiModelProperty("店铺code")
    private String shopCode;

    @ApiModelProperty("店铺名称")
    private String shopName;

    @ApiModelProperty("店铺类型")
    private String shopType;

    @ApiModelProperty("订货日期")
    private LocalDateTime orderDate;

    @ApiModelProperty("数量")
    private Integer count;

    @ApiModelProperty("标准零售价")
    private Long totalAmount;

    @ApiModelProperty("物流单号")
    private String expressNo;

    @ApiModelProperty("快递公司名称")
    private String expressCompany;

    @ApiModelProperty("采购单号")
    private String purchaseNo;

    @ApiModelProperty("交货单号")
    private String deliverNo;

    @ApiModelProperty("订单状态")
    private String orderStatus;

    @ApiModelProperty("sap销售单金额")
    private Long sapAmount;

    @ApiModelProperty("签名图片")
    private String signature;

    @ApiModelProperty("活动价")
    private Long promotionAmount;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建人")
    private Long createBy;

    @ApiModelProperty("创建人姓名")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    // 订单地址信息 --------------------------------------------------------
    @RelateTo(OrderAddress.class)
    @ApiModelProperty("省编码")
    private String provinceNo;

    @RelateTo(OrderAddress.class)
    @ApiModelProperty("省名称")
    private String provinceName;

    @RelateTo(OrderAddress.class)
    @ApiModelProperty("市编码")
    private String cityNo;

    @RelateTo(OrderAddress.class)
    @ApiModelProperty("市名称")
    private String cityName;

    @RelateTo(OrderAddress.class)
    @ApiModelProperty("区编码")
    private String countyNo;

    @RelateTo(OrderAddress.class)
    @ApiModelProperty("区名称")
    private String countyName;

    @RelateTo(OrderAddress.class)
    @ApiModelProperty("详细地址")
    private String street;

    @RelateTo(OrderAddress.class)
    @ApiModelProperty("收货人姓名")
    private String receiverName;

    @RelateTo(OrderAddress.class)
    @ApiModelProperty("收货手机号")
    private String receiverMobi;

    @RelateTo(OrderItem.class)
    @ApiModelProperty("订单行列表")
    private List<OrderItem> orderItems;

    @JoinIgnore({IgnoreWhen.Update})
    @RelateTo(value = OrderItem.class, field = "id")
    @ApiModelProperty("订单行id列表")
    private List<Long> orderItemIds;

    @RelateTo(OrderAddress.class)
    @ApiModelProperty("收货地址信息")
    private OrderAddress address;

}
