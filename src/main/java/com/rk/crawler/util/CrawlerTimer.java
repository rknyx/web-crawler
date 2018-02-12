package com.rk.crawler.util;

public class CrawlerTimer {
    private static ThreadLocal<Long> localTime = new ThreadLocal<>();

    public static void start() {
        localTime.set(System.currentTimeMillis());
    }

    public static String getTimeString() {
        return String.format("%sms", System.currentTimeMillis() - localTime.get());
    }
}
