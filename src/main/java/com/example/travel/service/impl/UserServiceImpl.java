package com.example.travel.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.common.ErrorCode;
import com.example.travel.common.Result;
import com.example.travel.constant.RedisConstants;
import com.example.travel.dto.LoginFormDTO;
import com.example.travel.dto.UserDTO;
import com.example.travel.entity.User;
import com.example.travel.mapper.UserMapper;
import com.example.travel.service.UserService;
import com.example.travel.utils.RegexUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String USER_NICKNAME_PREFIX = "user_";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Result<String> sendCode(String phone) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "phone format is invalid");
        }

        String code = RandomUtil.randomNumbers(6);
        String key = RedisConstants.LOGIN_CODE_KEY + phone;
        stringRedisTemplate.opsForValue().set(key, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        log.info("login code for {} is {}", phone, code);
        return Result.success(code);
    }

    @Override
    public Result<String> login(LoginFormDTO loginForm) {
        if (loginForm == null || RegexUtils.isPhoneInvalid(loginForm.getPhone())) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "phone format is invalid");
        }
        if (!StringUtils.hasText(loginForm.getCode())) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "code cannot be blank");
        }

        String phone = loginForm.getPhone();
        String codeKey = RedisConstants.LOGIN_CODE_KEY + phone;
        String cacheCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (!StringUtils.hasText(cacheCode) || !cacheCode.equals(loginForm.getCode())) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "verification code is invalid");
        }

        User user = lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            user = createUserWithPhone(phone);
        }

        String token = UUID.randomUUID().toString(true);
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, toUserMap(user));
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        stringRedisTemplate.delete(codeKey);

        return Result.success(token);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname(USER_NICKNAME_PREFIX + RandomUtil.randomString(8));
        try {
            save(user);
            return user;
        } catch (DuplicateKeyException exception) {
            return lambdaQuery().eq(User::getPhone, phone).one();
        }
    }

    private Map<String, String> toUserMap(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setPhone(user.getPhone());
        userDTO.setNickname(user.getNickname());
        userDTO.setIcon(user.getIcon());

        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", String.valueOf(userDTO.getId()));
        userMap.put("phone", userDTO.getPhone());
        userMap.put("nickname", userDTO.getNickname());
        if (StringUtils.hasText(userDTO.getIcon())) {
            userMap.put("icon", userDTO.getIcon());
        }
        return userMap;
    }
}
