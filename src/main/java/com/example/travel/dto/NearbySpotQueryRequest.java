package com.example.travel.dto;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;

@Data
public class NearbySpotQueryRequest {

    @NotNull(message = "longitude cannot be null")
    @DecimalMin(value = "-180.0", message = "longitude must be greater than or equal to -180")
    @DecimalMax(value = "180.0", message = "longitude must be less than or equal to 180")
    private BigDecimal longitude;

    @NotNull(message = "latitude cannot be null")
    @DecimalMin(value = "-90.0", message = "latitude must be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "latitude must be less than or equal to 90")
    private BigDecimal latitude;

    @Positive(message = "radiusMeters must be positive")
    private Integer radiusMeters;
}
