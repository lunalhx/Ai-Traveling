package com.example.travel.utils;

import java.math.BigDecimal;

public final class GeoDistanceUtils {

    private static final double EARTH_RADIUS_METER = 6371000.0;
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");
    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");

    private GeoDistanceUtils() {
    }

    public static int calculateDistanceMeter(
            BigDecimal fromLongitude,
            BigDecimal fromLatitude,
            BigDecimal toLongitude,
            BigDecimal toLatitude
    ) {
        double fromLatRad = Math.toRadians(fromLatitude.doubleValue());
        double toLatRad = Math.toRadians(toLatitude.doubleValue());
        double deltaLatRad = Math.toRadians(toLatitude.subtract(fromLatitude).doubleValue());
        double deltaLonRad = Math.toRadians(toLongitude.subtract(fromLongitude).doubleValue());

        double value = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2)
                + Math.cos(fromLatRad) * Math.cos(toLatRad)
                * Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double centralAngle = 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value));
        return (int) Math.round(EARTH_RADIUS_METER * centralAngle);
    }

    public static boolean isLongitudeInvalid(BigDecimal longitude) {
        return longitude == null || longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0;
    }

    public static boolean isLatitudeInvalid(BigDecimal latitude) {
        return latitude == null || latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0;
    }
}
