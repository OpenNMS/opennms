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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.jexl2.MapContext;
import org.opennms.core.mate.api.parser.Expression;
import org.opennms.core.mate.api.parser.JexlExpression;
import org.opennms.core.mate.api.parser.MateExpression;
import org.opennms.core.mate.api.parser.MateParserException;
import org.opennms.core.mate.api.parser.PlainTextExpression;
import org.opennms.core.mate.api.parser.SimpleExpression;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.utils.jexl.OnmsJexlEngine;
import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class Interpolator {
    private static final Logger LOG = LoggerFactory.getLogger(Interpolator.class);

    private static final int MAX_RECURSION_DEPTH = SystemProperties.getInteger("org.opennms.mate.maxRecursionDepth", 8);

    private static class ToBeInterpolated {
        private final Object value;
        public ToBeInterpolated(Object value) {
            this.value = value;
        }
    }

    private Interpolator() {}

    public static Map<String, Object> interpolateAttributes(final Map<String, Object> attributes, final Scope scope) {
        return Maps.transformValues(attributes, (raw) -> interpolateAttribute(raw, scope));
    }

    public static ToBeInterpolated pleaseInterpolate(final Object value) {
        return new ToBeInterpolated(value);
    }

    public static Map<String, Object> pleaseInterpolate(final Map<String, ?> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e-> pleaseInterpolate(e.getValue())));
    }

    public static Map<String, Object> interpolateObjects(final Map<String, Object> attributes, final Scope scope) {
        return Maps.transformValues(attributes, (raw) -> interpolate(raw, scope));
    }

    public static Map<String, String> interpolateStrings(final Map<String, String> attributes, final Scope scope) {
        return Maps.transformValues(attributes, (raw) -> raw != null ? interpolate(raw, scope).output : null);
    }

    private static Object interpolateAttribute(final Object value, final Scope scope) {
        if (value == null) {
            return null;
        }
        if (value instanceof ToBeInterpolated) {
            return interpolate(((ToBeInterpolated) value).value, scope);
        }

        return value;
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
            return new Result(null, List.of());
        }

        final ImmutableList.Builder<ResultPart> parts = ImmutableList.builder();
        final String output = interpolateRecursive(raw, parts, scope, 1);

        return new Result(output, parts.build());
    }

    private static String interpolateRecursive(final String input, final ImmutableList.Builder<ResultPart> parts, final Scope scope, final int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return input;
        }

        try {
            final String result = interpolateSingle(input, parts, scope);
            if (Objects.equals(input, result)) {
                return result;
            }
            return interpolateRecursive(result, parts, scope, depth + 1);
        } catch (MateParserException e) {
            LOG.warn("Error parsing MATE expression", e);
            return input;
        }
    }

    static class ParsingVisitor implements Expression.Visitor {
        public class Mate {
            public String mate(final String input) {
                return interpolate(input, scope).output;
            }
        }

        private final Scope scope;
        private String value = "";
        private final ImmutableList.Builder<ResultPart> parts;

        ParsingVisitor(final Scope scope, final ImmutableList.Builder<ResultPart> parts) {
            this.scope = scope;
            this.parts = parts;
        }

        @Override
        public void visit(final PlainTextExpression plainTextExpression) {
            value += plainTextExpression.getContent();
        }

        @Override
        public void visit(final SimpleExpression simpleExpression) {
            for(org.opennms.core.mate.api.parser.ContextKey contextKey : simpleExpression.getContextKeys()) {
                final ContextKey interpolatedContextKey = new ContextKey(interpolate(contextKey.getContext(), scope).output, interpolate(contextKey.getKey(), scope).output);
                final Optional<Scope.ScopeValue> replacement = scope.get(interpolatedContextKey);
                if (replacement.isPresent()) {
                    this.value += replacement.get().value;
                    parts.add(new ResultPart(simpleExpression.toString(), contextKey.toString(), replacement.get()));
                    return;
                }
            }
            if (simpleExpression.getDefaultValue() != null) {
                this.value += simpleExpression.getDefaultValue();
                parts.add(new ResultPart(simpleExpression.toString(), simpleExpression.getDefaultValue(), new Scope.ScopeValue(Scope.ScopeName.DEFAULT, simpleExpression.getDefaultValue())));
            }
        }

        @Override
        public void visit(JexlExpression jexlExpression) {
            final OnmsJexlEngine parser = new OnmsJexlEngine();
            parser.setFunctions(Collections.singletonMap(null, new Mate()));
            parser.white(Mate.class.getName());
            final org.apache.commons.jexl2.Expression expression = parser.createExpression(jexlExpression.getContent());
            value += String.valueOf(expression.evaluate(new MapContext()));
        }

        public String getValue() {
            return this.value;
        }
    }

    private static String interpolateSingle(final String input, final ImmutableList.Builder<ResultPart> parts, final Scope scope) throws MateParserException {
        final MateExpression mateExpression = MateExpression.of(input);
        final ParsingVisitor parsingVisitor = new ParsingVisitor(scope, parts);
        Expression.visit(mateExpression.getExpressions(), parsingVisitor);
        return parsingVisitor.value;
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
