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

public class MateExpression {

    private List<Expression> expressions = new ArrayList<>();

    enum State {
        NON_MATE(),
        DOLLAR('$'),
        SIMPLE('{', '}'),
        //REC('[', ']'),
        JEXL('(', ')'),
        DOUBLE('"'),
        SINGLE('\'');

        private Character startCharacter = null, endCharacter = null;
        private Character delimiter = null;

        State() {
        }

        State(final Character delimiter) {
            this.delimiter = delimiter;
        }

        State(final Character startCharacter, final Character endCharacter, final Character delimiter) {
            this.startCharacter = startCharacter;
            this.endCharacter = endCharacter;
            this.delimiter = delimiter;
        }

        State(final Character startCharacter, final Character endCharacter) {
            this.startCharacter = startCharacter;
            this.endCharacter = endCharacter;
        }

        public Character getDelimiter() {
            return delimiter;
        }

        public Character getStartCharacter() {
            return startCharacter;
        }

        public Character getEndCharacter() {
            return endCharacter;
        }
    }

    private MateExpression(final String expression) throws MateParserException {
        final StringBuffer stringBuffer = new StringBuffer();
        State state = State.NON_MATE;
        int brackets = 0;
        boolean escaped = false;
        State oldState = null;

        for (int column = 0; column < expression.length(); ++column) {
            final char c = expression.charAt(column);

            switch (state) {
                case NON_MATE:
                    if (c == '$') {
                        expressions.add(PlainTextExpression.of(stringBuffer.toString()));
                        stringBuffer.delete(0, Integer.MAX_VALUE);
                        state = State.DOLLAR;
                    } else {
                        stringBuffer.append(c);
                    }
                    break;
                case DOLLAR:
                    if (c == State.SIMPLE.getStartCharacter()) {
                        brackets = 0;
                        state = State.SIMPLE;
                        continue;
                    } else if (c == State.JEXL.getStartCharacter()) {
                        state = State.JEXL;
                        continue;
                    } else {
                        stringBuffer.append(State.DOLLAR.getDelimiter());
                        stringBuffer.append(c);
                        state = State.NON_MATE;
                    }
                    break;
                case SIMPLE:
                case JEXL:
                    if (c == state.getEndCharacter()) {
                        if (brackets > 0) {
                            stringBuffer.append(c);
                            brackets--;
                        } else {
                            switch (state) {
                                case SIMPLE:
                                    final SimpleExpression simpleExpression = SimpleExpression.of(stringBuffer.toString());
                                    if (simpleExpression.isValid()) {
                                        expressions.add(simpleExpression);
                                    } else {
                                        final String content = State.DOLLAR.getDelimiter().toString() + State.SIMPLE.getStartCharacter() + stringBuffer + State.SIMPLE.getEndCharacter();
                                        expressions.add(PlainTextExpression.of(content));
                                    }
                                    break;
                                case JEXL:
                                    expressions.add(JexlExpression.of(stringBuffer.toString()));
                                    break;
                            }
                            stringBuffer.delete(0, Integer.MAX_VALUE);
                            state = State.NON_MATE;
                        }
                    } else if (c == state.getStartCharacter()) {
                        brackets++;
                        stringBuffer.append(c);
                    } else if (c == State.DOUBLE.getDelimiter()) {
                        stringBuffer.append(c);
                        oldState = state;
                        state = State.DOUBLE;
                    } else if (c == State.SINGLE.getDelimiter()) {
                        stringBuffer.append(c);
                        oldState = state;
                        state = State.SINGLE;
                    } else {
                        stringBuffer.append(c);
                    }
                    break;
                case DOUBLE:
                case SINGLE:
                    if (c == '\\') {
                        escaped = true;
                    } else if (c == state.getDelimiter() && !escaped) {
                        state = oldState;
                    } else {
                        escaped = false;
                    }
                    stringBuffer.append(c);
                    break;
                default:
                    // this should never occur
                    throw new MateParserException("Unknown parser state " + state + " at position " + column);
            }
        }

        if (stringBuffer.length() > 0) {
            expressions.add(PlainTextExpression.of(stringBuffer.toString()));
        }

        if (state.equals(State.SINGLE)) {
            throw new MateParserException("Missing single quote");
        } else if (state.equals(State.DOUBLE)) {
            throw new MateParserException("Missing double quote");
        } else if (!state.equals(State.NON_MATE)) {
            throw new MateParserException("Unexpected end of input");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MateExpression that = (MateExpression) o;
        return Objects.equals(expressions, that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }

    @Override
    public String toString() {
        final StringBuffer stringBuffer = new StringBuffer();
        for (Expression expression : expressions) {
            stringBuffer.append(expression.toString());
        }
        return stringBuffer.toString();
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public static MateExpression of(final String expression) throws MateParserException {
        return new MateExpression(expression);
    }
}
