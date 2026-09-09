package com.situ.mall.pojo.vo;

import com.situ.mall.pojo.entity.Category;
import lombok.Data;

import java.util.List;

@Data
public class CategoryVO extends Category {
    private List<CategoryVO> children;
}
