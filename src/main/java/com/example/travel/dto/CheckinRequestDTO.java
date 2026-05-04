package com.example.travel.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckinRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "spotId cannot be null")
    private Long spotId;

    @NotNull(message = "longitude cannot be null")
    private BigDecimal longitude;

    @NotNull(message = "latitude cannot be null")
    private BigDecimal latitude;
}
