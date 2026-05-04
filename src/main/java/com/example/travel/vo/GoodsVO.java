package com.example.travel.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class GoodsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String coverUrl;

    private String description;

    private BigDecimal originalPrice;

    private Integer status;
}
