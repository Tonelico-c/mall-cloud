package com.situ.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.mall.pojo.entity.Product;
import com.situ.mall.mapper.ProductMapper;
import com.situ.mall.pojo.query.ProductQuery;
import com.situ.mall.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-08
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {
    @Autowired
    private ProductMapper productMapper;


    @Override
    public IPage<Product> list(ProductQuery productQuery) {
        IPage<Product> page = new Page<>(productQuery.getPage(), productQuery.getLimit());
        LambdaQueryWrapper<Product> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(productQuery.getName()), Product::getName, productQuery.getName())
                        .like(!ObjectUtils.isEmpty(productQuery.getSubtitle()), Product::getSubtitle, productQuery.getSubtitle());
        return productMapper.selectPage(page, lambdaQueryWrapper);
    }
}
