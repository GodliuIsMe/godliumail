package com.liu.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.constant.ProductConstant;
import com.liu.common.utils.PageUtils;
import com.liu.common.utils.Query;
import com.liu.mallproduct.dao.AttrAttrgroupRelationDao;
import com.liu.mallproduct.dao.AttrDao;
import com.liu.mallproduct.dao.AttrGroupDao;
import com.liu.mallproduct.dao.CategoryDao;
import com.liu.mallproduct.entity.*;
import com.liu.mallproduct.service.AttrService;
import com.liu.mallproduct.service.CategoryService;
import com.liu.mallproduct.vo.AttrGroupRelationVo;
import com.liu.mallproduct.vo.AttrResponseVo;
import com.liu.mallproduct.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrAttrgroupRelationDao attrgroupRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
//        System.out.println(attr);
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.save(attrEntity);
        //?????? ??????  ??? ?????????????????????
        if (attr.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() &&attr.getAttrGroupId()!=null ) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId,String type) {
        //??????i??????????????? ????????????
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type","base".equalsIgnoreCase(type)
                        ?ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if(catelogId!=0){
            wrapper.eq("catelog_id",catelogId);
        }

        String key = (String)params.get("key");
        //????????????
        if(!StringUtils.isEmpty(key)){
            wrapper.and((wrapper1)->{
                wrapper1.eq("attr_id",key).or().eq("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> responseVos = records.stream().map((attrEntity) -> {

            AttrResponseVo responseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity,responseVo);
            //???????????? ??? ???????????????    ???????????? ??????????????????
            if("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity relationEntity = attrgroupRelationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>()
                                .eq("attr_id", responseVo.getAttrId()));
                if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    responseVo.setGroupName(attrGroupEntity.getAttrGroupName());

                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if(categoryEntity!=null){
                responseVo.setCatelogName(categoryEntity.getName());
            }
            return responseVo;
        }).collect(Collectors.toList());
        pageUtils.setList(responseVos);
//        System.out.println(responseVos);
        return pageUtils;
    }

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrEntity byId = this.getById(attrId);
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        BeanUtils.copyProperties(byId, attrResponseVo);

        AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", byId.getAttrId()));

        if(ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode() == byId.getAttrType()){
            //1. ??????????????????
            if (null != relationEntity) {
                Long attrGroupId = relationEntity.getAttrGroupId();
                attrResponseVo.setAttrGroupId(attrGroupId);
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                if (null != attrGroupEntity) {
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }


        //2. ??????????????????
        Long catelogId = attrResponseVo.getCatelogId();
        Long[] catelogPath = categoryService.findCateLogPath(catelogId);
        attrResponseVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (null != categoryEntity) {
            attrResponseVo.setCatelogName(categoryEntity.getName());
        }
        return attrResponseVo;
    }

    @Override
    @Transactional
    public void updateAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);

        if(ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode() == attrEntity.getAttrType()){
            //??????????????????
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrVo.getAttrId());
            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            UpdateWrapper<AttrAttrgroupRelationEntity> updateWrapper = new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId());

            Integer count = relationDao.selectCount(updateWrapper);
            if( count > 0){
                relationDao.update(relationEntity,updateWrapper);
            }else {
                relationDao.insert(relationEntity);
            }
        }
    }
    /*
     * ?????? ??????id  ????????????id
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {

        List<AttrAttrgroupRelationEntity> attr_relations = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        List<Long> attrids = attr_relations.stream().map((attrRelation) -> {
            return attrRelation.getAttrId();
        }).collect(Collectors.toList());
        if(attrids==null || attrids.size()==0){
            return null;
        }
        List<AttrEntity> attrEntities = this.listByIds(attrids);

        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] relationVos) {
//        relationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",relationVos).eq("attr_id",))
   //??????????????????
        List<AttrAttrgroupRelationEntity> collect = Arrays.asList(relationVos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(collect);
    }

    /**
     * ?????????????????????????????????????????????
     * @param attrgroupId
     * @param params
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {
        //1??????????????????????????????????????????????????????????????????(??????????????????)
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        //2?????????????????????????????????????????????
        //2.1 ????????????????????????????????????
        //2.2 ???????????????????????????
        //2.3 ????????????????????????????????????????????????
        List<AttrGroupEntity> attrgroups = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId)).
                stream().collect(Collectors.toList());
        //????????????id
        List<Long> groupIds = attrgroups.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //??????id????????????,??????????????? ids
        List<AttrAttrgroupRelationEntity> attrEntities = attrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        List<Long> attrIds = attrEntities.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        //???????????????????????? ????????????  ????????? ????????????
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        
        if(null !=attrIds && attrIds.size()>0){
            wrapper.notIn("attr_id",attrIds);
        }

        String key = (String)params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w ->{
                w.eq("attr_id",key).or().like("attr_name",key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),wrapper);

        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

}