package com.liu.mallcoupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.to.MemberPrice;
import com.liu.common.to.SkuReductionTo;
import com.liu.common.utils.PageUtils;
import com.liu.common.utils.Query;
import com.liu.mallcoupon.dao.SkuFullReductionDao;
import com.liu.mallcoupon.entity.MemberPriceEntity;
import com.liu.mallcoupon.entity.SkuFullReductionEntity;
import com.liu.mallcoupon.entity.SkuLadderEntity;
import com.liu.mallcoupon.service.MemberPriceService;
import com.liu.mallcoupon.service.SkuFullReductionService;
import com.liu.mallcoupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //阶梯价格 ，满几件降价
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if(skuReductionTo.getFullCount()>0){
            skuLadderService.save(skuLadderEntity);
        }


        //满减优惠
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);
        if(skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1) {
            this.save(skuFullReductionEntity);
        }

        //会员价格
        List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();

        List<MemberPriceEntity> memberPriceEntities = memberPrices.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuLadderEntity.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(item->{
            return item.getMemberPrice().compareTo(BigDecimal.ZERO)==1;
        }).collect(Collectors.toList());

        memberPriceService.saveBatch(memberPriceEntities);

    }

}