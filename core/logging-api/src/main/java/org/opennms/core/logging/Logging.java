package org.opennms.core.logging;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

public class Logging {

    public static final String PREFIX_KEY = "prefix";

    public static <T> T withPrefix(String prefix, Callable<T> callable)
            throws Exception {
        Map mdc = Logging.getCopyOfContextMap();
        try {
            Logging.putPrefix(prefix);
            return callable.call();
        } finally {
            Logging.setContextMap(mdc);
        }

    }
    
    public static Map getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }
    
    public static void setContextMap(Map mdc) {
        if (mdc == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(mdc);
        }
    }

    @SuppressWarnings("unchecked")
    public static void withPrefix(String prefix, Runnable runnable) {
        Map mdc = Logging.getCopyOfContextMap();
        try {
            Logging.putPrefix(prefix);
            runnable.run();
        } finally {
            Logging.setContextMap(mdc);
        }
    }

    public static void configureInstallerLogging() {
    }

    public static void putPrefix(String name) {
        MDC.put(PREFIX_KEY, name);
    }

}
