package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.service.GoodsOrderService;
import com.example.travel.vo.GoodsOrderVO;
import com.example.travel.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GoodsOrderController {

    private final GoodsOrderService goodsOrderService;

    @GetMapping("/order/list")
    public Result<PageResult<GoodsOrderVO>> queryMyOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(goodsOrderService.queryMyOrders(page, pageSize));
    }
}
