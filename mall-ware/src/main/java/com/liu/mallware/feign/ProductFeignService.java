package com.liu.mallware.feign;

import com.liu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-product")
public interface ProductFeignService {
    //1\发给网关
    //2、直接发给后台服务
    @GetMapping("/mallproduct/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

}
