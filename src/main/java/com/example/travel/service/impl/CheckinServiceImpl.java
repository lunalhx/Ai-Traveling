package com.example.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.common.BusinessException;
import com.example.travel.common.ErrorCode;
import com.example.travel.constant.RedisConstants;
import com.example.travel.dto.CheckinRequestDTO;
import com.example.travel.dto.UserDTO;
import com.example.travel.entity.CheckinRecord;
import com.example.travel.entity.CheckinRule;
import com.example.travel.entity.Spot;
import com.example.travel.mapper.CheckinRecordMapper;
import com.example.travel.mapper.CheckinRuleMapper;
import com.example.travel.mapper.SpotMapper;
import com.example.travel.service.CheckinService;
import com.example.travel.utils.GeoDistanceUtils;
import com.example.travel.utils.UserHolder;
import com.example.travel.vo.CheckinRecordVO;
import com.example.travel.vo.CheckinResultVO;
import com.example.travel.vo.CheckinTodayVO;
import com.example.travel.vo.PageResult;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl extends ServiceImpl<CheckinRecordMapper, CheckinRecord> implements CheckinService {

    private static final int ENABLED_STATUS = 1;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final DateTimeFormatter CHECKIN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(RedisConstants.CHECKIN_DATE_PATTERN);

    private final CheckinRuleMapper checkinRuleMapper;

    private final SpotMapper spotMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public CheckinResultVO checkin(CheckinRequestDTO requestDTO) {
        Long userId = getCurrentUserId();
        checkCheckinRequest(requestDTO);

        Spot spot = getEnabledSpot(requestDTO.getSpotId());
        CheckinRule rule = getEnabledRule(requestDTO.getSpotId());
        int distanceMeter = GeoDistanceUtils.calculateDistanceMeter(
                requestDTO.getLongitude(),
                requestDTO.getLatitude(),
                spot.getLongitude(),
                spot.getLatitude()
        );
        if (distanceMeter > rule.getRadiusMeter()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未进入打卡范围");
        }

        LocalDate today = LocalDate.now();
        String redisKey = buildUserCheckinKey(userId, today);
        String spotIdMember = requestDTO.getSpotId().toString();
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(redisKey, spotIdMember))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "今日已打卡");
        }

        CheckinRecord record = buildRecord(userId, requestDTO, today, distanceMeter, rule);
        try {
            save(record);
        } catch (DuplicateKeyException exception) {
            addRedisCheckin(redisKey, spotIdMember);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "今日已打卡");
        }
        addRedisCheckin(redisKey, spotIdMember);
        return toResultVO(record, spot.getName(), "打卡成功");
    }

    @Override
    public Object queryTodayStatus(Long spotId) {
        Long userId = getCurrentUserId();
        LocalDate today = LocalDate.now();
        if (spotId != null) {
            if (spotId < 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "景点 id 必须大于 0");
            }
            return queryTodayStatusBySpot(userId, spotId, today);
        }
        List<CheckinRecord> records = lambdaQuery()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinDate, today)
                .eq(CheckinRecord::getStatus, ENABLED_STATUS)
                .orderByDesc(CheckinRecord::getCreateTime)
                .list();
        return toRecordVOs(records);
    }

    @Override
    public PageResult<CheckinRecordVO> queryMyRecords(Integer page, Integer pageSize) {
        Long userId = getCurrentUserId();
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);

        Long total = lambdaQuery()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getStatus, ENABLED_STATUS)
                .count();
        if (total == 0) {
            return new PageResult<>();
        }

        long offset = (long) (safePage - 1) * safePageSize;
        List<CheckinRecord> records = lambdaQuery()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getStatus, ENABLED_STATUS)
                .orderByDesc(CheckinRecord::getCreateTime)
                .last("LIMIT " + offset + ", " + safePageSize)
                .list();
        return new PageResult<>(total, toRecordVOs(records));
    }

    private Long getCurrentUserId() {
        UserDTO user = UserHolder.getUser();
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        return user.getId();
    }

    private void checkCheckinRequest(CheckinRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "打卡参数不能为空");
        }
        if (requestDTO.getSpotId() == null || requestDTO.getSpotId() < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "景点 id 必须大于 0");
        }
        if (GeoDistanceUtils.isLongitudeInvalid(requestDTO.getLongitude())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "经度范围必须在 -180 到 180 之间");
        }
        if (GeoDistanceUtils.isLatitudeInvalid(requestDTO.getLatitude())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "纬度范围必须在 -90 到 90 之间");
        }
    }

    private Spot getEnabledSpot(Long spotId) {
        Spot spot = spotMapper.selectById(spotId);
        if (spot == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "景点不存在");
        }
        if (!Integer.valueOf(ENABLED_STATUS).equals(spot.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "景点已下架");
        }
        if (spot.getLongitude() == null || spot.getLatitude() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "景点坐标未配置");
        }
        return spot;
    }

    private CheckinRule getEnabledRule(Long spotId) {
        CheckinRule rule = checkinRuleMapper.selectOne(new LambdaQueryWrapper<CheckinRule>()
                .eq(CheckinRule::getSpotId, spotId)
                .eq(CheckinRule::getStatus, ENABLED_STATUS));
        if (rule == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该景点暂不支持打卡");
        }
        return rule;
    }

    private CheckinRecord buildRecord(
            Long userId,
            CheckinRequestDTO requestDTO,
            LocalDate today,
            Integer distanceMeter,
            CheckinRule rule
    ) {
        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setSpotId(requestDTO.getSpotId());
        record.setCheckinDate(today);
        record.setUserLongitude(requestDTO.getLongitude());
        record.setUserLatitude(requestDTO.getLatitude());
        record.setDistanceMeter(distanceMeter);
        record.setRewardPoints(rule.getRewardPoints());
        record.setStatus(ENABLED_STATUS);
        return record;
    }

    private CheckinTodayVO queryTodayStatusBySpot(Long userId, Long spotId, LocalDate today) {
        String redisKey = buildUserCheckinKey(userId, today);
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(redisKey, spotId.toString()))) {
            CheckinTodayVO vo = new CheckinTodayVO();
            vo.setSpotId(spotId);
            vo.setCheckedIn(true);
            return vo;
        }

        CheckinRecord record = lambdaQuery()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getSpotId, spotId)
                .eq(CheckinRecord::getCheckinDate, today)
                .eq(CheckinRecord::getStatus, ENABLED_STATUS)
                .one();
        CheckinTodayVO vo = new CheckinTodayVO();
        vo.setSpotId(spotId);
        vo.setCheckedIn(record != null);
        if (record != null) {
            vo.setCheckinTime(record.getCreateTime());
            addRedisCheckin(redisKey, spotId.toString());
        }
        return vo;
    }

    private String buildUserCheckinKey(Long userId, LocalDate date) {
        return RedisConstants.CHECKIN_USER_KEY + userId + ":" + date.format(CHECKIN_DATE_FORMATTER);
    }

    private void addRedisCheckin(String redisKey, String spotIdMember) {
        stringRedisTemplate.opsForSet().add(redisKey, spotIdMember);
        stringRedisTemplate.expire(redisKey, secondsUntilTomorrow(), TimeUnit.SECONDS);
    }

    private long secondsUntilTomorrow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowStart = now.toLocalDate().plusDays(1).atStartOfDay();
        return Math.max(Duration.between(now, tomorrowStart).getSeconds(), 1L);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private CheckinResultVO toResultVO(CheckinRecord record, String spotName, String message) {
        CheckinResultVO vo = new CheckinResultVO();
        vo.setRecordId(record.getId());
        vo.setSpotId(record.getSpotId());
        vo.setSpotName(spotName);
        vo.setCheckinDate(record.getCheckinDate());
        vo.setDistanceMeter(record.getDistanceMeter());
        vo.setRewardPoints(record.getRewardPoints());
        vo.setMessage(message);
        return vo;
    }

    private List<CheckinRecordVO> toRecordVOs(List<CheckinRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> spotIds = records.stream()
                .map(CheckinRecord::getSpotId)
                .distinct()
                .collect(Collectors.toList());
        List<Spot> spots = spotMapper.selectList(new LambdaQueryWrapper<Spot>().in(Spot::getId, spotIds));
        Map<Long, Spot> spotMap = spots.stream()
                .collect(Collectors.toMap(Spot::getId, spot -> spot, (left, right) -> left));

        List<CheckinRecordVO> vos = new ArrayList<>();
        for (CheckinRecord record : records) {
            vos.add(toRecordVO(record, spotMap.get(record.getSpotId())));
        }
        return vos;
    }

    private CheckinRecordVO toRecordVO(CheckinRecord record, Spot spot) {
        CheckinRecordVO vo = new CheckinRecordVO();
        vo.setId(record.getId());
        vo.setSpotId(record.getSpotId());
        if (spot != null) {
            vo.setSpotName(spot.getName());
            vo.setCoverUrl(spot.getCoverUrl());
        }
        vo.setCheckinDate(record.getCheckinDate());
        vo.setDistanceMeter(record.getDistanceMeter());
        vo.setRewardPoints(record.getRewardPoints());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}
