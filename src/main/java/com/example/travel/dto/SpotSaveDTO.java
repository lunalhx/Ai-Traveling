package com.example.travel.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SpotSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "categoryId cannot be null")
    private Long categoryId;

    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotBlank(message = "address cannot be blank")
    private String address;

    @NotNull(message = "longitude cannot be null")
    private BigDecimal longitude;

    @NotNull(message = "latitude cannot be null")
    private BigDecimal latitude;

    private String coverUrl;

    private String images;

    private String brief;

    private String detail;

    private String openTime;

    @NotNull(message = "ticketPrice cannot be null")
    @DecimalMin(value = "0.00", message = "ticketPrice must be greater than or equal to 0")
    private BigDecimal ticketPrice;

    private Integer status;
}
