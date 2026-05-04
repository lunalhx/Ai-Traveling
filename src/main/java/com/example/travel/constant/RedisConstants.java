package com.example.travel.constant;

public final class RedisConstants {

    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 5L;

    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    public static final String CACHE_SPOT_DETAIL_KEY = "cache:spot:detail:";
    public static final String LOCK_SPOT_DETAIL_KEY = "lock:spot:detail:";
    public static final Long CACHE_SPOT_DETAIL_TTL = 30L;
    public static final Long CACHE_SPOT_DETAIL_RANDOM_TTL_MIN = 1L;
    public static final Long CACHE_SPOT_DETAIL_RANDOM_TTL_MAX = 10L;
    public static final Long CACHE_SPOT_DETAIL_NULL_TTL = 2L;
    public static final Long LOCK_SPOT_DETAIL_TTL = 10L;

    public static final String GEO_SPOT_KEY = "geo:spot";
    public static final Integer NEARBY_SPOT_DEFAULT_RADIUS = 3000;
    public static final Integer NEARBY_SPOT_MAX_RADIUS = 50000;
    public static final Integer NEARBY_SPOT_DEFAULT_LIMIT = 10;
    public static final Integer NEARBY_SPOT_MAX_LIMIT = 50;

    public static final String CHECKIN_USER_KEY = "checkin:user:";
    public static final String CHECKIN_SPOT_KEY = "checkin:spot:";
    public static final String CHECKIN_DATE_PATTERN = "yyyy-MM-dd";

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String SECKILL_ORDER_KEY = "seckill:order:";
    public static final String SECKILL_RESULT_KEY = "seckill:result:";

    private RedisConstants() {
    }
}
