package com.example.travel.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SpotDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long categoryId;

    private String categoryName;

    private String name;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String coverUrl;

    private String images;

    private String brief;

    private String detail;

    private String openTime;

    private BigDecimal ticketPrice;

    private Integer heatScore;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
