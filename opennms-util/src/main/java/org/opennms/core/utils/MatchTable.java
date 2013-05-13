/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
public class MatchTable implements PropertiesUtils.SymbolTable {
    
    private Matcher m_matcher;

    /**
     * <p>Constructor for MatchTable.</p>
     *
     * @param m a {@link java.util.regex.Matcher} object.
     */
    public MatchTable(Matcher m) {
        m_matcher = m;
    }

    /** {@inheritDoc} */
    @Override
    public String getSymbolValue(String symbol) {
        try {
            int groupNum = Integer.parseInt(symbol);
            if (groupNum > m_matcher.groupCount())
                return null;
            return m_matcher.group(groupNum);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
}
