package com.liu.mallware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.constant.WareConstant;
import com.liu.common.utils.PageUtils;
import com.liu.common.utils.Query;
import com.liu.mallware.dao.PurchaseDao;
import com.liu.mallware.entity.PurchaseDetailEntity;
import com.liu.mallware.entity.PurchaseEntity;
import com.liu.mallware.service.PurchaseDetailService;
import com.liu.mallware.service.PurchaseService;
import com.liu.mallware.service.WareSkuService;
import com.liu.mallware.vo.MergeVo;
import com.liu.mallware.vo.PurchaseFinishItem;
import com.liu.mallware.vo.PurchaseFinishVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status",0).or().eq("status",1);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /*
     * 合并采购需求
     */
    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {

        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        } else {
            PurchaseEntity purchaseEntity = this.baseMapper.selectById(mergeVo.getPurchaseId());
            boolean flage=(purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()) ||
                    (purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
            if(!flage){
                return;
            }
        }
        Long finalPurchaseId =purchaseId;
        List<Long> items = mergeVo.getItems();
        List<PurchaseDetailEntity> collect = items.stream().map(sku -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(sku);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /*
     *1、判断 采购单的状态 ，  是否为 新建 或者 已分配状态
     * 2、 改变这个采购单的状态
     * 3、改变采购项的状态
     */
    @Transactional
    @Override
    public void received(List<Long> ids) {
        // 没有采购需求直接返回，否则会破坏采购单
        if (ids == null || ids.size() == 0) {
            return;
        }
        //1、首先 将ids转为  具体 采购单实体 从数据库查出
        List<PurchaseEntity> purchaseEntities = this.listByIds(ids);
        //2、判断状态，并修改状态
        List<PurchaseEntity> purchaseList = purchaseEntities.stream()
                .filter(item -> {
                    return item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()
                            || item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode();
                }).map(item -> {
                    item.setUpdateTime(new Date());
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
                    return item;
                }).collect(Collectors.toList());

        //3、更新状态
        this.updateBatchById(purchaseList);

        //4、获取采购项的状态 并改变
        //4.1 获取采购单的id
        Stream<Long> purchaseIds = purchaseList.stream().map(item -> {
            return item.getId();
        });
        //4.2 获取所有采购需求
        QueryWrapper<PurchaseDetailEntity> purchase_ids = new QueryWrapper<PurchaseDetailEntity>().in("purchase_id", purchaseIds);
        List<PurchaseDetailEntity> purchaseDetailEntityList = purchaseDetailService.list(purchase_ids);
        //4.3 更改状态
        purchaseDetailEntityList = purchaseDetailEntityList.stream().map(purchaseDetailEntity -> {
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(purchaseDetailEntityList);


    }
    /*
     * 1、改变采购单状态
     * 2、改变采购项状态
     */
    @Transactional
    @Override
    public void done(PurchaseFinishVo doneVo) {
        //1、改变采购单状态   如果 有一个采购项  没完成 ，则采购单状态
        Long id = doneVo.getId();
        Boolean flag = true;
        List<PurchaseFinishItem> items = doneVo.getItems();
        ArrayList<PurchaseDetailEntity> updates = new ArrayList<>();

        double price;
        double p = 0;
        double sum = 0;

        // 2.改变采购项状态
        for (PurchaseFinishItem item : items) {
            //设置采购项内容
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASEERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                // 3.将成功采购的进行入库
                // 查出当前采购项的详细信息
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                // skuId、到那个仓库、sku名字,
                price = wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
                if (price != p) {
                    p = entity.getSkuNum() * price;
                }
                detailEntity.setSkuPrice(new BigDecimal(p));
                sum += p;
            }
            // 设置采购成功的id
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
    }

}