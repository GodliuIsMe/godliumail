package com.liu.mallware.dao;

import com.liu.mallware.entity.UndoLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 07:46:05
 */
@Mapper
public interface UndoLogDao extends BaseMapper<UndoLogEntity> {
	
}
