/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.statsd;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Report to be generated for nodes matching this
 *  package
 */
@XmlRootElement(name = "packageReport")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("statistics-daemon-configuration.xsd")
public class PackageReport implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final List<String> STATUS_OPTIONS = Arrays.asList("on", "off");

    /**
     * The report name. This is used internally to
     *  reference a configured report class.
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * The report description. This is shown in the web
     *  UI.
     */
    @XmlAttribute(name = "description", required = true)
    private String m_description;

    /**
     * the schedule at which the report is to be
     *  generated
     */
    @XmlAttribute(name = "schedule", required = true)
    private String m_schedule;

    /**
     * the amount of time after which this report has been
     *  created that it can be purged.
     */
    @XmlAttribute(name = "retainInterval", required = true)
    private String m_retainInterval;

    /**
     * status of the report; report is generated only if
     *  on
     */
    @XmlAttribute(name = "status", required = true)
    private PackageReportStatus m_status;

    /**
     * Package-specific parameters (if any) to be used
     *  for this report
     */
    @XmlElement(name = "parameter")
    private List<Parameter> m_parameters = new ArrayList<>();

    public PackageReport() { }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(final String description) {
        m_description = ConfigUtils.assertNotEmpty(description, "description");
    }

    public String getSchedule() {
        return m_schedule;
    }

    public void setSchedule(final String schedule) {
        m_schedule = ConfigUtils.assertNotEmpty(schedule, "schedule");
    }

    public String getRetainInterval() {
        return m_retainInterval;
    }

    public void setRetainInterval(final String retainInterval) {
        m_retainInterval = ConfigUtils.assertNotEmpty(retainInterval, "retainInterval");
    }

    public PackageReportStatus getStatus() {
        return m_status;
    }

    public void setStatus(final PackageReportStatus status) {
        m_status = ConfigUtils.assertOnlyContains(status, STATUS_OPTIONS, "status");
    }

    public List<Parameter> getParameters() {
        return m_parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        if (parameters == m_parameters) return;
        m_parameters.clear();
        if (parameters != null) m_parameters.addAll(parameters);
    }

    public void addParameter(final String key, final String value) {
        m_parameters.add(new Parameter(key, value));
    }

    public void addParameter(final Parameter parameter) {
        m_parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_description, 
                            m_schedule, 
                            m_retainInterval, 
                            m_status, 
                            m_parameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof PackageReport) {
            final PackageReport that = (PackageReport)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_description, that.m_description)
                    && Objects.equals(this.m_schedule, that.m_schedule)
                    && Objects.equals(this.m_retainInterval, that.m_retainInterval)
                    && Objects.equals(this.m_status, that.m_status)
                    && Objects.equals(this.m_parameters, that.m_parameters);
        }
        return false;
    }

}
