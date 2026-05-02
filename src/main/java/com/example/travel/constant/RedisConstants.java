package com.example.travel.constant;

public final class RedisConstants {

    public static final String LOGIN_CODE_KEY = "travel:login:code:";
    public static final long LOGIN_CODE_TTL_MINUTES = 5L;

    public static final String LOGIN_USER_KEY = "travel:login:token:";
    public static final long LOGIN_USER_TTL_MINUTES = 60L * 24L;

    public static final String CACHE_SPOT_KEY = "travel:cache:spot:";
    public static final long CACHE_SPOT_TTL_MINUTES = 30L;
    public static final long CACHE_NULL_TTL_MINUTES = 2L;

    public static final String LOCK_SPOT_KEY = "travel:lock:spot:";
    public static final long LOCK_SPOT_TTL_SECONDS = 10L;

    public static final String GEO_SPOT_KEY = "travel:geo:spot";
    public static final String CHECKIN_RECORD_KEY = "travel:checkin:record:";

    public static final String SECKILL_STOCK_KEY = "travel:seckill:stock:";
    public static final String SECKILL_ORDER_KEY = "travel:seckill:order:";

    private RedisConstants() {
    }
}
