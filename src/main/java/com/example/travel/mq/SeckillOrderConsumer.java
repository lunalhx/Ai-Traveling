package com.example.travel.mq;

import com.example.travel.constant.MqConstants;
import com.example.travel.dto.SeckillOrderMessageDTO;
import com.example.travel.service.GoodsOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderConsumer {

    private final GoodsOrderService goodsOrderService;

    @RabbitListener(queues = MqConstants.SECKILL_ORDER_QUEUE)
    public void handleSeckillOrder(SeckillOrderMessageDTO messageDTO) {
        try {
            goodsOrderService.createOrderFromMessage(messageDTO);
        } catch (Exception exception) {
            log.error("consume seckill order message failed, message={}", messageDTO, exception);
            throw exception;
        }
    }
}
