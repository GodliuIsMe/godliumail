package com.liu.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.common.utils.PageUtils;
import com.liu.mallproduct.entity.SkuSaleAttrValueEntity;

import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 06:59:09
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

