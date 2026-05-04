package com.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.entity.Goods;
import com.example.travel.mapper.GoodsMapper;
import com.example.travel.service.GoodsService;
import com.example.travel.vo.GoodsVO;
import com.example.travel.vo.PageResult;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private static final int ENABLED_STATUS = 1;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    @Override
    public PageResult<GoodsVO> listGoods(Integer page, Integer pageSize) {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        Long total = lambdaQuery()
                .eq(Goods::getStatus, ENABLED_STATUS)
                .count();
        if (total == 0) {
            return new PageResult<>();
        }
        long offset = (long) (safePage - 1) * safePageSize;
        List<GoodsVO> records = lambdaQuery()
                .eq(Goods::getStatus, ENABLED_STATUS)
                .orderByDesc(Goods::getCreateTime)
                .last("LIMIT " + offset + ", " + safePageSize)
                .list()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(total, records);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private GoodsVO toVO(Goods goods) {
        GoodsVO vo = new GoodsVO();
        vo.setId(goods.getId());
        vo.setName(goods.getName());
        vo.setCoverUrl(goods.getCoverUrl());
        vo.setDescription(goods.getDescription());
        vo.setOriginalPrice(goods.getOriginalPrice());
        vo.setStatus(goods.getStatus());
        return vo;
    }
}
