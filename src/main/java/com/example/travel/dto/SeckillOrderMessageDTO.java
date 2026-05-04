package com.example.travel.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeckillOrderMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private Long activityId;

    private Long goodsId;

    private BigDecimal amount;

    private LocalDateTime createTime;
}
