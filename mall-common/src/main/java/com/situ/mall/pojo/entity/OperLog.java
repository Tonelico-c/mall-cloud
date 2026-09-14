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
 * 操作日志记录
 * </p>
 *
 * @author Gao
 * @since 2026-09-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OperLog implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模块标题
     */
    private String module;

    /**
     * 日志类型
     */
    @TableField("log_type")
    private String logType;

    /**
     * 操作人员id
     */
    @TableField("admin_id")
    private Long adminId;

    /**
     * 操作人员
     */
    @TableField("admin_name")
    private String adminName;

    /**
     * 请求方式GET/POST
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求URI
     */
    @TableField("request_uri")
    private String requestUri;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 返回参数
     */
    @TableField("response_params")
    private String responseParams;

    /**
     * 主机地址
     */
    @TableField("request_ip")
    private String requestIp;

    /**
     * 请求服务器地址
     */
    @TableField("server_address")
    private String serverAddress;

    /**
     * 是否异常 1是异常，0不是
     */
    private Integer exception;

    /**
     * 异常信息
     */
    @TableField("exception_msg")
    private String exceptionMsg;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private Date endTime;

    /**
     * 执行时间
     */
    @TableField("execute_time")
    private Integer executeTime;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 操作系统
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 浏览器名称
     */
    @TableField("browser_name")
    private String browserName;

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
