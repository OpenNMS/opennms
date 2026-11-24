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
package org.opennms.protocols.json.collector;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewrites XPath location steps that JXPath cannot parse.
 * <p>The JSON collection handlers evaluate the XPath expressions configured in
 * <code>xml-datacollection-config.xml</code> with Apache Commons JXPath, on top of the parsed
 * JSON document. JXPath parses those expressions as XPath 1.0, where a location step has to be
 * an <code>NCName</code>. JSON object keys are not restricted that way: a key may start with a
 * digit (<code>2xx</code>) or contain characters that are illegal in an XML name, and
 * <code>/stats/2xx</code> therefore fails with a
 * {@link org.apache.commons.jxpath.JXPathInvalidSyntaxException}.</p>
 * <p>This class rewrites such a step into the equivalent <code>*[name()='...']</code> form, which
 * JXPath does parse. That form was chosen deliberately over JXPath's own
 * <code>.[@name='...']</code> pointer syntax: only the wildcard form goes through the same
 * child-axis machinery as a plain named step, so it yields one pointer per element when the key
 * holds an array. <code>.[@name='...']</code> yields a single pointer at the whole array, which
 * would silently collapse a multi-instance <code>resource-xpath</code> to one resource.</p>
 * <p>Expressions that JXPath can already parse are returned unchanged, as the very same String
 * instance, so callers can cheaply detect whether a rewrite happened.</p>
 *
 * @see AbstractJsonCollectionHandler
 */
public final class JsonXpathRewriter {

    /**
     * Characters that, when they show up in the name part of a step, mean the step is something
     * other than a plain name test: a function call or node test (<code>node()</code>,
     * <code>count(...)</code>), an axis specifier or namespace prefix (<code>self::</code>),
     * a wildcard (which also covers an already rewritten step), an attribute somewhere other than
     * at the front, a variable reference, or an operator of a full XPath expression such as a
     * union. Those are left alone.
     */
    private static final String STEP_STOP_CHARS = "()@:*$|,=<>!";

    private JsonXpathRewriter() {
    }

    /**
     * Rewrites the location steps of the given expression that are not valid XPath names.
     *
     * @param xpath the XPath expression as configured; may be null
     * @return a JXPath-parsable equivalent, or the argument itself when nothing had to be rewritten
     */
    public static String rewrite(final String xpath) {
        if (xpath == null || xpath.isEmpty()) {
            return xpath;
        }
        final List<String> steps = splitSteps(xpath);
        String[] rewritten = null;
        for (int i = 0; i < steps.size(); i++) {
            final String step = steps.get(i);
            final String replacement = rewriteStep(step);
            if (replacement != step) {
                if (rewritten == null) {
                    rewritten = steps.toArray(new String[steps.size()]);
                }
                rewritten[i] = replacement;
            }
        }
        return rewritten == null ? xpath : String.join("/", rewritten);
    }

    /**
     * Splits an expression into location steps at the slashes that separate them.
     * <p>Only slashes at the top level are separators: a slash inside a predicate, inside the
     * arguments of a function call or inside a string literal belongs to the step it sits in.
     * Empty segments are kept, so that a leading <code>/</code>, a <code>//</code> and a trailing
     * <code>/</code> survive the round trip through {@link String#join}.</p>
     *
     * @param expr the expression
     * @return its steps, in order
     */
    private static List<String> splitSteps(final String expr) {
        final List<String> steps = new ArrayList<>();
        int brackets = 0;
        int parentheses = 0;
        char quote = 0;
        int start = 0;
        for (int i = 0; i < expr.length(); i++) {
            final char c = expr.charAt(i);
            if (quote != 0) {
                // XPath 1.0 string literals have no escape mechanism: the next matching quote ends it
                if (c == quote) {
                    quote = 0;
                }
                continue;
            }
            switch (c) {
                case '\'':
                case '"':
                    quote = c;
                    break;
                case '[':
                    brackets++;
                    break;
                case ']':
                    if (brackets > 0) {
                        brackets--;
                    }
                    break;
                case '(':
                    parentheses++;
                    break;
                case ')':
                    if (parentheses > 0) {
                        parentheses--;
                    }
                    break;
                case '/':
                    if (brackets == 0 && parentheses == 0) {
                        steps.add(expr.substring(start, i));
                        start = i + 1;
                    }
                    break;
                default:
                    break;
            }
        }
        steps.add(expr.substring(start));
        return steps;
    }

    /**
     * Rewrites a single location step, or returns it unchanged (as the same instance).
     *
     * @param step the step
     * @return the step, rewritten if its name is not a valid XPath name
     */
    private static String rewriteStep(final String step) {
        if (step.isEmpty()) {
            return step;
        }
        // predicates can only follow the name test, and brackets are balanced within a step
        final int predicate = step.indexOf('[');
        final String name = predicate < 0 ? step : step.substring(0, predicate);
        final String predicates = predicate < 0 ? "" : step.substring(predicate);

        final boolean attribute = name.startsWith("@");
        final String core = attribute ? name.substring(1) : name;

        if (!needsRewrite(core)) {
            return step;
        }
        return (attribute ? "@*[name()=" : "*[name()=") + toXPathLiteral(core) + ']' + predicates;
    }

    /**
     * Checks whether the name part of a step has to be rewritten to be parsable by JXPath.
     *
     * @param core the name part of the step, without a leading attribute axis abbreviation
     * @return true if the name has to be rewritten
     */
    private static boolean needsRewrite(final String core) {
        if (core.isEmpty() || ".".equals(core) || "..".equals(core)) {
            return false;
        }
        for (int i = 0; i < core.length(); i++) {
            final char c = core.charAt(i);
            if (Character.isWhitespace(c) || STEP_STOP_CHARS.indexOf(c) >= 0) {
                return false;
            }
        }
        return !isNcName(core);
    }

    /**
     * Checks whether the given name is a valid XPath name, following the <code>NCName</code> token
     * of JXPath's own grammar.
     * <p>The character classes are approximated with the ones the JDK offers. Both directions of
     * that approximation are safe: a name we wrongly consider valid is simply left alone and keeps
     * failing the way it does today, and a name we wrongly consider invalid still resolves
     * correctly through the rewritten form.</p>
     *
     * @param name the name
     * @return true if JXPath can parse the name as a location step
     */
    static boolean isNcName(final String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        final char first = name.charAt(0);
        if (first != '_' && !Character.isLetter(first)) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (c != '.' && c != '-' && c != '_' && !Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Turns the given value into an XPath string literal.
     * <p>XPath 1.0 has no escape mechanism inside a literal, so a value containing an apostrophe
     * is quoted with double quotes instead, and a value containing both quote characters has to be
     * assembled with <code>concat()</code>.</p>
     *
     * @param value the value
     * @return an XPath expression evaluating to the value
     */
    static String toXPathLiteral(final String value) {
        if (value.indexOf('\'') < 0) {
            return '\'' + value + '\'';
        }
        if (value.indexOf('"') < 0) {
            return '"' + value + '"';
        }
        final StringBuilder sb = new StringBuilder("concat(");
        final String[] parts = value.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(", \"'\", ");
            }
            sb.append('\'').append(parts[i]).append('\'');
        }
        return sb.append(')').toString();
    }
}
