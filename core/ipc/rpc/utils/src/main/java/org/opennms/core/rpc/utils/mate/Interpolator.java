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

import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.poller.ComplexPollerParameter;
import org.opennms.netmgt.poller.PollerParameter;
import org.opennms.netmgt.poller.SimplePollerParameter;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Interpolator {
    private static final String OUTER_REGEXP = "\\$\\{(.+?:.+?)\\}";
    private static final String INNER_REGEXP = "(?:([^\\|]+?:[^\\|]+)|([^\\|]+))";
    private static final Pattern OUTER_PATTERN = Pattern.compile(OUTER_REGEXP);
    private static final Pattern INNER_PATTERN = Pattern.compile(INNER_REGEXP);

    private Interpolator() {}

    public static PollerParameter interpolate(final PollerParameter value, final Scope scope) {
        if (value == null) {
            return null;
        }

        Objects.requireNonNull(scope);

        final Optional<SimplePollerParameter> simple = value.asSimple();
        if (simple.isPresent()) {
            return PollerParameter.simple(interpolate(simple.get().getValue(), scope));
        }

        final Optional<ComplexPollerParameter> complex = value.asComplex();
        if (complex.isPresent()) {
            final Element element = complex.get().getElement();

            final Element interpolated = (Element) element.cloneNode(true);

            // Walk the element and interpolate all attribute and element values
            final Stack<Element> tail = new Stack<>();
            tail.push(interpolated);

            while (!tail.empty()) {
                final Element current = tail.pop();

                final NodeList elements = current.getElementsByTagName("*");
                for (int i = 0; i < elements.getLength(); i++) {
                    tail.push((Element) elements.item(i));
                }

                if (current.getNodeValue() != null) {
                    current.setNodeValue(interpolate(current.getNodeValue(), scope));
                }

                final NamedNodeMap attributes = current.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    final Attr attr = (Attr) attributes.item(i);
                    attr.setValue(interpolate(attr.getValue(), scope));
                }
            }

            return PollerParameter.complex(interpolated);
        }

        return value;
    }

    public static String interpolate(final String raw, final Scope scope) {
        if (raw == null) {
            return null;
        }

        Objects.requireNonNull(scope);

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
