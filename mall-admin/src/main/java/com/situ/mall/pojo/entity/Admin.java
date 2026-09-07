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
 * @since 2026-09-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Admin implements Serializable {


    /**
     * 用户表id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String name;

    /**
     * 用户密码，MD5加密
     */
    private String password;

    /**
     * 头像
     */
    private String avatar;

    private String email;

    private String phone;

    /**
     * 角色0-管理员,1-普通用户
     */
    private Integer role;

    /**
     * 状态（1：正常 0：停用）
     */
    private Integer status;

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
