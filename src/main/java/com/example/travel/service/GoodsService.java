package com.example.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.travel.entity.Goods;
import com.example.travel.vo.GoodsVO;
import com.example.travel.vo.PageResult;

public interface GoodsService extends IService<Goods> {

    PageResult<GoodsVO> listGoods(Integer page, Integer pageSize);
}
