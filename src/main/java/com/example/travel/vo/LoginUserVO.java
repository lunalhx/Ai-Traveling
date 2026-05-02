package com.example.travel.vo;

import lombok.Data;

@Data
public class LoginUserVO {

    private Long userId;

    private String phone;

    private String nickname;

    private String token;
}
