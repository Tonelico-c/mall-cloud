package com.situ.mall.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.mall.annotation.MyLog;
import com.situ.mall.utils.Result;
import com.situ.mall.pojo.entity.Product;
import com.situ.mall.pojo.query.ProductQuery;
import com.situ.mall.pojo.vo.ProductVO;
import com.situ.mall.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Set;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-08
 */
@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private IProductService productService;

    @GetMapping
    public Result<IPage<ProductVO>> list(ProductQuery productQuery){
        IPage<ProductVO> page = productService.list(productQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<Product> selectById(@PathVariable Long id){
        Product product = productService.selectById(id);
        return Result.ok(product);
    }
    // 获取所有商品图片
    @GetMapping("/selectAllImage")
    Set<String> selectAllImage(){
        return productService.selectAllImage();
    }

    @PostMapping
    public Result add(@RequestBody Product product){
        productService.save(product);
        return Result.ok("添加成功");
    }
    @MyLog(module = "修改商品")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Product product){
        product.setId(id);
        productService.update(product);
        return Result.ok("修改成功");
    }
    @MyLog(module = "删除商品")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id){
        productService.deleteById(id);
        return Result.ok("删除成功");
    }
    @MyLog(module = "删除商品")
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        productService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}

