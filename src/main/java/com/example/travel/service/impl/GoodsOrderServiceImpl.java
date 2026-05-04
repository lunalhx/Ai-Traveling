package com.example.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.common.BusinessException;
import com.example.travel.common.ErrorCode;
import com.example.travel.constant.MqConstants;
import com.example.travel.constant.RedisConstants;
import com.example.travel.dto.SeckillOrderMessageDTO;
import com.example.travel.dto.UserDTO;
import com.example.travel.entity.Goods;
import com.example.travel.entity.GoodsOrder;
import com.example.travel.entity.SeckillActivity;
import com.example.travel.mapper.GoodsMapper;
import com.example.travel.mapper.GoodsOrderMapper;
import com.example.travel.mapper.SeckillActivityMapper;
import com.example.travel.service.GoodsOrderService;
import com.example.travel.utils.UserHolder;
import com.example.travel.vo.GoodsOrderVO;
import com.example.travel.vo.PageResult;
import com.example.travel.vo.SeckillResultVO;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsOrderServiceImpl extends ServiceImpl<GoodsOrderMapper, GoodsOrder> implements GoodsOrderService {

    private static final int ENABLED_STATUS = 1;
    private static final int ORDER_STATUS_CREATED = 1;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final SeckillActivityMapper seckillActivityMapper;

    private final GoodsMapper goodsMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final DefaultRedisScript<Long> seckillScript;

    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillResultVO createOrderMysql(Long activityId) {
        Long userId = getCurrentUserId();
        SeckillActivity activity = getActivity(activityId);
        SeckillResultVO unavailable = checkActivityUnavailable(activity);
        if (unavailable != null) {
            return unavailable;
        }
        if (hasOrder(userId, activityId)) {
            return repeatResult();
        }
        return createOrderInCurrentTransaction(buildOrderMessage(userId, activity), "下单成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillResultVO createOrderLuaSync(Long activityId) {
        Long userId = getCurrentUserId();
        SeckillActivity activity = getActivity(activityId);
        SeckillResultVO unavailable = checkActivityUnavailable(activity);
        if (unavailable != null) {
            return unavailable;
        }
        SeckillResultVO luaResult = executeSeckillLua(activityId, userId);
        if (!Boolean.TRUE.equals(luaResult.getAccepted())) {
            return luaResult;
        }
        return createOrderInCurrentTransaction(buildOrderMessage(userId, activity), "下单成功");
    }

    @Override
    public SeckillResultVO createOrderAsync(Long activityId) {
        Long userId = getCurrentUserId();
        SeckillActivity activity = getActivity(activityId);
        SeckillResultVO unavailable = checkActivityUnavailable(activity);
        if (unavailable != null) {
            return unavailable;
        }
        SeckillResultVO luaResult = executeSeckillLua(activityId, userId);
        if (!Boolean.TRUE.equals(luaResult.getAccepted())) {
            return luaResult;
        }

        SeckillOrderMessageDTO messageDTO = buildOrderMessage(userId, activity);
        try {
            rabbitTemplate.convertAndSend(
                    MqConstants.SECKILL_EXCHANGE,
                    MqConstants.SECKILL_ORDER_ROUTING_KEY,
                    messageDTO
            );
        } catch (Exception exception) {
            log.error("send seckill order message failed, userId={}, activityId={}", userId, activityId, exception);
            return result(false, null, "系统繁忙，请稍后重试", "BUSY");
        }
        return result(true, messageDTO.getOrderId(), "抢购请求已受理", "PROCESSING");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrderFromMessage(SeckillOrderMessageDTO messageDTO) {
        if (messageDTO == null || messageDTO.getUserId() == null || messageDTO.getActivityId() == null) {
            log.warn("invalid seckill order message, message={}", messageDTO);
            return;
        }
        if (hasOrder(messageDTO.getUserId(), messageDTO.getActivityId())) {
            return;
        }
        int updated = deductStock(messageDTO.getActivityId());
        if (updated == 0) {
            log.warn("mysql seckill stock not enough, userId={}, activityId={}",
                    messageDTO.getUserId(), messageDTO.getActivityId());
            return;
        }
        try {
            save(buildOrder(messageDTO));
        } catch (DuplicateKeyException exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.info("duplicate seckill order message ignored, userId={}, activityId={}",
                    messageDTO.getUserId(), messageDTO.getActivityId());
        }
    }

    @Override
    public SeckillResultVO queryOrderResult(Long activityId) {
        Long userId = getCurrentUserId();
        if (activityId == null || activityId < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "活动 id 必须大于 0");
        }
        GoodsOrder order = lambdaQuery()
                .eq(GoodsOrder::getUserId, userId)
                .eq(GoodsOrder::getActivityId, activityId)
                .one();
        if (order == null) {
            return result(false, null, "订单处理中或未抢购成功", "PROCESSING");
        }
        return result(true, order.getId(), "已生成订单", "SUCCESS");
    }

    @Override
    public PageResult<GoodsOrderVO> queryMyOrders(Integer page, Integer pageSize) {
        Long userId = getCurrentUserId();
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        Long total = lambdaQuery()
                .eq(GoodsOrder::getUserId, userId)
                .count();
        if (total == 0) {
            return new PageResult<>();
        }
        long offset = (long) (safePage - 1) * safePageSize;
        List<GoodsOrder> orders = lambdaQuery()
                .eq(GoodsOrder::getUserId, userId)
                .orderByDesc(GoodsOrder::getCreateTime)
                .last("LIMIT " + offset + ", " + safePageSize)
                .list();
        return new PageResult<>(total, toOrderVOs(orders));
    }

    private Long getCurrentUserId() {
        UserDTO user = UserHolder.getUser();
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        return user.getId();
    }

    private SeckillActivity getActivity(Long activityId) {
        if (activityId == null || activityId < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "活动 id 必须大于 0");
        }
        return seckillActivityMapper.selectById(activityId);
    }

    private SeckillResultVO checkActivityUnavailable(SeckillActivity activity) {
        if (activity == null || !Integer.valueOf(ENABLED_STATUS).equals(activity.getStatus())) {
            return result(false, null, "抢购活动不存在", "NOT_FOUND");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            return result(false, null, "活动未开始", "NOT_STARTED");
        }
        if (now.isAfter(activity.getEndTime())) {
            return result(false, null, "活动已结束", "ENDED");
        }
        return null;
    }

    private boolean hasOrder(Long userId, Long activityId) {
        Long count = lambdaQuery()
                .eq(GoodsOrder::getUserId, userId)
                .eq(GoodsOrder::getActivityId, activityId)
                .count();
        return count != null && count > 0;
    }

    private SeckillResultVO executeSeckillLua(Long activityId, Long userId) {
        String stockKey = RedisConstants.SECKILL_STOCK_KEY + activityId;
        String orderKey = RedisConstants.SECKILL_ORDER_KEY + activityId;
        Long code = stringRedisTemplate.execute(
                seckillScript,
                Arrays.asList(stockKey, orderKey),
                userId.toString()
        );
        if (Long.valueOf(0L).equals(code)) {
            return result(true, null, "Redis 预扣成功", "SUCCESS");
        }
        if (Long.valueOf(2L).equals(code)) {
            return repeatResult();
        }
        if (Long.valueOf(3L).equals(code)) {
            return result(false, null, "活动库存未初始化", "STOCK_NOT_LOADED");
        }
        return noStockResult();
    }

    private SeckillResultVO createOrderInCurrentTransaction(SeckillOrderMessageDTO messageDTO, String message) {
        int updated = deductStock(messageDTO.getActivityId());
        if (updated == 0) {
            return noStockResult();
        }
        try {
            GoodsOrder order = buildOrder(messageDTO);
            save(order);
            return result(true, order.getId(), message, "SUCCESS");
        } catch (DuplicateKeyException exception) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return repeatResult();
        }
    }

    private int deductStock(Long activityId) {
        return seckillActivityMapper.update(null, new LambdaUpdateWrapper<SeckillActivity>()
                .eq(SeckillActivity::getId, activityId)
                .gt(SeckillActivity::getStock, 0)
                .setSql("stock = stock - 1"));
    }

    private SeckillOrderMessageDTO buildOrderMessage(Long userId, SeckillActivity activity) {
        Long orderId = IdWorker.getId();
        SeckillOrderMessageDTO messageDTO = new SeckillOrderMessageDTO();
        messageDTO.setOrderId(orderId);
        messageDTO.setOrderNo("SO" + orderId);
        messageDTO.setUserId(userId);
        messageDTO.setActivityId(activity.getId());
        messageDTO.setGoodsId(activity.getGoodsId());
        messageDTO.setAmount(activity.getSeckillPrice());
        messageDTO.setCreateTime(LocalDateTime.now());
        return messageDTO;
    }

    private GoodsOrder buildOrder(SeckillOrderMessageDTO messageDTO) {
        GoodsOrder order = new GoodsOrder();
        order.setId(messageDTO.getOrderId());
        order.setOrderNo(messageDTO.getOrderNo());
        order.setUserId(messageDTO.getUserId());
        order.setActivityId(messageDTO.getActivityId());
        order.setGoodsId(messageDTO.getGoodsId());
        order.setQuantity(1);
        order.setAmount(messageDTO.getAmount());
        order.setStatus(ORDER_STATUS_CREATED);
        return order;
    }

    private SeckillResultVO noStockResult() {
        return result(false, null, "库存不足", "NO_STOCK");
    }

    private SeckillResultVO repeatResult() {
        return result(false, null, "重复下单", "REPEAT");
    }

    private SeckillResultVO result(Boolean accepted, Long orderId, String message, String status) {
        SeckillResultVO vo = new SeckillResultVO();
        vo.setAccepted(accepted);
        vo.setOrderId(orderId);
        vo.setMessage(message);
        vo.setStatus(status);
        return vo;
    }

    private List<GoodsOrderVO> toOrderVOs(List<GoodsOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> goodsIds = orders.stream()
                .map(GoodsOrder::getGoodsId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Goods> goodsMap = goodsMapper.selectList(new LambdaQueryWrapper<Goods>().in(Goods::getId, goodsIds))
                .stream()
                .collect(Collectors.toMap(Goods::getId, goods -> goods, (left, right) -> left));
        return orders.stream()
                .map(order -> toOrderVO(order, goodsMap.get(order.getGoodsId())))
                .collect(Collectors.toList());
    }

    private GoodsOrderVO toOrderVO(GoodsOrder order, Goods goods) {
        GoodsOrderVO vo = new GoodsOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setActivityId(order.getActivityId());
        vo.setGoodsId(order.getGoodsId());
        if (goods != null) {
            vo.setGoodsName(goods.getName());
            vo.setCoverUrl(goods.getCoverUrl());
        }
        vo.setQuantity(order.getQuantity());
        vo.setAmount(order.getAmount());
        vo.setStatus(order.getStatus());
        vo.setCreateTime(order.getCreateTime());
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
