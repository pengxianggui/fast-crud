package io.github.pengxianggui.crud.demo.domain.order;

import io.github.pengxianggui.crud.demo.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@ApiModel(value = "Orders对象", description = "订单表")
public class Orders extends BaseEntity {
    /**
     * 验证静态类对dto解析的影响
     */
    private static final String staticField = "staticField";

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("履约类型 1 门店履约 2 总部履约")
    private String performType;

    @ApiModelProperty("sap订单号")
    private String sapNo;

    @ApiModelProperty("备注")
    private String remark;

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

    @ApiModelProperty("销售员工编号")
    private String salesmanCode;

    @ApiModelProperty("销售员姓名")
    private String salesmanName;

    @ApiModelProperty("销售员电话")
    private String salesmanMobi;

    @ApiModelProperty("客户姓名")
    private String clientName;

    @ApiModelProperty("客户手机号")
    private String clientMobi;

    @ApiModelProperty("订货日期")
    private LocalDateTime orderDate;

    @ApiModelProperty("期望安装时间(原预期发货日期)")
    private LocalDateTime expectSendDate;

    @ApiModelProperty("发货时间")
    private LocalDateTime sendDate;

    @ApiModelProperty("预约安装时间")
    private LocalDateTime expectInstallDate;

    @ApiModelProperty("实际安装时间")
    private LocalDateTime installDate;

    @ApiModelProperty("数量")
    private Integer count;

    @ApiModelProperty("标准零售价")
    private Long totalAmount;

    @ApiModelProperty("优惠金额")
    private Long reduceAmount;

    @ApiModelProperty("成交价（公司侧价格,扣减优惠）计算公式: actual_amount=sale_actual_amount-reduce_amount")
    private Long actualAmount;

    @ApiModelProperty("客户已付金额")
    private Long payedAmount;

    @ApiModelProperty("上次支付时间")
    private LocalDateTime lastPayedDate;

    @ApiModelProperty("模糊检索关键字")
    private String keyword;

    @ApiModelProperty("第三方订单类型冗余")
    private String thirdType;

    @ApiModelProperty("第三方订单标记冗余")
    private String thirdTag;

    @ApiModelProperty("第三方文件")
    private String thirdFiles;

    @ApiModelProperty("商品概要")
    private String outline;

    @ApiModelProperty("是否申请特价")
    private Boolean specialPrice;

    @ApiModelProperty("订单主流程状态")
    private Integer status;

    @ApiModelProperty("物流单号")
    private String expressNo;

    @ApiModelProperty("sap最新一次同步状态")
    private String sapStatus;

    @ApiModelProperty("快递公司名称")
    private String expressCompany;

    @ApiModelProperty("采购单号")
    private String purchaseNo;

    @ApiModelProperty("交货单号")
    private String deliverNo;

    @ApiModelProperty("销售主体id")
    private Long subjectId;

    @ApiModelProperty("客服备注")
    private String csRemark;

    @ApiModelProperty("买家留言")
    private String buyerMsg;

    @ApiModelProperty("卖家留言")
    private String sellerMsg;

    @ApiModelProperty("销售主体名称")
    private String subjectName;

    @ApiModelProperty("sap销售单金额")
    private Long sapAmount;

    @ApiModelProperty("签名图片")
    private String signature;

    @ApiModelProperty("活动价")
    private Long promotionAmount;

    @ApiModelProperty("客户-财务确认金额")
    private Long confirmedAmount;

    @ApiModelProperty("经销商应付")
    private Long supplyAmount;

    @ApiModelProperty("经销商已付")
    private Long supplyPayedAmount;

    @ApiModelProperty("经销商-财务确认金额")
    private Long supplyConfirmedAmount;

    @ApiModelProperty("上次支付时间")
    private LocalDateTime lastSupplyPayedDate;

    @ApiModelProperty("订单状态（新）")
    private String orderStatus;

    @ApiModelProperty("用户支付状态")
    private String payStatus;

    @ApiModelProperty("经销商支付状态")
    private String supplyPayStatus;

    @ApiModelProperty("订单需发物料总计")
    private Integer materialNeedCount;

    @ApiModelProperty("订单已发物料总计")
    private Integer materialSentCount;

    @ApiModelProperty("加盟商返利池支付金额")
    private Long rebateAmount;

    @ApiModelProperty("金蝶订单号")
    private String kingDeeNo;

    @ApiModelProperty("销售网络id")
    private Long netId;

    @ApiModelProperty("差价")
    private Long diffAmount;

    @ApiModelProperty("完成时间")
    private LocalDateTime completeTime;

    @ApiModelProperty("直营-订单凭证图")
    private String voucherFile;

    @ApiModelProperty("尊享服务标签")
    private String serviceLabel;

    @ApiModelProperty("驳回原因")
    private String rejectReason;

    @ApiModelProperty("客户应付金额（客户侧价格，扣减优惠+补贴）计算公式: cus_pay_amount=sale_actual_amount-reduce_amount-subsidy_amount")
    private Long cusPayAmount;

    @ApiModelProperty("补贴金额")
    private Long subsidyAmount;

    @ApiModelProperty("商品总价（销售手填）")
    private Long saleActualAmount;

    @ApiModelProperty("签署状态(已签署:SIGNED;待签署:TO_SIGNED;免签：VISA_FREE)")
    private String signStatus;

    @ApiModelProperty("订单驳回次数（第几次驳回）")
    private Integer rejectCount;

    @ApiModelProperty("返利类型")
    private String rebateType;
}
