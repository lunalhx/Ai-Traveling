package com.example.travel.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.travel.common.BusinessException;
import com.example.travel.common.ErrorCode;
import com.example.travel.constant.RedisConstants;
import com.example.travel.dto.SpotQueryDTO;
import com.example.travel.dto.SpotSaveDTO;
import com.example.travel.entity.Spot;
import com.example.travel.mapper.SpotMapper;
import com.example.travel.service.SpotService;
import com.example.travel.vo.PageResult;
import com.example.travel.vo.SpotDetailVO;
import com.example.travel.vo.SpotListVO;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotServiceImpl extends ServiceImpl<SpotMapper, Spot> implements SpotService {

    private static final int ENABLED_STATUS = 1;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final long LOCK_RETRY_SLEEP_MILLIS = 50L;
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) "
                    + "else return 0 end";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResult<SpotListVO> listSpots(SpotQueryDTO queryDTO) {
        SpotQueryDTO safeQuery = queryDTO == null ? new SpotQueryDTO() : queryDTO;
        int page = normalizePage(safeQuery.getPage());
        int pageSize = normalizePageSize(safeQuery.getPageSize());

        Long total = buildQuery(safeQuery).count();
        if (total == 0) {
            return new PageResult<>();
        }

        long offset = (long) (page - 1) * pageSize;
        List<SpotListVO> records = buildQuery(safeQuery)
                .orderByDesc(Spot::getHeatScore)
                .last("LIMIT " + offset + ", " + pageSize)
                .list()
                .stream()
                .map(this::toListVO)
                .collect(Collectors.toList());

        return new PageResult<>(total, records);
    }

    @Override
    public SpotDetailVO getSpotDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "景点 id 不能为空");
        }

        String cacheKey = RedisConstants.CACHE_SPOT_DETAIL_KEY + id;
        SpotDetailVO cacheDetail = querySpotDetailFromCache(cacheKey);
        if (cacheDetail != null) {
            return cacheDetail;
        }

        String lockKey = RedisConstants.LOCK_SPOT_DETAIL_KEY + id;
        String lockValue = UUID.randomUUID().toString(true);
        while (!tryLock(lockKey, lockValue)) {
            sleepBeforeRetry();
            cacheDetail = querySpotDetailFromCache(cacheKey);
            if (cacheDetail != null) {
                return cacheDetail;
            }
        }

        try {
            cacheDetail = querySpotDetailFromCache(cacheKey);
            if (cacheDetail != null) {
                return cacheDetail;
            }
            return rebuildSpotDetailCache(id, cacheKey);
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    @Override
    public List<SpotListVO> listHotSpots(Integer limit) {
        int safeLimit = normalizePageSize(limit);
        return lambdaQuery()
                .eq(Spot::getStatus, ENABLED_STATUS)
                .orderByDesc(Spot::getHeatScore)
                .last("LIMIT " + safeLimit)
                .list()
                .stream()
                .map(this::toListVO)
                .collect(Collectors.toList());
    }

    @Override
    public Long createSpot(SpotSaveDTO saveDTO) {
        checkSaveDTO(saveDTO);
        Spot spot = toEntity(saveDTO);
        if (spot.getStatus() == null) {
            spot.setStatus(ENABLED_STATUS);
        }
        save(spot);
        return spot.getId();
    }

    @Override
    public void updateSpot(Long id, SpotSaveDTO saveDTO) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "景点 id 不能为空");
        }
        checkSaveDTO(saveDTO);
        Spot spot = toEntity(saveDTO);
        spot.setId(id);
        boolean updated = updateById(spot);
        if (!updated) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "景点不存在");
        }
        deleteSpotDetailCache(id);
    }

    private LambdaQueryChainWrapper<Spot> buildQuery(SpotQueryDTO queryDTO) {
        LambdaQueryChainWrapper<Spot> query = lambdaQuery()
                .eq(Spot::getStatus, ENABLED_STATUS);
        if (queryDTO.getCategoryId() != null) {
            query.eq(Spot::getCategoryId, queryDTO.getCategoryId());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            query.like(Spot::getName, queryDTO.getKeyword());
        }
        return query;
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

    private long buildSpotDetailCacheTtl() {
        return RedisConstants.CACHE_SPOT_DETAIL_TTL
                + RandomUtil.randomLong(
                RedisConstants.CACHE_SPOT_DETAIL_RANDOM_TTL_MIN,
                RedisConstants.CACHE_SPOT_DETAIL_RANDOM_TTL_MAX + 1
        );
    }

    private SpotDetailVO querySpotDetailFromCache(String cacheKey) {
        String cacheJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheJson == null) {
            return null;
        }
        if (!StringUtils.hasText(cacheJson)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "景点不存在");
        }
        return JSONUtil.toBean(cacheJson, SpotDetailVO.class);
    }

    private SpotDetailVO rebuildSpotDetailCache(Long id, String cacheKey) {
        Spot spot = lambdaQuery()
                .eq(Spot::getId, id)
                .eq(Spot::getStatus, ENABLED_STATUS)
                .one();
        if (spot == null) {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    "",
                    RedisConstants.CACHE_SPOT_DETAIL_NULL_TTL,
                    TimeUnit.MINUTES
            );
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "景点不存在");
        }

        SpotDetailVO detailVO = toDetailVO(spot);
        stringRedisTemplate.opsForValue().set(
                cacheKey,
                JSONUtil.toJsonStr(detailVO),
                buildSpotDetailCacheTtl(),
                TimeUnit.MINUTES
        );
        return detailVO;
    }

    private boolean tryLock(String lockKey, String lockValue) {
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(
                lockKey,
                lockValue,
                RedisConstants.LOCK_SPOT_DETAIL_TTL,
                TimeUnit.SECONDS
        );
        return Boolean.TRUE.equals(success);
    }

    private void unlock(String lockKey, String lockValue) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(UNLOCK_SCRIPT);
        redisScript.setResultType(Long.class);
        stringRedisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
    }

    private void deleteSpotDetailCache(Long id) {
        String cacheKey = RedisConstants.CACHE_SPOT_DETAIL_KEY + id;
        try {
            stringRedisTemplate.delete(cacheKey);
        } catch (Exception exception) {
            log.warn("delete spot detail cache failed, spotId={}, cacheKey={}", id, cacheKey, exception);
        }
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(LOCK_RETRY_SLEEP_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求被中断");
        }
    }

    private void checkSaveDTO(SpotSaveDTO saveDTO) {
        if (saveDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "景点信息不能为空");
        }
    }

    private SpotListVO toListVO(Spot spot) {
        SpotListVO vo = new SpotListVO();
        vo.setId(spot.getId());
        vo.setCategoryId(spot.getCategoryId());
        vo.setName(spot.getName());
        vo.setAddress(spot.getAddress());
        vo.setLongitude(spot.getLongitude());
        vo.setLatitude(spot.getLatitude());
        vo.setCoverUrl(spot.getCoverUrl());
        vo.setBrief(spot.getBrief());
        vo.setOpenTime(spot.getOpenTime());
        vo.setTicketPrice(spot.getTicketPrice());
        vo.setHeatScore(spot.getHeatScore());
        return vo;
    }

    private SpotDetailVO toDetailVO(Spot spot) {
        SpotDetailVO vo = new SpotDetailVO();
        vo.setId(spot.getId());
        vo.setCategoryId(spot.getCategoryId());
        vo.setName(spot.getName());
        vo.setAddress(spot.getAddress());
        vo.setLongitude(spot.getLongitude());
        vo.setLatitude(spot.getLatitude());
        vo.setCoverUrl(spot.getCoverUrl());
        vo.setImages(spot.getImages());
        vo.setBrief(spot.getBrief());
        vo.setDetail(spot.getDetail());
        vo.setOpenTime(spot.getOpenTime());
        vo.setTicketPrice(spot.getTicketPrice());
        vo.setHeatScore(spot.getHeatScore());
        vo.setStatus(spot.getStatus());
        vo.setCreateTime(spot.getCreateTime());
        vo.setUpdateTime(spot.getUpdateTime());
        return vo;
    }

    private Spot toEntity(SpotSaveDTO saveDTO) {
        Spot spot = new Spot();
        spot.setCategoryId(saveDTO.getCategoryId());
        spot.setName(saveDTO.getName());
        spot.setAddress(saveDTO.getAddress());
        spot.setLongitude(saveDTO.getLongitude());
        spot.setLatitude(saveDTO.getLatitude());
        spot.setCoverUrl(saveDTO.getCoverUrl());
        spot.setImages(saveDTO.getImages());
        spot.setBrief(saveDTO.getBrief());
        spot.setDetail(saveDTO.getDetail());
        spot.setOpenTime(saveDTO.getOpenTime());
        spot.setTicketPrice(saveDTO.getTicketPrice());
        spot.setStatus(saveDTO.getStatus());
        return spot;
    }
}
