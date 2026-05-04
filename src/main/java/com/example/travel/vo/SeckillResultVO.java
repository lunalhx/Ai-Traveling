package com.example.travel.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SeckillResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean accepted;

    private Long orderId;

    private String message;

    private String status;
}
