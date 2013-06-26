package org.opennms.core.logging;

import java.io.File;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

public class Logging {

    public static final String PREFIX_KEY = "prefix";

    private static LoggingStrategy m_strategy;

    public static void availabilityReportConfigureLogging(String prefix) {
        getStrategy().availabilityReportConfigureLogging(prefix);
    }

    // FIXME: please
    public static void remotePollerInitializeLogging(String pollerHome)
            throws Exception {
        String logFile;
        logFile = System.getProperty("poller.logfile", pollerHome
                + File.separator + "opennms-remote-poller.log");
        File logDirectory = new File(logFile).getParentFile();
        if (!logDirectory.exists()) {
            if (!logDirectory.mkdirs()) {
                throw new IllegalStateException(
                                                "Could not create parent directory for log file '"
                                                        + logFile + "'");
            }
        }
        if (Boolean.getBoolean("debug")) {
            // LogUtils.logToConsole();
        } else {
            // LogUtils.logToFile(logFile);
        }
    }

    public static void remotePollerEnableDebugging() {
        // FIXME: Stubbed
    }

    private static synchronized LoggingStrategy getStrategy() {
        if (m_strategy == null) {
            ServiceLoader<LoggingStrategy> loader = ServiceLoader.load(LoggingStrategy.class);
            for (LoggingStrategy strategy : loader) {
                if (m_strategy != null) {
                    m_strategy = strategy;
                }
            }

            if (m_strategy == null) {
                throw new IllegalStateException(
                                                "Unable to locate a logging strategy");
            }
        }
        return m_strategy;
    }

    public static void shutdownLogging() {
        getStrategy().shutdownLogging();
    }

    public static void configureLogging() {
        getStrategy().configureLogging();
    }

    public static <T> T withPrefix(String prefix, Callable<T> callable)
            throws Exception {
        Map mdc = MDC.getCopyOfContextMap();
        try {
            mdc.put(PREFIX_KEY, prefix);
            return callable.call();
        } finally {
            MDC.setContextMap(mdc);
        }

    }

    @SuppressWarnings("unchecked")
    public static void withPrefix(String prefix, Runnable runnable) {
        Map mdc = MDC.getCopyOfContextMap();
        try {
            mdc.put(PREFIX_KEY, prefix);
            runnable.run();
        } finally {
            MDC.setContextMap(mdc);
        }
    }

    public static void configureInstallerLogging() {
        getStrategy().configureInstallerLogging();
    }

}
