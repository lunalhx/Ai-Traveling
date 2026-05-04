package com.example.travel.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CheckinTodayVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long spotId;

    private Boolean checkedIn;

    private LocalDateTime checkinTime;
}
