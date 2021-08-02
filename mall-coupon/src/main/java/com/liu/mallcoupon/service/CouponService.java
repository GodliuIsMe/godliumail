package com.liu.mallcoupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.common.utils.PageUtils;
import com.liu.mallcoupon.entity.CouponEntity;

import java.util.Map;

/**
 * 优惠券信息
 *
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 07:31:20
 */
public interface CouponService extends IService<CouponEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

