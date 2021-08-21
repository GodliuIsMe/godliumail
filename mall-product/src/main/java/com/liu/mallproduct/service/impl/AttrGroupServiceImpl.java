package com.liu.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.utils.PageUtils;
import com.liu.common.utils.Query;
import com.liu.mallproduct.dao.AttrGroupDao;
import com.liu.mallproduct.entity.AttrEntity;
import com.liu.mallproduct.entity.AttrGroupEntity;
import com.liu.mallproduct.service.AttrGroupService;
import com.liu.mallproduct.service.AttrService;
import com.liu.mallproduct.vo.AttrGroupWithAttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key =(String) params.get("key");
        //select * from attr_group where catelogId =?
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();

        if(!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        };

        if (catelogId==0){
            IPage<AttrGroupEntity> page = this.page(
                    //从map中提取各个值 如 分页信息 ，多少页 ，第几页
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        }else {
            wrapper.eq("catelog_id",catelogId);
            IPage<AttrGroupEntity> page = this.page(
                    //从map中提取各个值 如 分页信息 ，多少页 ，第几页
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );

            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> group_ids = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrVo> collect = group_ids.stream().map((item) -> {
            AttrGroupWithAttrVo attrVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(item, attrVo);
            List<AttrEntity> relationAttr = attrService.getRelationAttr(item.getAttrGroupId());
            attrVo.setAttrs(relationAttr);
            return attrVo;
        }).collect(Collectors.toList());
        return collect;
    }

}