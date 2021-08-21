package com.liu.mallproduct.controller;

import com.liu.common.utils.PageUtils;
import com.liu.common.utils.R;
import com.liu.mallproduct.entity.AttrEntity;
import com.liu.mallproduct.entity.AttrGroupEntity;
import com.liu.mallproduct.service.AttrAttrgroupRelationService;
import com.liu.mallproduct.service.AttrGroupService;
import com.liu.mallproduct.service.AttrService;
import com.liu.mallproduct.service.CategoryService;
import com.liu.mallproduct.vo.AttrGroupRelationVo;
import com.liu.mallproduct.vo.AttrGroupWithAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 06:59:09
 */
@RestController
@RequestMapping("mallproduct/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /*
     * 获取当前分类下所有分组 和  关联属性
     * /product/attrgroup/{catelogId}/withattr
     */
    @GetMapping("{catelogId}/withattr")
    public R getAllAttr(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrVo> attrVo = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",attrVo);
    }

    /*
    属性于分组的关系 添加
     */
    @PostMapping("/attr/relation")
    public R saveAttrRelation(@RequestBody List<AttrGroupRelationVo> relationVos){
        relationService.saveAttrRelations(relationVos);
        return R.ok();
    }



    /*
    删除属性于分组的关联
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] relationVos){
        attrService.deleteRelation(relationVos);
        return R.ok();
    }

    /*
    获取同分类下 未被分组的属性
     */
    @GetMapping("{attrgroupId}/noattr/relation")
    public R attrNoRelation( @PathVariable("attrgroupId")Long attrgroupId,
                            @RequestParam Map<String, Object> params){
        PageUtils page = attrService.getNoRelationAttr(attrgroupId,params);
        return R.ok().put("page",page);
    }
    /*
    获取属性与 分组的关联
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> entityList = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data",entityList);
    }
    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
		Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCateLogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
