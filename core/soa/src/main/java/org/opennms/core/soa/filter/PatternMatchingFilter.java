/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
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
        StringBuilder buf = new StringBuilder();
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
        
        Pattern pattern = Pattern.compile(value);
        return pattern;
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
