package com.example.travel.dto;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginFormDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "phone cannot be blank")
    private String phone;

    @NotBlank(message = "code cannot be blank")
    private String code;
}
