package com.example.travel.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class NearbySpotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long categoryId;

    private String address;

    private String coverUrl;

    private String brief;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Double distance;

    private Integer heatScore;
}
