package com.situ.mall.mapper;

import com.situ.mall.pojo.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.situ.mall.pojo.vo.OrderVO;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Gao
 * @since 2026-09-17
 */
public interface OrderMapper extends BaseMapper<Order> {

    List<OrderVO> listItem(Long userId);
}
