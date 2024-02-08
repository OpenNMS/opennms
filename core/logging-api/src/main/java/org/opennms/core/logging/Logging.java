/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
