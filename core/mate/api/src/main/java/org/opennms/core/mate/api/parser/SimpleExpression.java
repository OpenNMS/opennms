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
package org.opennms.core.mate.api.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aspectj.weaver.patterns.IToken;

public class SimpleExpression implements Expression {
    private final List<ContextKey> contextKeys;
    private final String defaultValue;

    private SimpleExpression(final List<ContextKey> contextKeys, final String defaultValue) {
        this.contextKeys = contextKeys;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(MateExpression.State.DOLLAR.getDelimiter());
        stringBuffer.append(MateExpression.State.SIMPLE.getStartCharacter());

        boolean first = true;

        for (ContextKey contextKey : contextKeys) {
            if (!first) {
                stringBuffer.append("|");
            }
            stringBuffer.append(contextKey.toString());
            first = false;
        }

        if (defaultValue != null) {
            stringBuffer.append("|");
            stringBuffer.append(defaultValue);
        }

        stringBuffer.append(MateExpression.State.SIMPLE.getEndCharacter());
        return stringBuffer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleExpression that = (SimpleExpression) o;
        return Objects.equals(contextKeys, that.contextKeys) && Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextKeys, defaultValue);
    }

    public List<ContextKey> getContextKeys() {
        return contextKeys;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    static SimpleExpression of(String expression) {
        final List<ContextKey> tokens = new ArrayList<>();

        String defaultValue = null;
        do {
            int index;

            if (expression.startsWith("\"")) {
                index = expression.indexOf("\"", 1);
                final String string = expression.substring(1, index);
                defaultValue = string;
                break;
            } else if (expression.startsWith("'")) {
                index = expression.indexOf("'", 1);
                final String string = expression.substring(1, index);
                defaultValue = string;
                break;
            } else {
                index = expression.indexOf("|");
                final String string;
                if (index == -1) {
                    string = expression;
                } else {
                    string = expression.substring(0, index);
                }
                final ContextKey token = ContextKey.of(string);
                if (token == null) {
                    defaultValue = string;
                } else {
                    tokens.add(token);
                }

                if (index == -1) {
                    break;
                }
            }

            expression = expression.substring(index + 1);
        } while (!expression.isEmpty());

        return new SimpleExpression(tokens, defaultValue);
    }

    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public boolean isValid() {
        return !contextKeys.isEmpty();
    }
}
