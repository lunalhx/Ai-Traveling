package com.example.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.travel.common.Result;
import com.example.travel.dto.LoginFormDTO;
import com.example.travel.entity.User;

public interface UserService extends IService<User> {

    Result<String> sendCode(String phone);

    Result<String> login(LoginFormDTO loginForm);
}
