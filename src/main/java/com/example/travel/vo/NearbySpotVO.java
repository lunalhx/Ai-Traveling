package com.example.travel.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class NearbySpotVO {

    private Long id;

    private String name;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Double distanceMeters;
}
