package com.liu.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.utils.PageUtils;
import com.liu.common.utils.Query;
import com.liu.mallproduct.dao.SkuInfoDao;
import com.liu.mallproduct.entity.SkuInfoEntity;
import com.liu.mallproduct.service.SkuInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        //key 模糊查询
        String key=(String)params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and(item ->{
                item.eq("sku_id",key).or().like("sku_name",key);
            });
        }
        //目录id
        String catelogId=(String)params.get("catelogId");
        if(StringUtils.isNotEmpty(catelogId) && ! "0".equalsIgnoreCase(catelogId)){
            queryWrapper.eq("catalog_id",catelogId);
        }

        //品牌
        String brandId=(String)params.get("brandId");
        if(StringUtils.isNotEmpty(brandId) && ! "0".equalsIgnoreCase(brandId)){
            queryWrapper.eq("brand_id",brandId);
        }

        //最小值
        String min=(String)params.get("min");
        if(StringUtils.isNotEmpty(min)){
            queryWrapper.ge("price",min);
        }
        //z最大值
        String max=(String)params.get("max");
        if(StringUtils.isNotEmpty(max)){

            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if( bigDecimal.compareTo(new BigDecimal("0")) == 1){
                    queryWrapper.le("price",max);
                }
            } catch (Exception e) {

            }
        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),queryWrapper
        );

        return new PageUtils(page);
    }


}