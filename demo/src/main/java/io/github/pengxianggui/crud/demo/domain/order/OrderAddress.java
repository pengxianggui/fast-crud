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
@TableName("order_address")
@ApiModel(value = "OrderAddress对象", description = "订单地址信息表")
public class OrderAddress {

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("省编码")
    private String provinceNo;

    @ApiModelProperty("省名称")
    private String provinceName;

    @ApiModelProperty("市编码")
    private String cityNo;

    @ApiModelProperty("市名称")
    private String cityName;

    @ApiModelProperty("区编码")
    private String countyNo;

    @ApiModelProperty("区名称")
    private String countyName;

    @ApiModelProperty("详细地址")
    private String street;

    @ApiModelProperty("收货人姓名")
    private String receiverName;

    @ApiModelProperty("收货手机号")
    private String receiverMobi;

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

    @ApiModelProperty("来源")
    private String type;

    @ApiModelProperty("街道")
    private String streetArea;
}
