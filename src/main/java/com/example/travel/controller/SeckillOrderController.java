package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.service.GoodsOrderService;
import com.example.travel.vo.SeckillResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeckillOrderController {

    private final GoodsOrderService goodsOrderService;

    @PostMapping("/seckill/order/mysql/{activityId}")
    public Result<SeckillResultVO> createOrderMysql(@PathVariable Long activityId) {
        return Result.success(goodsOrderService.createOrderMysql(activityId));
    }

    @PostMapping("/seckill/order/lua-sync/{activityId}")
    public Result<SeckillResultVO> createOrderLuaSync(@PathVariable Long activityId) {
        return Result.success(goodsOrderService.createOrderLuaSync(activityId));
    }

    @PostMapping("/seckill/order/{activityId}")
    public Result<SeckillResultVO> createOrderAsync(@PathVariable Long activityId) {
        return Result.success(goodsOrderService.createOrderAsync(activityId));
    }

    @GetMapping("/seckill/order/result/{activityId}")
    public Result<SeckillResultVO> queryOrderResult(@PathVariable Long activityId) {
        return Result.success(goodsOrderService.queryOrderResult(activityId));
    }
}
