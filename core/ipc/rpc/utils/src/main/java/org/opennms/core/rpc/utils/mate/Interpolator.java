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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.xml.JaxbUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpolator {
    private static final String OUTER_REGEXP = "\\$\\{([^\\{\\}]+?:[^\\{\\}]+?)\\}";
    private static final String INNER_REGEXP = "(?:([^\\|]+?:[^\\|]+)|([^\\|]+))";
    private static final Pattern OUTER_PATTERN = Pattern.compile(OUTER_REGEXP);
    private static final Pattern INNER_PATTERN = Pattern.compile(INNER_REGEXP);

    private static final int MAX_RECURSION_DEPTH = SystemProperties.getInteger("org.opennms.mate.maxRecursionDepth", 8);

    private Interpolator() {}

    public static Map<String, Object> interpolateObjects(final Map<String, Object> attributes, final Scope scope) {
        return Maps.transformValues(attributes, (raw) -> interpolate(raw, scope));
    }

    public static Map<String, String> interpolateStrings(final Map<String, String> attributes, final Scope scope) {
        return Maps.transformValues(attributes, (raw) -> raw != null ? interpolate(raw, scope).output : null);
    }

    public static Object interpolate(final Object value, final Scope scope) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return interpolate((String) value, scope).output;
        } else {
            if (value.getClass().isAnnotationPresent(XmlRootElement.class)) {
                return JaxbUtils.unmarshal(value.getClass(), interpolate(JaxbUtils.marshal(value), scope).output, false);
            } else {
                return value;
            }
        }
    }

    public static Result interpolate(final String raw, final Scope scope) {
        if (raw == null) {
            return null;
        }

        final ImmutableList.Builder<ResultPart> parts = ImmutableList.builder();
        final String output = interpolateRecursive(raw, parts, scope, 1);

        return new Result(output, parts.build());
    }

    private static String interpolateRecursive(final String input, final ImmutableList.Builder<ResultPart> parts, final Scope scope, final int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return input;
        }

        final String result = interpolateSingle(input, parts, scope);
        if (Objects.equals(input, result)) {
            return result;
        }

        return interpolateRecursive(result, parts, scope, depth + 1);
    }

    private static String interpolateSingle(final String input, final ImmutableList.Builder<ResultPart> parts, final Scope scope) {
        final StringBuilder output = new StringBuilder();
        final Matcher outerMatcher = OUTER_PATTERN.matcher(input);
        while (outerMatcher.find()) {
            final Matcher innerMatcher = INNER_PATTERN.matcher(outerMatcher.group(1));

            String result = "";
            while (innerMatcher.find()) {
                if (innerMatcher.group(1) != null) {
                    final String[] arr = innerMatcher.group(1).split(":", 2);
                    final ContextKey contextKey = new ContextKey(arr[0], arr[1]);

                    final Optional<Scope.ScopeValue> replacement = scope.get(contextKey);
                    if (replacement.isPresent()) {
                        result = Matcher.quoteReplacement(replacement.get().value);
                        parts.add(new ResultPart(outerMatcher.group(), innerMatcher.group(1), replacement.get()));
                        break;
                    }
                } else if (innerMatcher.group(2) != null) {
                    result = Matcher.quoteReplacement(innerMatcher.group(2));
                    parts.add(new ResultPart(outerMatcher.group(), innerMatcher.group(2), new Scope.ScopeValue(Scope.ScopeName.DEFAULT, innerMatcher.group(2))));
                    break;
                }
            }

            outerMatcher.appendReplacement(output, result);
        }

        outerMatcher.appendTail(output);

        return output.toString();
    }

    public static class Result {
        public final String output;
        public final List<ResultPart> parts;

        public Result(final String output, final List<ResultPart> parts) {
            this.output = output;
            this.parts = parts;
        }
    }

    public static class ResultPart {
        public final String input;
        public final String match;
        public final Scope.ScopeValue value;

        public ResultPart(final String input, final String match, final Scope.ScopeValue value) {
            this.input = input;
            this.match = match;
            this.value = value;
        }
    }
}
