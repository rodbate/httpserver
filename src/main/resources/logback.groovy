

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

import static ch.qos.logback.classic.Level.ALL

scan("30 seconds")

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%file:%line] [%level] %msg%n"
    }
}

appender("com.rodbate", RollingFileAppender) {
    encoder(PatternLayoutEncoder){
        pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%file:%line] [%level] %msg%n";
    }
    rollingPolicy(TimeBasedRollingPolicy){
        fileNamePattern = "logs/server-log-%d{yyyy-MM-dd}.log";
        // 6 + 1
        maxHistory = 6;
        cleanHistoryOnStart = true;
    }
}

logger("com.rodbate", ALL, ["com.rodbate", "STDOUT"], false)
root(ALL, ["STDOUT"])
//root(DEBUG, ["STDOUT"])
