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
package org.opennms.mock.wsman;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates the WHERE clause subset the fake agent understands: comparisons of a
 * property against a quoted string or a number, joined by AND, with OR allowed
 * inside one level of parentheses. Numbers compare numerically, strings
 * lexically (which is what makes DMTF timestamps order correctly).
 */
final class WqlWhere {

    private static final Pattern COMPARISON = Pattern.compile(
            "^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*(<>|!=|>=|<=|=|>|<)\\s*(?:'((?:[^'\\\\]|\\\\.)*)'|(-?\\d+(?:\\.\\d+)?))\\s*$");

    private WqlWhere() {
    }

    static boolean matches(final String where, final Map<String, String> instance) {
        if (where == null || where.isBlank()) {
            return true;
        }
        for (final String term : splitTopLevel(where, "and")) {
            String t = term.trim();
            if (t.startsWith("(") && t.endsWith(")")) {
                t = t.substring(1, t.length() - 1);
                boolean any = false;
                for (final String alt : splitTopLevel(t, "or")) {
                    any |= comparison(alt, instance);
                }
                if (!any) {
                    return false;
                }
            } else if (!comparison(t, instance)) {
                return false;
            }
        }
        return true;
    }

    private static boolean comparison(final String expr, final Map<String, String> instance) {
        final Matcher m = COMPARISON.matcher(expr);
        if (!m.matches()) {
            throw new IllegalArgumentException("Unsupported WQL term: " + expr);
        }
        final String property = m.group(1);
        final String op = m.group(2);
        String actual = null;
        for (final Map.Entry<String, String> e : instance.entrySet()) {
            if (e.getKey().equalsIgnoreCase(property)) {
                actual = e.getValue();
            }
        }
        if (actual == null) {
            return false;
        }
        final int cmp;
        if (m.group(4) != null) {
            cmp = Double.compare(Double.parseDouble(actual.trim()), Double.parseDouble(m.group(4)));
        } else {
            cmp = actual.compareToIgnoreCase(unescape(m.group(3)));
        }
        switch (op) {
            case "=": return cmp == 0;
            case "<>":
            case "!=": return cmp != 0;
            case ">": return cmp > 0;
            case ">=": return cmp >= 0;
            case "<": return cmp < 0;
            default: return cmp <= 0;
        }
    }

    /** WQL escapes with a backslash. */
    private static String unescape(final String literal) {
        final StringBuilder sb = new StringBuilder(literal.length());
        for (int i = 0; i < literal.length(); i++) {
            final char c = literal.charAt(i);
            if (c == '\\' && i + 1 < literal.length()) {
                sb.append(literal.charAt(++i));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Splits on a keyword that sits outside quotes and parentheses. */
    private static List<String> splitTopLevel(final String s, final String keyword) {
        final List<String> parts = new ArrayList<>();
        int depth = 0;
        boolean quoted = false;
        int start = 0;
        final String lower = s.toLowerCase();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '\\' && quoted) {
                i++;
            } else if (c == '\'') {
                quoted = !quoted;
            } else if (!quoted && c == '(') {
                depth++;
            } else if (!quoted && c == ')') {
                depth--;
            } else if (!quoted && depth == 0 && Character.isWhitespace(c)
                    && lower.startsWith(keyword, i + 1)
                    && i + 1 + keyword.length() < s.length()
                    && Character.isWhitespace(s.charAt(i + 1 + keyword.length()))) {
                parts.add(s.substring(start, i));
                start = i + 1 + keyword.length();
                i = start;
            }
        }
        parts.add(s.substring(start));
        return parts;
    }
}
