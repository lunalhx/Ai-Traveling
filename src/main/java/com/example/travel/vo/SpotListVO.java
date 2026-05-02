package com.example.travel.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SpotListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long categoryId;

    private String name;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String coverUrl;

    private String brief;

    private String openTime;

    private BigDecimal ticketPrice;

    private Integer heatScore;
}
