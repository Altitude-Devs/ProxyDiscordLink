package com.alttd.proxydiscordlink.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ALogger {

    private static Logger logger;

    public static void init(Logger log) {
        logger = log;
    }

    private void log(String message) {
        logger.info(message);
    }

    public static void warn(String message) {
        logger.warning(message);
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void error(String message) {
        logger.log(Level.SEVERE, message);
    }
}
