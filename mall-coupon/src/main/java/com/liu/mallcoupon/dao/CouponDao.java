package com.liu.mallcoupon.dao;

import com.liu.mallcoupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 07:31:20
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
