package com.example.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.travel.dto.SeckillOrderMessageDTO;
import com.example.travel.entity.GoodsOrder;
import com.example.travel.vo.GoodsOrderVO;
import com.example.travel.vo.PageResult;
import com.example.travel.vo.SeckillResultVO;

public interface GoodsOrderService extends IService<GoodsOrder> {

    SeckillResultVO createOrderMysql(Long activityId);

    SeckillResultVO createOrderLuaSync(Long activityId);

    SeckillResultVO createOrderAsync(Long activityId);

    void createOrderFromMessage(SeckillOrderMessageDTO messageDTO);

    SeckillResultVO queryOrderResult(Long activityId);

    PageResult<GoodsOrderVO> queryMyOrders(Integer page, Integer pageSize);
}
