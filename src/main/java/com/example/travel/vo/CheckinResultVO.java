package com.example.travel.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CheckinResultVO {

    private Long spotId;

    private Boolean checkedIn;

    private String message;

    private LocalDateTime checkinTime;
}
