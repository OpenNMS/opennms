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
package org.opennms.netmgt.config.statsd.model;

import java.util.LinkedHashMap;
import java.util.Map;

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
    private Map<String, String> m_parameters = new LinkedHashMap<String, String>();
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
    public Map<String, String> getParameters() {
        return m_parameters;
    }
    /**
     * <p>setParameters</p>
     *
     * @param parameters a {@link java.util.LinkedHashMap} object.
     */
    public void setParameters(Map<String, String> parameters) {
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
    public Map<String, String> getAggregateParameters() {
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
