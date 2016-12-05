/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.logging;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

public abstract class Logging {
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

    public static void putThreadContext(final String key, final String value) {
        MDC.put(key,  value);
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

    /**
     * An adapter to restore the MDC context when done.
     */
    public static class MDCCloseable implements Closeable {
        private final Map<String, String> mdc;

        private MDCCloseable(Map<String, String> mdc) {
            this.mdc = mdc;
        }

        @Override
        public void close() {
            Logging.setContextMap(mdc);
        }
    }

    public static MDCCloseable withPrefixCloseable(final String prefix) {
        final Map<String, String> mdc = Logging.getCopyOfContextMap();
        Logging.putPrefix(prefix);
        return new MDCCloseable(mdc);
    }

    public static MDCCloseable withContextMapCloseable(final Map<String, String> contextMap) {
        final Map<String, String> mdc = Logging.getCopyOfContextMap();
        Logging.setContextMap(contextMap);
        return new MDCCloseable(mdc);
    }

    public static Runnable preserve(final Runnable runnable) {
        final Map<String, String> parentMdc = Logging.getCopyOfContextMap();
        return new Runnable() {
            @Override
            public void run() {
                final Map<String, String> localMdc = Logging.getCopyOfContextMap();
                try {
                    Logging.setContextMap(parentMdc);
                    runnable.run();
                } finally {
                    Logging.setContextMap(localMdc);
                }
            }
        };
    }

    public static void putPrefix(final String name) {
        MDC.put(PREFIX_KEY, name);
    }

}
