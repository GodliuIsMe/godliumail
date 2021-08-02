package com.liu.mallcoupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.common.utils.PageUtils;
import com.liu.mallcoupon.entity.SeckillPromotionEntity;

import java.util.Map;

/**
 * 秒杀活动
 *
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 07:31:20
 */
public interface SeckillPromotionService extends IService<SeckillPromotionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

