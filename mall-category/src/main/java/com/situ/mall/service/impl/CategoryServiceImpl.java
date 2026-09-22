package com.situ.mall.service.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.situ.mall.mapper.CategoryMapper;
import com.situ.mall.pojo.entity.Category;
import com.situ.mall.pojo.vo.CategoryVO;
import com.situ.mall.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 商品分类服务实现类
 * </p>
 * 主要提供商品分类树形结构的查询功能，
 * 并结合 Redis 缓存来减少数据库的重复查询压力。
 *
 * @author Gao
 * @since 2026-09-09
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    /**
     * 分类树缓存的 key。
     * 读、写、失效三处必须统一引用这个常量：
     * 之前读用的是 "categoryTree"、写用的是 "categoryVOTree"，两处不一致导致缓存永远不命中，
     * 而且每次请求都往 categoryVOTree 这个 list 里追加数据，list 会无限增长。
     * 抽成常量之后就不会再出现两处写法不一致的问题。
     */
    private static final String CATEGORY_TREE_KEY = "categoryTree";

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询商品分类的树形结构
     * <p>
     * 执行流程：
     * 1. 优先从 Redis 缓存中读取分类树（key: CATEGORY_TREE_KEY），命中则直接返回，避免访问数据库；
     * 2. 若缓存未命中（为空），则查询数据库中全部分类（按 sort 字段升序排列），
     *    将实体 Category 转换为 CategoryVO 后构建树形结构；
     * 3. 将构建好的分类树写入 Redis 缓存（key: CATEGORY_TREE_KEY），供下次查询使用。
     *
     * @return 分类树形结构列表，只包含顶级分类（parentId = 0），子分类挂载在 children 属性中
     */
    @Override
    public List<CategoryVO> selectCategoryTree() {
        // 先从Redis中获取分类树（range(0, -1) 表示取出该 list 中的所有元素）
        List<CategoryVO> categoryVOTree = redisTemplate.opsForList().range(CATEGORY_TREE_KEY, 0, -1);
        // 缓存未命中，回源数据库查询并重建分类树
        if (CollectionUtils.isEmpty(categoryVOTree)) {
            // 构造查询条件：按 sort 字段升序，保证分类展示顺序正确
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(Category::getSort);
            List<Category> categoryList = categoryMapper.selectList(queryWrapper);
            // 将 Category 实体转换为 CategoryVO 视图对象
            List<CategoryVO> categoryVOList = categoryList.stream().map(category -> {
                CategoryVO categoryVO = new CategoryVO();
                BeanUtils.copyProperties(category, categoryVO);
                return categoryVO;
            }).toList();

            // 将平铺的分类列表组装成树形结构
            categoryVOTree = buildTree(categoryVOList);

            // 将分类树写入 Redis 缓存，下次查询可直接命中
            redisTemplate.opsForList().rightPushAll(CATEGORY_TREE_KEY, categoryVOTree);
            // 加一个过期时间兜底：万一某次改库没走到失效逻辑，缓存也能自己恢复，不会长期脏读
            redisTemplate.expire(CATEGORY_TREE_KEY, 1, TimeUnit.HOURS);
        }

        return categoryVOTree;
    }

    @Override
    public void add(Category category) {
        categoryMapper.insert(category);
        // 分类树变了，删掉缓存，下次查询时重建
        redisTemplate.delete(CATEGORY_TREE_KEY);
    }

    /**
     * 修改分类
     * <p>
     * 缓存里存的是整棵树，一次修改可能影响层级或排序，所以不做增量更新，
     * 直接删掉缓存让它下次查询时整体重建。
     */
    @Override
    public void update(Category category) {
        updateById(category);
        redisTemplate.delete(CATEGORY_TREE_KEY);
    }

    /**
     * 删除分类（逻辑删除），同样要失效分类树缓存
     */
    @Override
    public void deleteById(Long id) {
        removeById(id);
        redisTemplate.delete(CATEGORY_TREE_KEY);
    }

    /**
     * 构建分类树的第一层：筛选出所有顶级分类并为它们递归装配子分类
     * <p>
     * 约定 parentId = 0 的分类为顶级（根）分类。
     * 对每个顶级分类，调用 buildChildrenTree 递归查找其子分类并挂载到 children 属性上。
     *
     * @param categoryVOList 平铺的全部分类列表（未组装层级关系）
     * @return 树形结构列表，每个元素为一棵以顶级分类为根的分类树
     */
    public List<CategoryVO> buildTree(List<CategoryVO> categoryVOList) {
        return categoryVOList.stream()
                // 顶级分类的 parentId 为 0，作为每棵树的根节点
                .filter(categoryVO -> categoryVO.getParentId() == 0)
                .map(categoryVO -> {
                    // 递归装配当前顶级分类下的所有子孙分类
                    categoryVO.setChildren(buildChildrenTree(categoryVO, categoryVOList));
                    return categoryVO;
                }).toList();
    }

    /**
     * 递归构建指定分类的子分类树
     * <p>
     * 从全量分类列表中筛选出 parentId 等于当前分类 id 的直接子分类，
     * 再对每个子分类递归调用自身，继续向下查找，直到没有更深的子分类为止（叶子节点得到空列表）。
     *
     * @param parent         当前待装配子分类的父分类节点
     * @param categoryVOList 平铺的全部分类列表
     * @return 当前分类的直接子分类列表，每个子分类的 children 中又包含其自身的子分类
     */
    public List<CategoryVO> buildChildrenTree(CategoryVO parent, List<CategoryVO> categoryVOList) {
        return categoryVOList.stream()
                // 筛选 parentId 等于父分类 id 的分类，即当前分类的直接子分类
                .filter(categoryVO -> categoryVO.getParentId().equals(parent.getId()))
                .map(categoryVO -> {
                    // 递归向下装配，处理更深层的子分类
                    categoryVO.setChildren(buildChildrenTree(categoryVO, categoryVOList));
                    return categoryVO;
                }).toList();
    }


}
