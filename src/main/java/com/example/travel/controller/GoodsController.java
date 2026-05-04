package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.service.GoodsService;
import com.example.travel.vo.GoodsVO;
import com.example.travel.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @GetMapping("/goods/list")
    public Result<PageResult<GoodsVO>> listGoods(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(goodsService.listGoods(page, pageSize));
    }
}
