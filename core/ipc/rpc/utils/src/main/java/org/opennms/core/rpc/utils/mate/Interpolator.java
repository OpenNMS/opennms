/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.utils.mate;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class Interpolator {
    private final static Logger LOG = LoggerFactory.getLogger(Interpolator.class);

    private static final String OUTER_REGEXP = "\\$\\{(.+?:.+?)\\}";
    private static final String INNER_REGEXP = "(?:([^\\|]+?:[^\\|]+)|([^\\|]+))";
    private static final Pattern OUTER_PATTERN = Pattern.compile(OUTER_REGEXP);
    private static final Pattern INNER_PATTERN = Pattern.compile(INNER_REGEXP);

    private Interpolator() {}

    public static Map<String, Object> interpolateObjects(final Map<String, Object> attributes, final Scope scope) {
        return Maps.transformValues(attributes, (raw) -> interpolate(raw, scope));
    }

    public static Map<String, String> interpolateStrings(final Map<String, String> attributes, final Scope scope) {
        return Maps.transformValues(attributes, (raw) -> interpolate(raw, scope));
    }

    public static Object interpolate(final Object value, final Scope scope) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return interpolate((String) value, scope);
        } else {
            // Serialize, interpolate and deserialize the value

            final String xml;
            try {
                xml = JaxbUtils.marshal(value);
            } catch (final Throwable ex) {
                LOG.error("Failed to serialize config", ex);
                return value;
            }

            final String interpolatedXml = interpolate(xml, scope);

            final Object interpolatedValue;
            try {
                interpolatedValue = JaxbUtils.unmarshal(value.getClass(), interpolatedXml);
            } catch (final Throwable ex) {
                LOG.error("Failed to deserialize interpolated config", ex);
                return value;
            }

            return interpolatedValue;
        }
    }

    public static String interpolate(final String raw, final Scope scope) {
        final StringBuffer stringBuffer = new StringBuffer();
        final Matcher outerMatcher = OUTER_PATTERN.matcher(raw);
        while (outerMatcher.find()) {
            final Matcher innerMatcher = INNER_PATTERN.matcher(outerMatcher.group(1));

            String result = "";
            while (innerMatcher.find()) {
                if (innerMatcher.group(1) != null) {
                    final String[] arr = innerMatcher.group(1).split(":", 2);
                    final ContextKey contextKey = new ContextKey(arr[0], arr[1]);

                    final Optional<String> replacement = scope.get(contextKey);
                    if (replacement.isPresent()) {
                        result = Matcher.quoteReplacement(replacement.get());
                        break;
                    }
                } else if (innerMatcher.group(2) != null) {
                    result = Matcher.quoteReplacement(innerMatcher.group(2));
                    break;
                }
            }

            outerMatcher.appendReplacement(stringBuffer, result);
        }

        outerMatcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }
}
