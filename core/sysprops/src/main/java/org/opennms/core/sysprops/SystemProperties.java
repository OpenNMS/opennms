/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.sysprops;

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

}
