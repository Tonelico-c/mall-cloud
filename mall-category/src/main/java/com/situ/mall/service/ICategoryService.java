package com.situ.mall.service;

import com.situ.mall.pojo.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.mall.pojo.vo.CategoryVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-09
 */
public interface ICategoryService extends IService<Category> {

    List<CategoryVO> selectCategoryTree();
}
