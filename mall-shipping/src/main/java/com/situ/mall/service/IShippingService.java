package com.situ.mall.service;

import com.situ.mall.pojo.entity.Shipping;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-16
 */
public interface IShippingService extends IService<Shipping> {

    List<Shipping> listUserShapping();
}
