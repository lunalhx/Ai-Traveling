package com.example.travel.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeckillActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long goodsId;

    private String goodsName;

    private String coverUrl;

    private String activityName;

    private BigDecimal seckillPrice;

    private Integer totalStock;

    private Integer stock;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;
}
