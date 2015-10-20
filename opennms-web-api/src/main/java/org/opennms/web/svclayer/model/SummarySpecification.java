/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.model;

/**
 * <p>SummarySpecification class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class SummarySpecification {
    private String m_filterRule;
    private long m_startTime;
    private long m_endTime;
    private String m_attributeSieve;
    
    /**
     * <p>getFilterRule</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFilterRule() {
        return m_filterRule;
    }
    /**
     * <p>setFilterRule</p>
     *
     * @param filterRule a {@link java.lang.String} object.
     */
    public void setFilterRule(String filterRule) {
        m_filterRule = filterRule;
    }
    /**
     * <p>getStartTime</p>
     *
     * @return a long.
     */
    public long getStartTime() {
        return m_startTime;
    }
    /**
     * <p>setStartTime</p>
     *
     * @param startTime a long.
     */
    public void setStartTime(long startTime) {
        m_startTime = startTime;
    }
    /**
     * <p>getEndTime</p>
     *
     * @return a long.
     */
    public long getEndTime() {
        return m_endTime;
    }
    /**
     * <p>setEndTime</p>
     *
     * @param endTime a long.
     */
    public void setEndTime(long endTime) {
        m_endTime = endTime;
    }
    
    /**
     * <p>getAttributeSieve</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAttributeSieve() {
        return m_attributeSieve;
    }
    
    /**
     * <p>setAttributeSieve</p>
     *
     * @param attributeSieve a {@link java.lang.String} object.
     */
    public void setAttributeSieve(String attributeSieve) {
        m_attributeSieve = attributeSieve;
    }
}

