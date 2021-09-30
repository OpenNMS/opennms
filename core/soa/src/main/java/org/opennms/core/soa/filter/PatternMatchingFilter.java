/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.soa.filter;

import java.util.regex.Pattern;

/**
 * PatternMatchingFilter
 *
 * @author brozow
 */
public class PatternMatchingFilter extends AttributeComparisonFilter {

    private Pattern m_pattern;
    
    private PatternMatchingFilter(String attribute, Pattern pattern) {
        super(attribute);
        m_pattern = pattern;
    }

    public PatternMatchingFilter(String attribute, String value) {
        this(attribute, toRegex(value));
    }

    @Override
    protected boolean valueMatches(String value) {
        return m_pattern.matcher(value).matches();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("(");
        buf.append(getAttribute());
        buf.append("=");
        buf.append(toFilterMatch(m_pattern));
        buf.append(")");
        return buf.toString();
    }

    public static Pattern toRegex(String value) {
        // a pattern matching filter - convert value to regexp
        // 1. first hide 'escaped' stars so the aren't replaced later
        value = value.replace("\\*", "~~ESCAPED_STAR~~");
        
        // 2. replace all other backslashed chars with their actual values
        value = value.replaceAll("\\\\(.)", "$1");
        
        // 3. first escape the back slashes (before we escape the other chars
        value = escapeAll(value, "\\");
        
        // 4. escape regexp special chars (other than star and backslash)
        value = escapeAll(value, "?+.[]()^${}");
        
        // 5. convert wildcards into .*
        value = value.replace("*", ".*");
        
        // 6. put back escaped starts
        value = value.replace("~~ESCAPED_STAR~~", "\\*");
        
        return Pattern.compile(value);
    }
    
    public static String toFilterMatch(Pattern pattern) {
        String value = pattern.pattern();
        
        value = value.replace("\\*", "~~ESCAPED_STAR~~");
        
        value = value.replace(".*", "*");
        
        value = value.replaceAll("\\\\(.)", "$1");
        
        value = escapeAll(value, "\\");

        value = escapeAll(value, "()");
        
        value = value.replace("~~ESCAPED_STAR~~", "\\*");

        return value;
    }

    public static String escapeAll(String input, String chars) {
        String output = input;
        for(int i = 0; i < chars.length(); i++) {
            char ch = chars.charAt(i);
            output = output.replace(Character.toString(ch), "\\"+ch);
        }
        return output;
    }

}
