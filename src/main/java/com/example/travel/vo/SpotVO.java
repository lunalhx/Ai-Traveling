package com.example.travel.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SpotVO {

    private Long id;

    private Long categoryId;

    private String name;

    private String coverUrl;

    private String introduction;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal price;

    private BigDecimal score;
}
