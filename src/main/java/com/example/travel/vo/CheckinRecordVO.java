package com.example.travel.vo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CheckinRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long spotId;

    private String spotName;

    private String coverUrl;

    private LocalDate checkinDate;

    private Integer distanceMeter;

    private Integer rewardPoints;

    private LocalDateTime createTime;
}
