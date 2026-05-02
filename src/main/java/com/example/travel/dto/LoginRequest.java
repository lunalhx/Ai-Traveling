package com.example.travel.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "phone cannot be blank")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "phone format is invalid")
    private String phone;

    @NotBlank(message = "code cannot be blank")
    private String code;
}
