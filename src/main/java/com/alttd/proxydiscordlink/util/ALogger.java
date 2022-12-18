package com.alttd.proxydiscordlink.util;

public class ALogger {

    private static ALogger logger;

    public static void init(ALogger log) {
        logger = log;
    }

    private void log(String message) {
        logger.log(message);
    }

    public static void warn(String message) {
        logger.log("WARNING: " + message);
    }

    public static void info(String message) {
        logger.log("INFO: " + message);
    }

    public static void error(String message) {
        logger.log("ERROR: " + message);
    }
}
