/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.svclayer;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class SummarySpecification {
    private String m_filterRule;
    private long m_startTime;
    private long m_endTime;
    private String m_attributeSieve;
    
    public String getFilterRule() {
        return m_filterRule;
    }
    public void setFilterRule(String filterRule) {
        m_filterRule = filterRule;
    }
    public long getStartTime() {
        return m_startTime;
    }
    public void setStartTime(long startTime) {
        m_startTime = startTime;
    }
    public long getEndTime() {
        return m_endTime;
    }
    public void setEndTime(long endTime) {
        m_endTime = endTime;
    }
    
    public String getAttributeSieve() {
        return m_attributeSieve;
    }
    
    public void setAttributeSieve(String attributeSieve) {
        m_attributeSieve = attributeSieve;
    }
}

