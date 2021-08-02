package com.liu.mallmember.Feign;

import com.liu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


@FeignClient("mall-coupon")  //告诉springcloud 这是远程客户端
public interface CouponFeignService {
    @GetMapping("/mallcoupon/coupon/member/list")
    public R membercoupons();
}
