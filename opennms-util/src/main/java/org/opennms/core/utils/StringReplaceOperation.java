/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
