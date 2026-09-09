package com.situ.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.situ.mall.mapper.CategoryMapper;
import com.situ.mall.pojo.entity.Category;
import com.situ.mall.pojo.vo.CategoryVO;
import com.situ.mall.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-09
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> selectCategoryTree() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        List<Category> categoryList = categoryMapper.selectList(queryWrapper);

        List<CategoryVO> categoryVOList = categoryList.stream().map(category -> {
            CategoryVO categoryVO = new CategoryVO();
            BeanUtils.copyProperties(category, categoryVO);
            return categoryVO;
        }).toList();

        List<CategoryVO> categoryTree = buildTree(categoryVOList);
        return categoryTree;
    }
    public List<CategoryVO> buildTree(List<CategoryVO> categoryVOList) {
        return categoryVOList.stream()
                .filter(categoryVO -> categoryVO.getParentId() == 0)
                .map(categoryVO -> {
                    categoryVO.setChildren(buildChildrenTree(categoryVO, categoryVOList));
                    return categoryVO;
                }).toList();
    }
    public List<CategoryVO> buildChildrenTree(CategoryVO parent,List<CategoryVO> categoryVOList){
        return categoryVOList.stream()
                .filter(categoryVO -> categoryVO.getParentId().equals(parent.getId()))
                .map(categoryVO -> {
                    categoryVO.setChildren(buildChildrenTree(categoryVO, categoryVOList));
                    return categoryVO;
                }).toList();
    }


}
