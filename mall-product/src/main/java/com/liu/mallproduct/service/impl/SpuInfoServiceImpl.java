package com.liu.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.to.SkuReductionTo;
import com.liu.common.to.SpuBoundTo;
import com.liu.common.utils.PageUtils;
import com.liu.common.utils.Query;
import com.liu.common.utils.R;
import com.liu.mallproduct.dao.SpuInfoDao;
import com.liu.mallproduct.entity.*;
import com.liu.mallproduct.feign.CouponFeignService;
import com.liu.mallproduct.service.*;
import com.liu.mallproduct.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 1.保存spu基本信息 pms_sku_info
        // 插入后id自动返回注入
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo,spuInfoEntity);

        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        // 此处有分布式id的问题，所以要加事务

        // 2.保存spu的表述图片  pms_spu_info_desc
        List<String> descript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        //id不是自增
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",descript));

        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);



        // 3.保存spu的图片集  pms_spu_images
        List<String> images = spuSaveVo.getImages();
        SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);


        // 先获取所有图片
        // 保存图片的时候 并且保存这个是那个spu的图片

        // 4.保存spu的规格属性  pms_product_attr_value
        List<BaseAttrs>  attrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = attrs.stream().map((item) -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(item.getAttrId());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            productAttrValueEntity.setAttrValue(item.getAttrValues());
            productAttrValueEntity.setQuickShow(item.getShowDesc());

            AttrEntity byId = attrService.getById(item.getAttrId());
            productAttrValueEntity.setAttrName(byId.getAttrName());

            return productAttrValueEntity;
        }).collect(Collectors.toList());

        productAttrValueService.saveBatch(collect);


        // 1).spu的积分信息 sms_spu_bounds（跨库）
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r1 = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r1.getCode()!=0){
            log.error("远程保存spu积分信息失败");
        }


        // 5.保存当前spu对应所有sku信息

        List<Skus> skus = spuSaveVo.getSkus();
        if(skus!=null && skus.size()>0){
            for (Skus item : skus) {//保存默认图片需要
                String defaultImg = "";
                for (Images img : item.getImages()) {
                    if (img.getDefaultImg() == 1) {
                        defaultImg = img.getImgUrl();
                    }
                }

                //5.1 SKU的基本信息；pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                //保存默认图片
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();
                //5.2 SKU的图片信息；pms_spu_images
                List<SkuImagesEntity> skuImagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg().intValue());
                    return skuImagesEntity;
                }).filter(item2->{
                    return !StringUtils.isEmpty(item2.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                //5.3 SKU的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attrs2 = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs2.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //5.4 SKU的优惠，满减等信息；sms_sku_ladder；sms_sku_full_reduction；sms_member_price
                //需要调用远程服务，服务在注册中心中。 写个接口  ，要开启远程调用共嗯那个
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount()>0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1) {
                    R r = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r.getCode() != 0) {
                        log.error("远程保存spu积分信息失败");
                    }
                }
            }
        }



        // 2).基本信息的保存 pms_sku_info
        // skuName 、price、skuTitle、skuSubtitle 这些属性需要手动保存

        // 设置spu的品牌id

        // 3).保存sku的图片信息  pms_sku_images
        // sku保存完毕 自增主键就出来了 收集所有图片

        // 4).sku的销售属性  pms_sku_sale_attr_value
        // 5.) sku的优惠、满减、会员价格等信息  [跨库]
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        //根据key 模糊检索
        String key=(String)params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and(item -> {
                item.eq("id",key).or().like("spu_name",key);
            });
        }

        //上架
        String status=(String)params.get("status");
        if(!StringUtils.isEmpty(status)){
            queryWrapper.eq("publish_status",status);
        }

        String brandId=(String)params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && (!"0".equalsIgnoreCase(brandId))){
            queryWrapper.eq("brand_id",brandId);
        }

        String catelogId=(String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && (!"0".equalsIgnoreCase(catelogId))){
            queryWrapper.eq("catalog_id",catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }


    private void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

}