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
package org.opennms.core.sysprops;

import java.math.BigDecimal;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to extract system properties, similar to Integer.getInteger. This class allows to distinguish between a none
 * present value and a malformed value. In case of the latter a log message is produced. Integer.getInteger would just return
 * the defaultValue without informing the user and can therefor lead to hard to find configuration problems.
 */
public class SystemProperties {

    private final static Logger LOG = LoggerFactory.getLogger(SystemProperties.class);

    public static BigDecimal getBigDecimal(String propertyName) {
        return getBigDecimal(propertyName, null);
    }

    public static BigDecimal getBigDecimal(String propertyName, BigDecimal defaultValue) {
        Function<String, BigDecimal> resolver = (propertyValue) -> (new BigDecimal(propertyValue));
        return getProperty(propertyName, defaultValue, resolver);
    }

    public static Long getLong(String propertyName) {
        return getLong(propertyName, null);
    }

    public static Long getLong(String propertyName, int defaultValue) {
        Long result = getLong(propertyName, null);
        return (result == null) ? Long.valueOf(defaultValue) : result;
    }

    public static Long getLong(String propertyName, Long defaultValue) {
        Function<String, Long> resolver = (propertyValue) -> (Long.parseLong(propertyValue));
        return getProperty(propertyName, defaultValue, resolver);
    }

    public static Integer getInteger(String propertyName) {
        return getInteger(propertyName, null);
    }

    public static Integer getInteger(String propertyName, int defaultValue) {
        Integer result = getInteger(propertyName, null);
        return (result == null) ? Integer.valueOf(defaultValue) : result;
    }

    public static Integer getInteger(String propertyName, Integer defaultValue) {
        Function<String, Integer> resolver = (propertyValue) -> (Integer.parseInt(propertyValue));
        return getProperty(propertyName, defaultValue, resolver);
    }

    private static <T> T getProperty(String propertyName, T defaultValue, Function<String, T> transformer) {
        String valueAsString = System.getProperty(propertyName);
        if (valueAsString == null) {
            return defaultValue;
        }
        try {
            return transformer.apply(valueAsString);
        } catch (NumberFormatException e) {
            String message = String.format("cannot parse system property: %s with value=%s, using default=%s instead."
                    , propertyName
                    , valueAsString
                    , defaultValue);
            LOG.warn(message, e);
        }
        return defaultValue;
    }


    public static Boolean getBooleanWithDefaultAsTrue(String name) {
        boolean defaultValue = true;
        try {
            String singleTopic = System.getProperty(name);
            if(singleTopic != null) {
                defaultValue = !singleTopic.equalsIgnoreCase("false");
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            // Ignore
        }
        return defaultValue;
    }


}
