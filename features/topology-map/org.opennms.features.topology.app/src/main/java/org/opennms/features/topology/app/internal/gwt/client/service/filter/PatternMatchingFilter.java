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
package org.opennms.features.topology.app.internal.gwt.client.service.filter;

import com.google.gwt.regexp.shared.RegExp;

public class PatternMatchingFilter extends AttributeComparisonFilter {

    private RegExp m_regex;
    
    private PatternMatchingFilter(String attribute, RegExp regex) {
        super(attribute);
        m_regex = regex;
    }

    public PatternMatchingFilter(String attribute, String value) {
        this(attribute, toRegex(value));
    }

    @Override
    protected boolean valueMatches(String value) {
        return m_regex.exec(value) == null;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("(");
        buf.append(getAttribute());
        buf.append("=");
        buf.append(toFilterMatch(m_regex));
        buf.append(")");
        return buf.toString();
    }

    public static RegExp toRegex(String value) {
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
        
        return RegExp.compile(value);
    }
    
    public static String toFilterMatch(RegExp regex) {
        String value = regex.getSource();
        
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
