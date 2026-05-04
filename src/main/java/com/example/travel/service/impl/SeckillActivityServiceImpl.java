package com.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.common.BusinessException;
import com.example.travel.common.ErrorCode;
import com.example.travel.constant.RedisConstants;
import com.example.travel.entity.Goods;
import com.example.travel.entity.SeckillActivity;
import com.example.travel.mapper.GoodsMapper;
import com.example.travel.mapper.SeckillActivityMapper;
import com.example.travel.service.SeckillActivityService;
import com.example.travel.vo.PageResult;
import com.example.travel.vo.SeckillActivityVO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity>
        implements SeckillActivityService {

    private static final int ENABLED_STATUS = 1;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final GoodsMapper goodsMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResult<SeckillActivityVO> listActivities(Integer page, Integer pageSize) {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        Long total = lambdaQuery().count();
        if (total == 0) {
            return new PageResult<>();
        }
        long offset = (long) (safePage - 1) * safePageSize;
        List<SeckillActivity> activities = lambdaQuery()
                .orderByDesc(SeckillActivity::getStartTime)
                .last("LIMIT " + offset + ", " + safePageSize)
                .list();
        return new PageResult<>(total, toVOs(activities));
    }

    @Override
    public SeckillActivityVO getActivityDetail(Long activityId) {
        if (activityId == null || activityId < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "活动 id 必须大于 0");
        }
        SeckillActivity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "抢购活动不存在");
        }
        Goods goods = goodsMapper.selectById(activity.getGoodsId());
        return toVO(activity, goods);
    }

    @Override
    public Integer loadActivityStock(Long activityId) {
        if (activityId == null || activityId < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "活动 id 必须大于 0");
        }
        SeckillActivity activity = getById(activityId);
        if (activity == null || !Integer.valueOf(ENABLED_STATUS).equals(activity.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "抢购活动不存在");
        }
        String stockKey = RedisConstants.SECKILL_STOCK_KEY + activityId;
        String orderKey = RedisConstants.SECKILL_ORDER_KEY + activityId;
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(activity.getStock()));
        stringRedisTemplate.delete(orderKey);
        return activity.getStock();
    }

    private List<SeckillActivityVO> toVOs(List<SeckillActivity> activities) {
        if (activities.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Long> goodsIds = activities.stream()
                .map(SeckillActivity::getGoodsId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Goods> goodsMap = goodsMapper.selectBatchIds(goodsIds)
                .stream()
                .collect(Collectors.toMap(Goods::getId, goods -> goods, (left, right) -> left));
        return activities.stream()
                .map(activity -> toVO(activity, goodsMap.get(activity.getGoodsId())))
                .collect(Collectors.toList());
    }

    private SeckillActivityVO toVO(SeckillActivity activity, Goods goods) {
        SeckillActivityVO vo = new SeckillActivityVO();
        vo.setId(activity.getId());
        vo.setGoodsId(activity.getGoodsId());
        if (goods != null) {
            vo.setGoodsName(goods.getName());
            vo.setCoverUrl(goods.getCoverUrl());
        }
        vo.setActivityName(activity.getActivityName());
        vo.setSeckillPrice(activity.getSeckillPrice());
        vo.setTotalStock(activity.getTotalStock());
        vo.setStock(activity.getStock());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());
        return vo;
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
}
