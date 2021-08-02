package com.liu.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.common.utils.PageUtils;
import com.liu.mallproduct.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author liujiaxin
 * @email liujiaxin@gmail.com
 * @date 2021-07-31 06:59:09
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

