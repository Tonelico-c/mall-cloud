package com.situ.mall.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.mall.common.utils.Result;
import com.situ.mall.pojo.entity.Product;
import com.situ.mall.pojo.query.ProductQuery;
import com.situ.mall.service.IProductService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-08
 */
@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private IProductService productService;

    @GetMapping
    public Result<IPage<Product>> list(ProductQuery productQuery){
        IPage<Product> page = productService.list(productQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<Product> selectById(@PathVariable Long id){
        Product product = productService.getById(id);
        return Result.ok(product);
    }

    @PostMapping
    public Result add(@RequestBody Product product){
        productService.save(product);
        return Result.ok("添加成功");
    }
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Product product){
        product.setId(id);
        productService.updateById(product);
        return Result.ok("修改成功");
    }
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id){
        productService.removeById(id);
        return Result.ok("删除成功");
    }
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        productService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}

