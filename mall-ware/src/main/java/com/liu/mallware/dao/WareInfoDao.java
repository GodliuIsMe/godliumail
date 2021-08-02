package com.liu.mallware.dao;

import com.liu.mallware.entity.WareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库信息
 * 
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 07:46:05
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {
	
}
