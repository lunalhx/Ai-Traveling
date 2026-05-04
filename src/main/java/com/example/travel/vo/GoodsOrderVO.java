package com.example.travel.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GoodsOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long activityId;

    private Long goodsId;

    private String goodsName;

    private String coverUrl;

    private Integer quantity;

    private BigDecimal amount;

    private Integer status;

    private LocalDateTime createTime;
}
