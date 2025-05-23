package io.github.pengxianggui.crud.demo.domain.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("order_item")
@ApiModel(value = "OrderItem对象", description = "订单行")
public class OrderItem {

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("标准零售价单价")
    private Long price;

    @ApiModelProperty("数量")
    private Integer quantity;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("品牌代码")
    private String brandCode;

    @ApiModelProperty("商品图片")
    private String image;

    @ApiModelProperty("是否赠品")
    private Boolean gift;

    @ApiModelProperty("创建人")
    private Long createBy;

    @ApiModelProperty("创建人姓名")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人")
    private Long updateBy;

    @ApiModelProperty("更新人姓名")
    private String updateName;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("是否删除 0 否 1 是")
    private Boolean deleted;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("商品sku(手动维护)")
    private String sku;

    @ApiModelProperty("商品分类code")
    private String classes;

    @ApiModelProperty("商品分类名称冗余")
    private String classesName;

    @ApiModelProperty("尺寸")
    private String size;

    @ApiModelProperty("已锁数量，售后完成后增加")
    private Byte lockedQuantity;

    @ApiModelProperty("父商品代码(自动生成)")
    private String parentCode;

    @ApiModelProperty("结构")
    private String structure;

    @ApiModelProperty("硬度")
    private String solidity;

    @ApiModelProperty("硬度左")
    private String solidityLeft;

    @ApiModelProperty("硬度右")
    private String solidityRight;

    @ApiModelProperty("电商平台SKU")
    private String onlineSku;

    @ApiModelProperty("电商商品名称")
    private String onlineProduct;

    @ApiModelProperty("履约类型  SHOP-门店履约 CENTER-总部履约")
    private String performType;

    @ApiModelProperty("活动价单价(结算价)")
    private Long promotionPrice;

    @ApiModelProperty("优惠金额小计")
    private Long reduceAmount;

    @ApiModelProperty("优惠金额单价")
    private Long reducePrice;

    @ApiModelProperty("成交价小计（扣减优惠）")
    private Long actualAmount;

    @ApiModelProperty("成交价单价（扣减优惠）")
    private Long actualPrice;

    @ApiModelProperty("经销商-提货价单价")
    private Long supplyPrice;

    @ApiModelProperty("商品id")
    private Long productId;

    @ApiModelProperty("颜色")
    private String color;

    @ApiModelProperty("活动名称")
    private String promotionName;

    @ApiModelProperty("加盟商返利金额")
    private Long rebateAmount;

    @ApiModelProperty("是否定制")
    private Boolean customize;

    @ApiModelProperty("定制物料json")
    private String customizeMaterials;

    @ApiModelProperty("是否使用返利")
    private Boolean rebate;

    @ApiModelProperty("定制长度")
    private Integer customLength;

    @ApiModelProperty("定制宽度")
    private Integer customWidth;

    @ApiModelProperty("其他定制需求")
    private String customRequire;

    @ApiModelProperty("活动价单价(参考价)")
    private Long promotionBasePrice;

    @ApiModelProperty("赠品分类")
    private String giftType;

    @ApiModelProperty("sku物料json")
    private String skuMaterials;

    @ApiModelProperty("活动id")
    private Long promotionId;

    @ApiModelProperty("商品单价（销售手填）")
    private Long saleActualPrice;

    @ApiModelProperty("补贴金额小计")
    private Long subsidyAmount;

    @ApiModelProperty("补贴金额单价")
    private Long subsidyPrice;

    @ApiModelProperty("客户应付金额-单价（扣减优惠和补贴）")
    private Long cusPayPrice;

    @ApiModelProperty("客户应付金额-小计（扣减优惠和补贴）")
    private Long cusPayAmount;

    @ApiModelProperty("商品单价-小计（销售手填）")
    private Long saleActualAmount;

    @ApiModelProperty("是否使用京东返利池")
    private Boolean jdRebate;
}
