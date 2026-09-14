package com.situ.mall.controller;


import com.situ.mall.utils.LoginContext;
import com.situ.mall.utils.Result;
import com.situ.mall.pojo.entity.Category;
import com.situ.mall.pojo.vo.CategoryVO;
import com.situ.mall.service.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-09
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private ICategoryService categoryService;
    @GetMapping("/name/{id}")
    String getCategoryNameById(@PathVariable Long id){
        Long id1 = (Long) LoginContext.getLoginInfo().get("id");
        log.info("CategoryController selectNameById id1: {}", id1);

        return categoryService.getById(id).getName();
    }

    /**
     * 分类树形结构
     * GET /category/tree
     */
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        List<CategoryVO> list = categoryService.selectCategoryTree();
        return Result.ok(list);
    }

    /**
     * 根据ID查询分类
     * GET /category/1
     */
    @GetMapping("/{id}")
    public Result<Category> selectById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return Result.ok(category);
    }

    /**
     * 新增分类
     * POST /category
     */
    @PostMapping
    public Result add(@RequestBody Category category) {
        categoryService.add(category);
        return Result.ok("添加成功");
    }

    /**
     * 修改分类
     * PUT /category/1
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateById(category);
        return Result.ok("更新成功");
    }

    /**
     * 根据ID删除分类（逻辑删除）
     * DELETE /category/1
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.ok("删除成功");
    }
}

