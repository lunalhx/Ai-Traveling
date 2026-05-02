package com.example.travel.interceptor;

import com.example.travel.constant.RedisConstants;
import com.example.travel.dto.UserDTO;
import com.example.travel.utils.UserHolder;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "authorization";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(token)) {
            return true;
        }

        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);
        if (CollectionUtils.isEmpty(userMap)) {
            return true;
        }

        UserDTO userDTO = toUserDTO(userMap);
        UserHolder.saveUser(userDTO);
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.removeUser();
    }

    private UserDTO toUserDTO(Map<Object, Object> userMap) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(parseLong(userMap.get("id")));
        userDTO.setPhone(toString(userMap.get("phone")));
        userDTO.setNickname(toString(userMap.get("nickname")));
        userDTO.setIcon(toString(userMap.get("icon")));
        return userDTO;
    }

    private Long parseLong(Object value) {
        String text = toString(value);
        return StringUtils.hasText(text) ? Long.valueOf(text) : null;
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
