package com.liu.mallware.dao;

import com.liu.mallware.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 07:46:05
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}
