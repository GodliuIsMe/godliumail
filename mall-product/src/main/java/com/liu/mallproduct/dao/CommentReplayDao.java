package com.liu.mallproduct.dao;

import com.liu.mallproduct.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 06:59:09
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
