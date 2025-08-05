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
package org.opennms.netmgt.snmp.snmp4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.log.LogLevel;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Collections;

/**
 * Custom SNMP4J LogFactory implementation that bridges to SLF4J.
 * This replaces the need for snmp4j-log4j dependency while maintaining
 * all logging functionality.
 */
public class Slf4jLogFactory extends LogFactory {

    @Override
    protected LogAdapter createLogger(String name) {
        return new Slf4jLogAdapter(LoggerFactory.getLogger(name));
    }


    @Override
    public LogAdapter getRootLogger() {
        return new Slf4jLogAdapter(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME));
    }

    @Override
    public Iterator<?> loggers() {
        // SLF4J doesn't provide a way to enumerate all loggers
        // Return empty iterator as this is rarely used
        return Collections.emptyIterator();
    }

    /**
     * SLF4J-based LogAdapter implementation for SNMP4J
     */
    private static class Slf4jLogAdapter implements LogAdapter, Comparable<Object> {
        private final Logger logger;

        public Slf4jLogAdapter(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void debug(Serializable message) {
            logger.debug(String.valueOf(message));
        }

        @Override
        public void info(CharSequence message) {
            logger.info(String.valueOf(message));
        }

        @Override
        public void warn(Serializable message) {
            logger.warn(String.valueOf(message));
        }

        @Override
        public void error(Serializable message) {
            logger.error(String.valueOf(message));
        }

        @Override
        public void error(CharSequence message, Throwable throwable) {
            logger.error(String.valueOf(message), throwable);
        }

        @Override
        public void fatal(Object message) {
            logger.error(String.valueOf(message));
        }

        @Override
        public void fatal(CharSequence message, Throwable throwable) {
            logger.error(String.valueOf(message), throwable);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        @Override
        public LogLevel getLogLevel() {
            if (logger.isDebugEnabled()) {
                return LogLevel.DEBUG;
            } else if (logger.isInfoEnabled()) {
                return LogLevel.INFO;
            } else if (logger.isWarnEnabled()) {
                return LogLevel.WARN;
            } else {
                return LogLevel.ERROR;
            }
        }

        @Override
        public LogLevel getEffectiveLogLevel() {
            return getLogLevel();
        }

        @Override
        public void setLogLevel(LogLevel level) {
            // SLF4J log levels are typically configured externally
            // This method is a no-op as SLF4J doesn't support runtime level changes
        }

        @Override
        public String getName() {
            return logger.getName();
        }

        @Override
        public Iterator<?> getLogHandler() {
            // SLF4J doesn't expose appenders directly
            // Return empty iterator as this is rarely used
            return Collections.emptyIterator();
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof Slf4jLogAdapter) {
                return getName().compareTo(((Slf4jLogAdapter) o).getName());
            }
            return 0;
        }
    }
}