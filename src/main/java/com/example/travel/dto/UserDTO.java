package com.example.travel.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String phone;

    private String nickname;

    private String icon;
}
