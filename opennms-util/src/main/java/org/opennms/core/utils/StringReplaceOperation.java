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
package org.opennms.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>Abstract StringReplaceOperation class.</p>
 */
public abstract class StringReplaceOperation {
    protected String m_pattern;
    protected String m_replacement;
    
    /**
     * <p>Constructor for StringReplaceOperation.</p>
     *
     * @param spec a {@link java.lang.String} object.
     */
    public StringReplaceOperation(String spec) {
        if (spec == null) spec = "";
        Matcher specMatcher = Pattern.compile("^s/([^/]+)/([^/]*)/$").matcher(spec);
        if (specMatcher.matches()) {
            // Intern these strings to save RAM
            m_pattern = specMatcher.group(1).intern();
            m_replacement = specMatcher.group(2).intern();
        } else {
            throw new IllegalArgumentException("Specification '" + spec + "' is invalid; must be of the form s/pattern/replacement/ with no trailing modifiers");
        }
    }
    
    /**
     * <p>getPattern</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPattern() {
        return m_pattern;
    }
    
    /**
     * <p>getReplacement</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReplacement() {
        return m_replacement;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "Class: " + getClass().getName() + "; Pattern: " + m_pattern + "; Replacement: " + m_replacement;
    }
    
    /**
     * <p>replace</p>
     *
     * @param input a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public abstract String replace(String input);
    
}
