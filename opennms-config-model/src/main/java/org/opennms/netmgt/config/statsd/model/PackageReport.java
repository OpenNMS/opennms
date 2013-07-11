/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.statsd.model;

import java.util.LinkedHashMap;

/**
 * Represents a report that is configured on a specific package.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see Report
 * @see StatsdPackage
 * @version $Id: $
 */
public class PackageReport {
    private Report m_report;
    private String m_schedule;
    private boolean m_enabled;
    private LinkedHashMap<String, String> m_parameters = new LinkedHashMap<String, String>();
    private String m_description;
    private Long m_retainInterval;
    private StatsdPackage m_pkg;
    
    /**
     * <p>isEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isEnabled() {
        return m_enabled;
    }
    /**
     * <p>setEnabled</p>
     *
     * @param enabled a boolean.
     */
    public void setEnabled(boolean enabled) {
        m_enabled = enabled;
    }
    /**
     * <p>getParameters</p>
     *
     * @return a {@link java.util.LinkedHashMap} object.
     */
    public LinkedHashMap<String, String> getParameters() {
        return m_parameters;
    }
    /**
     * <p>setParameters</p>
     *
     * @param parameters a {@link java.util.LinkedHashMap} object.
     */
    public void setParameters(LinkedHashMap<String, String> parameters) {
        m_parameters = parameters;
    }
    /**
     * <p>addParameter</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void addParameter(String key, String value) {
        m_parameters.put(key, value);
    }
    /**
     * <p>getAggregateParameters</p>
     *
     * @return a {@link java.util.LinkedHashMap} object.
     */
    public LinkedHashMap<String, String> getAggregateParameters() {
        LinkedHashMap<String, String> agg = new LinkedHashMap<String, String>(getReport().getParameters());
        agg.putAll(getParameters());
        return agg;
    }
    /**
     * <p>getSchedule</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSchedule() {
        return m_schedule;
    }
    /**
     * <p>setSchedule</p>
     *
     * @param schedule a {@link java.lang.String} object.
     */
    public void setSchedule(String schedule) {
        m_schedule = schedule;
    }
    /**
     * <p>getReport</p>
     *
     * @return a {@link org.opennms.netmgt.config.statsd.model.Report} object.
     */
    public Report getReport() {
        return m_report;
    }
    /**
     * <p>setReport</p>
     *
     * @param report a {@link org.opennms.netmgt.config.statsd.model.Report} object.
     */
    public void setReport(Report report) {
        m_report = report;
    }
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_description;
    }
    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        m_description = description;
    }
    /**
     * <p>getRetainInterval</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getRetainInterval() {
        return m_retainInterval;
    }
    /**
     * <p>setRetainInterval</p>
     *
     * @param retainInterval a {@link java.lang.Long} object.
     */
    public void setRetainInterval(Long retainInterval) {
        m_retainInterval = retainInterval;
    }
    /**
     * <p>getPackage</p>
     *
     * @return a {@link org.opennms.netmgt.config.statsd.model.StatsdPackage} object.
     */
    public StatsdPackage getPackage() {
        return m_pkg;
    }
    /**
     * <p>setPackage</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.statsd.model.StatsdPackage} object.
     */
    public void setPackage(StatsdPackage pkg) {
        m_pkg = pkg;
    }
}
