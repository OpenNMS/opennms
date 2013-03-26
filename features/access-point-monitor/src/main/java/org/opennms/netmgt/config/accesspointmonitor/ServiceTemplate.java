/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.accesspointmonitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * <p>
 * ServiceTemplate class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
@XmlType(name = "service-template")
public class ServiceTemplate implements Serializable, Comparable<ServiceTemplate> {
    private static final long serialVersionUID = -7451942028852991463L;

    protected static final Parameter[] OF_PARAMETERS = new Parameter[0];

    @XmlAttribute(name = "name")
    protected String m_name;

    @XmlAttribute(name = "threads")
    protected Integer m_threads;

    @XmlAttribute(name = "passive-service-name")
    protected String m_passiveServiceName;

    @XmlAttribute(name = "interval")
    protected Long m_interval;

    @XmlAttribute(name = "status")
    protected String m_status;

    @XmlElement(name = "parameter")
    protected List<Parameter> m_parameters = new ArrayList<Parameter>();

    public ServiceTemplate() {

    }

    public ServiceTemplate(ServiceTemplate copy) {
        if (copy.m_name != null) {
            m_name = new String(copy.m_name);
        }
        if (copy.m_threads != null) {
            m_threads = Integer.valueOf(copy.m_threads);
        }
        if (copy.m_passiveServiceName != null) {
            m_passiveServiceName = new String(copy.m_passiveServiceName);
        }
        if (copy.m_interval != null) {
            m_interval = Long.valueOf(copy.m_interval);
        }
        if (copy.m_status != null) {
            m_status = new String(copy.m_status);
        }
        for (Parameter p : copy.m_parameters) {
            m_parameters.add(new Parameter(p));
        }
    }

    @XmlTransient
    public List<Parameter> getParameters() {
        return m_parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        m_parameters = parameters;
    }

    public void addParameter(Parameter parameter) {
        m_parameters.add(parameter);
    }

    public void removeParameter(Parameter parameter) {
        m_parameters.remove(parameter);
    }

    public void removeParameterByKey(String key) {
        for (Iterator<Parameter> itr = m_parameters.iterator(); itr.hasNext();) {
            Parameter parameter = itr.next();
            if (parameter.getKey().equals(key)) {
                m_parameters.remove(parameter);
                return;
            }
        }
    }

    @XmlTransient
    public Map<String, String> getParameterMap() {
        Map<String, String> parameterMap = new HashMap<String, String>();
        for (Parameter p : getParameters()) {
            parameterMap.put(p.getKey(), p.getValue());
        }
        return parameterMap;
    }

    @XmlTransient
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    @XmlTransient
    public Integer getThreads() {
        return m_threads;
    }

    public void setThreads(Integer threads) {
        m_threads = threads;
    }

    @XmlTransient
    public String getPassiveServiceName() {
        return m_passiveServiceName;
    }

    public void setPassiveServiceName(String passiveServiceName) {
        m_passiveServiceName = passiveServiceName;
    }

    @XmlTransient
    public Long getInterval() {
        return m_interval;
    }

    public void setInterval(Long interval) {
        m_interval = interval;
    }

    @XmlTransient
    public String getStatus() {
        return m_status;
    }

    public void setStatus(String status) {
        m_status = status;
    }

    public int compareTo(ServiceTemplate obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getThreads(), obj.getThreads())
            .append(getPassiveServiceName(), obj.getPassiveServiceName())
            .append(getInterval(), obj.getInterval())
            .append(getStatus(), obj.getStatus())
            .append(getParameters().toArray(OF_PARAMETERS), obj.getParameters().toArray(OF_PARAMETERS))
            .toComparison();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_interval == null) ? 0 : m_interval.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_parameters == null) ? 0 : m_parameters.hashCode());
        result = prime * result + ((m_passiveServiceName == null) ? 0 : m_passiveServiceName.hashCode());
        result = prime * result + ((m_status == null) ? 0 : m_status.hashCode());
        result = prime * result + ((m_threads == null) ? 0 : m_threads.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceTemplate) {
            ServiceTemplate other = (ServiceTemplate) obj;
            return new EqualsBuilder()
                .append(getName(), other.getName())
                .append(getThreads(), other.getThreads())
                .append(getPassiveServiceName(), other.getPassiveServiceName())
                .append(getInterval(), other.getInterval())
                .append(getStatus(), other.getStatus())
                .append(getParameters().toArray(OF_PARAMETERS), other.getParameters().toArray(OF_PARAMETERS))
                .isEquals();
        }
        return false;
    }
}
