package com.example.travel.vo;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CheckinResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long recordId;

    private Long spotId;

    private String spotName;

    private LocalDate checkinDate;

    private Integer distanceMeter;

    private Integer rewardPoints;

    private String message;
}
