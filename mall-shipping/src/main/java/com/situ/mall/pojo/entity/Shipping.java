package com.situ.mall.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author Gao
 * @since 2026-09-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Shipping implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 收货姓名
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 收货固定电话
     */
    @TableField("receiver_phone")
    private String receiverPhone;

    /**
     * 收货移动电话
     */
    @TableField("receiver_mobile")
    private String receiverMobile;

    /**
     * 省份
     */
    @TableField("receiver_province")
    private String receiverProvince;

    /**
     * 城市
     */
    @TableField("receiver_city")
    private String receiverCity;

    /**
     * 区/县
     */
    @TableField("receiver_district")
    private String receiverDistrict;

    /**
     * 详细地址
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 邮编
     */
    @TableField("receiver_zip")
    private String receiverZip;

    /**
     * （1：默认 0：非默认）
     */
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 逻辑删除 1 表示删除，0 表示未删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
