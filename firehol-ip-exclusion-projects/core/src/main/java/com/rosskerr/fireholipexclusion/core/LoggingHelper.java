package com.rosskerr.fireholipexclusion.core;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class LoggingHelper {

    LambdaLogger baseLogger;

    public LoggingHelper(LambdaLogger baseLogger) {
        this.baseLogger = baseLogger;
    }

    public void info(String message) {
        baseLogger.log(String.format("INFO: %s\n", message));
    }
    public void warn(String message) {
        baseLogger.log(String.format("WARN: %s\n", message));
    }
    public void debug(String message) {
        baseLogger.log(String.format("DEBUG: %s\n", message));
    }
    public void error(String message, Throwable ex) {
        baseLogger.log(String.format("INFO: %s\n", message));
        if (ex != null) {
            baseLogger.log(Arrays.asList(ex.getStackTrace()).stream().map(i -> i.toString()).collect(Collectors.joining("\n")) + "\n");
        }
    }            
}
