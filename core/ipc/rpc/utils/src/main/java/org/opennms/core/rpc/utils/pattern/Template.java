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

package org.opennms.core.rpc.utils.pattern;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Template {
    private final Pattern pattern;
    private final List<String> variables;

    private Template(final Pattern pattern,
                    final List<String> variables) {
        this.pattern = Objects.requireNonNull(pattern);
        this.variables = Objects.requireNonNull(variables);
    }

    public List<String> getVariables() {
        return this.variables;
    }

    public Optional<Map<String, String>> match(final String s) {
        Objects.requireNonNull(s);

        final Matcher matcher = this.pattern.matcher(s);
        if (matcher.find()) {
            final Map<String, String> result = new LinkedHashMap<>(this.variables.size());

            for (int i = 1; i <= matcher.groupCount(); i++) {
                final String name = this.variables.get(i - 1);
                final String value = matcher.group(i);

                result.put(name, value);
            }

            return Optional.of(result);
        }

        return Optional.empty();
    }

    public static Template parse(final String template) throws IllegalArgumentException {
        final List<String> variables = new ArrayList<>();
        final StringBuilder pattern = new StringBuilder();

        pattern.append('^');

        int level = 0;

        StringBuilder builder = new StringBuilder();

        for (int i = 0 ; i < template.length(); i++) {
            final char c = template.charAt(i);

            if (c == '{') {
                if (++level == 1) {
                    // start of variable
                    pattern.append(quote(builder));
                    builder = new StringBuilder();
                    continue;
                }
            }

            if (c == '}') {
                if (--level == 0) {
                    // end of URI variable
                    final String variable = builder.toString();
                    final int idx = variable.indexOf(':');
                    if (idx == -1) {
                        pattern.append("(.*)");
                        variables.add(variable);
                    }
                    else {
                        if (idx + 1 == variable.length()) {
                            throw new IllegalArgumentException(
                                    "No custom regular expression specified after ':' in \"" + variable + "\"");
                        }
                        pattern.append('(');
                        pattern.append(variable.substring(idx + 1));
                        pattern.append(')');
                        variables.add(variable.substring(0, idx));
                    }
                    builder = new StringBuilder();
                    continue;
                }
            }
            builder.append(c);
        }

        if (builder.length() > 0) {
            pattern.append(quote(builder));
        }

        pattern.append('$');

        return new Template(Pattern.compile(pattern.toString()), variables);
    }

    private static String quote(final StringBuilder builder) {
        return (builder.length() > 0 ? Pattern.quote(builder.toString()) : "");
    }
}
