package com.example.travel.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillOrderRequest {

    @NotNull(message = "activityId cannot be null")
    private Long activityId;
}
