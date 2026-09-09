package com.situ.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.mall.pojo.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.mall.pojo.query.ProductQuery;
import com.situ.mall.pojo.vo.ProductVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-08
 */
public interface IProductService extends IService<Product> {

    IPage<ProductVO> list(ProductQuery productQuery);
}
