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
package org.opennms.core.mate.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.xml.JaxbUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Interpolator {
    private static final String OUTER_REGEXP = "\\$\\{([^\\{\\}]+?:[^\\{\\}]+?)\\}";
    private static final String INNER_REGEXP = "(?:([^\\|]+?:[^\\|]+)|([^\\|]+))";
    private static final Pattern OUTER_PATTERN = Pattern.compile(OUTER_REGEXP);
    private static final Pattern INNER_PATTERN = Pattern.compile(INNER_REGEXP);

    private static final int MAX_RECURSION_DEPTH = SystemProperties.getInteger("org.opennms.mate.maxRecursionDepth", 8);

    private static LoadingCache<ScopeProvider, Scope> scopeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(300, TimeUnit.SECONDS)
            .build(new CacheLoader<ScopeProvider, Scope>() {
                @Override
                public Scope load(final ScopeProvider scopeProvider) {
                    return scopeProvider.getScope();
                }
            });

    private static class ToBeInterpolated {
        private final Object value;
        public ToBeInterpolated(Object value) {
            this.value = value;
        }
    }

    private Interpolator() {}

    public static Map<String, Object> interpolateAttributes(final Map<String, Object> attributes, final ScopeProvider scopeProvider) {
        return Maps.transformValues(attributes, (raw) -> interpolateAttribute(raw, scopeProvider));
    }

    public static ToBeInterpolated pleaseInterpolate(final Object value) {
        return new ToBeInterpolated(value);
    }

    public static Map<String, Object> pleaseInterpolate(final Map<String, ?> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e-> pleaseInterpolate(e.getValue())));
    }

    public static Map<String, Object> interpolateObjects(final Map<String, Object> attributes, final ScopeProvider scopeProvider) {
        return Maps.transformValues(attributes, (raw) -> interpolate(raw, scopeProvider));
    }

    public static Map<String, String> interpolateStrings(final Map<String, String> attributes, final ScopeProvider scopeProvider) {
        return Maps.transformValues(attributes, (raw) -> raw != null ? interpolate(raw, scopeProvider).output : null);
    }

    private static Object interpolateAttribute(final Object value, final ScopeProvider scopeProvider) {
        if (value == null) {
            return null;
        }
        if (value instanceof ToBeInterpolated) {
            return interpolate(((ToBeInterpolated) value).value, scopeProvider);
        }

        return value;
    }

    public static Object interpolate(final Object value, final ScopeProvider scopeProvider) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return interpolate((String) value, scopeProvider).output;
        } else {
            if (value.getClass().isAnnotationPresent(XmlRootElement.class)) {
                return JaxbUtils.unmarshal(value.getClass(), interpolate(JaxbUtils.marshal(value), scopeProvider).output, false);
            } else {
                return value;
            }
        }
    }

    public static Result interpolate(final String raw, final ScopeProvider scopeProvider) {
        if (raw == null) {
            return new Result(null, List.of());
        }

        final ImmutableList.Builder<ResultPart> parts = ImmutableList.builder();
        final String output = interpolateRecursive(raw, parts, scopeProvider, 1);

        return new Result(output, parts.build());
    }

    private static String interpolateRecursive(final String input, final ImmutableList.Builder<ResultPart> parts, final ScopeProvider scopeProvider, final int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return input;
        }

        final String result = interpolateSingle(input, parts, scopeProvider);
        if (Objects.equals(input, result)) {
            return result;
        }

        return interpolateRecursive(result, parts, scopeProvider, depth + 1);
    }

    private static String interpolateSingle(final String input, final ImmutableList.Builder<ResultPart> parts, final ScopeProvider scopeProvider) {
        final StringBuilder output = new StringBuilder();
        final Matcher outerMatcher = OUTER_PATTERN.matcher(input);

        while (outerMatcher.find()) {
            final Matcher innerMatcher = INNER_PATTERN.matcher(outerMatcher.group(1));

            String result = "";
            while (innerMatcher.find()) {
                if (innerMatcher.group(1) != null) {
                    final String[] arr = innerMatcher.group(1).split(":", 2);
                    final ContextKey contextKey = new ContextKey(arr[0], arr[1]);

                    final Optional<Scope.ScopeValue> replacement = scopeCache.getUnchecked(scopeProvider).get(contextKey);
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
