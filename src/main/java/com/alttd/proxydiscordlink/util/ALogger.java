package com.alttd.proxydiscordlink.util;

import org.slf4j.Logger;

public class ALogger {

    private static Logger logger;

    public static void init(Logger log) {
        logger = log;
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void error(String message) {
        logger.error(message);
    }
}
