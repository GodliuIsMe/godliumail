package com.liu.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.utils.PageUtils;
import com.liu.common.utils.Query;
import com.liu.mallproduct.dao.CategoryDao;
import com.liu.mallproduct.entity.CategoryEntity;
import com.liu.mallproduct.service.CategoryBrandRelationService;
import com.liu.mallproduct.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查询说有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2、组成父子的树形结构

        //2.1 找到所有一级分类
        List<CategoryEntity> level1Munus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid()==0)
                .map((menu)->{
                    menu.setChildren(getChildrens(menu,entities));
                    return menu;
                }).sorted((menu1,menu2)->{
                    return (menu1.getSort()==0?0:menu1.getSort())-(menu2.getSort()==0?0:menu2.getSort());
                })
                .collect(Collectors.toList());


        return level1Munus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单，是否被别的引用

        //逻辑删除 通过 修改数据库中 的某一标志位 来表示是否删除

        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCateLogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> paths2 = findParentPath(catelogId,paths);
        return (Long[]) paths2.toArray(new Long[paths2.size()]);
    }

    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        relationService.updateCategory(category.getCatId(),category.getName());
    }

    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        paths.add(0,catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        //root 当前菜单，  all 所有菜单
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
            //找到子菜单
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
            //菜单排序
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == 0 ? 0 : menu1.getSort()) - (menu2.getSort() == 0 ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}