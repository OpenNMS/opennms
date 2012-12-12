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

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * <p>
 * Service class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
@XmlType(name = "service")
public class Service extends ServiceTemplate implements Cloneable {
    private static final long serialVersionUID = -7231942028852991463L;

    @XmlAttribute(name = "template-name")
    private String m_templateName;

    @XmlTransient
    private ServiceTemplate m_template;

    public Service() {
        super();
    }

    public Service(Service copy) {
        super(copy);
        if (copy.m_templateName != null) {
            m_templateName = new String(copy.m_templateName);
        }
        if (copy.m_template != null) {
            m_template = new ServiceTemplate(copy.m_template);
        }
    }

    @XmlTransient
    public String getTemplateName() {
        return m_templateName;
    }

    public void setTemplateName(String templateName) {
        m_templateName = templateName;
    }

    @XmlTransient
    public ServiceTemplate getTemplate() {
        return m_template;
    }

    public void setTemplate(ServiceTemplate template) {
        m_template = template;
    }

    public int compareTo(Service obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getThreads(), obj.getThreads())
            .append(getPassiveServiceName(), obj.getPassiveServiceName())
            .append(getInterval(), obj.getInterval())
            .append(getStatus(), obj.getStatus())
            .append(getTemplateName(), obj.getTemplateName())
            .append(getParameters().toArray(OF_PARAMETERS), obj.getParameters().toArray(OF_PARAMETERS))
            .toComparison();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((m_template == null) ? 0 : m_template.hashCode());
        result = prime * result + ((m_templateName == null) ? 0 : m_templateName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Service) {
            Service other = (Service) obj;
            return new EqualsBuilder()
                .append(getName(), other.getName())
                .append(getThreads(), other.getThreads())
                .append(getPassiveServiceName(), other.getPassiveServiceName())
                .append(getInterval(), other.getInterval())
                .append(getStatus(), other.getStatus())
                .append(getTemplateName(), other.getTemplateName())
                .append(getParameters().toArray(OF_PARAMETERS), other.getParameters().toArray(OF_PARAMETERS))
                .isEquals();
        }
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Service cloned = new Service();
        cloned.m_template = m_template;
        cloned.m_templateName = m_templateName;
        cloned.m_name = m_name;
        cloned.m_threads = m_threads;
        cloned.m_passiveServiceName = m_passiveServiceName;
        cloned.m_interval = m_interval;
        cloned.m_status = m_status;
        cloned.m_parameters = new ArrayList<Parameter>();
        for (Parameter p : getParameters()) {
            cloned.m_parameters.add((Parameter) p.clone());
        }
        return cloned;
    }

}
