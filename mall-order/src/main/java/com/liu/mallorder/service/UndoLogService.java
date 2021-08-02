package com.liu.mallorder.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.common.utils.PageUtils;
import com.liu.mallorder.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 07:42:36
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

