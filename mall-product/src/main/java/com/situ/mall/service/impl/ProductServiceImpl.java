package com.situ.mall.service.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.mall.api.category.CategoryClient;
import com.situ.mall.pojo.entity.Product;
import com.situ.mall.mapper.ProductMapper;
import com.situ.mall.pojo.query.ProductQuery;
import com.situ.mall.pojo.vo.ProductVO;
import com.situ.mall.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public IPage<ProductVO> list(ProductQuery productQuery) {

        IPage<Product> page = new Page<>(productQuery.getPage(), productQuery.getLimit());
        LambdaQueryWrapper<Product> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(productQuery.getName()), Product::getName, productQuery.getName())
                .like(!ObjectUtils.isEmpty(productQuery.getSubtitle()), Product::getSubtitle, productQuery.getSubtitle())
                .eq(!ObjectUtils.isEmpty(productQuery.getCategoryId()), Product::getCategoryId, productQuery.getCategoryId());
        productMapper.selectPage(page, lambdaQueryWrapper);
        List<Product> list = page.getRecords();
        List<ProductVO> productVOList  = list.stream().map(product->{
            ProductVO productVO = new ProductVO();
            BeanUtils.copyProperties(product, productVO);
            // 远程调用，获取分类名称
            String categoryName = categoryClient.getCategoryNameById(product.getCategoryId());
            productVO.setCategoryName(categoryName);
            return productVO;
        }).toList();

        IPage<ProductVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(productVOList);
        return voPage;
    }

    @Cacheable(value = "productCache", key = "#id")
    @Override
    public Product selectById(Long id) {
        return productMapper.selectById(id);
    }

    @Caching(evict = {
            @CacheEvict(value = "productCache", key = "#id")
    })
    @Override
    public void deleteById(Long id) {
        productMapper.deleteById(id);
    }

    @Caching(put = {
            @CachePut(value = "productCache", key = "#product.id")
    })
    @Override
    public void update(Product product) {
        productMapper.updateById(product);
    }

    @Override
    public Set<String> selectAllImage() {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("main_image");
        List<Product> list = productMapper.selectList(queryWrapper);
        Set<String> set = new HashSet<>();
        for (Product product : list) {
            set.add(product.getMainImage());
        }
        return set;
    }


    // @Override
    public Product selectById1(Long id) {
        Product product = (Product) redisTemplate.opsForValue().get("product:" + id);
        if(product == null){
            product = productMapper.selectById(id);
            if(product != null){
                redisTemplate.opsForValue().set("product:" + id, product, 1, TimeUnit.DAYS);
            }else {
                redisTemplate.opsForValue().set("product:" + id, new Product(), 1, TimeUnit.MINUTES);
            }

        }
        return product;
    }
}
