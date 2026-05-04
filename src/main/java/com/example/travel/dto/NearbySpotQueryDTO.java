package com.example.travel.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class NearbySpotQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer radius = 3000;

    private Integer limit = 10;

    private Long categoryId;
}
