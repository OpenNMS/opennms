package org.opennms.core.logging;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

public class Logging {
    public static final String PREFIX_KEY = "prefix";

    public static <T> T withPrefix(final String prefix, final Callable<T> callable) throws Exception {
        final Map<String, String> mdc = Logging.getCopyOfContextMap();
        try {
            Logging.putPrefix(prefix);
            return callable.call();
        } finally {
            Logging.setContextMap(mdc);
        }

    }

    public static Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    public static void setContextMap(final Map<String, String> mdc) {
        if (mdc == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(mdc);
        }
    }

    public static void withPrefix(final String prefix, final Runnable runnable) {
        final Map<String, String> mdc = Logging.getCopyOfContextMap();
        try {
            Logging.putPrefix(prefix);
            runnable.run();
        } finally {
            Logging.setContextMap(mdc);
        }
    }

    public static void putPrefix(final String name) {
        MDC.put(PREFIX_KEY, name);
    }

}
