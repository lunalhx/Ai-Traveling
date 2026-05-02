package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.LoginFormDTO;
import com.example.travel.dto.UserDTO;
import com.example.travel.service.UserService;
import com.example.travel.utils.UserHolder;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/code")
    public Result<String> sendCode(@RequestParam String phone) {
        return userService.sendCode(phone);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginFormDTO loginForm) {
        return userService.login(loginForm);
    }

    @GetMapping("/me")
    public Result<UserDTO> me() {
        return Result.success(UserHolder.getUser());
    }
}
