/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 10: Created this file.
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.castor.statsd;

import java.util.LinkedHashMap;

/**
 * Represents a report that is configured on a specific package.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see Report
 * @see StatsdPackage
 */
public class PackageReport {
    private Report m_report;
    private String m_schedule;
    private boolean m_enabled;
    private LinkedHashMap<String, String> m_parameters = new LinkedHashMap<String, String>();
    private String m_description;
    private Long m_retainInterval;
    private StatsdPackage m_pkg;
    
    public boolean isEnabled() {
        return m_enabled;
    }
    public void setEnabled(boolean enabled) {
        m_enabled = enabled;
    }
    public LinkedHashMap<String, String> getParameters() {
        return m_parameters;
    }
    public void setParameters(LinkedHashMap<String, String> parameters) {
        m_parameters = parameters;
    }
    public void addParameter(String key, String value) {
        m_parameters.put(key, value);
    }
    public LinkedHashMap<String, String> getAggregateParameters() {
        LinkedHashMap<String, String> agg = new LinkedHashMap<String, String>(getReport().getParameters());
        agg.putAll(getParameters());
        return agg;
    }
    public String getSchedule() {
        return m_schedule;
    }
    public void setSchedule(String schedule) {
        m_schedule = schedule;
    }
    public Report getReport() {
        return m_report;
    }
    public void setReport(Report report) {
        m_report = report;
    }
    public String getDescription() {
        return m_description;
    }
    public void setDescription(String description) {
        m_description = description;
    }
    public Long getRetainInterval() {
        return m_retainInterval;
    }
    public void setRetainInterval(Long retainInterval) {
        m_retainInterval = retainInterval;
    }
    public StatsdPackage getPackage() {
        return m_pkg;
    }
    public void setPackage(StatsdPackage pkg) {
        m_pkg = pkg;
    }
}
