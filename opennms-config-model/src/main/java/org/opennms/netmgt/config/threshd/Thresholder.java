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

package org.opennms.netmgt.config.threshd;

import java.io.Serializable;
import java.util.ArrayList;
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
 * Thresholder for a service
 */
@XmlRootElement(name = "thresholder")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("thresholding.xsd")
public class Thresholder implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Service name
     */
    @XmlAttribute(name = "service", required = true)
    private String m_service;

    /**
     * Java class name used to perform thresholding via the
     *  service
     */
    @XmlAttribute(name = "class-name", required = true)
    private String m_className;

    /**
     * Parameters to be used for threshold checking this
     *  service. Parameters are specfic to the service
     *  thresholder.
     */
    @XmlElement(name = "parameter")
    private List<Parameter> m_parameters = new ArrayList<>();

    public Thresholder() { }

    public String getService() {
        return m_service;
    }

    public void setService(final String service) {
        m_service = ConfigUtils.assertNotEmpty(service, "service");
    }

    public String getClassName() {
        return m_className;
    }

    public void setClassName(final String className) {
        m_className = ConfigUtils.assertNotEmpty(className, "class-name");
    }

    public List<Parameter> getParameters() {
        return m_parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        if (parameters == m_parameters) return;
        m_parameters.clear();
        if (parameters != null) m_parameters.addAll(parameters);
    }

    public void addParameter(final Parameter parameter) {
        m_parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_service, 
                            m_className, 
                            m_parameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Thresholder) {
            final Thresholder that = (Thresholder)obj;
            return Objects.equals(this.m_service, that.m_service)
                    && Objects.equals(this.m_className, that.m_className)
                    && Objects.equals(this.m_parameters, that.m_parameters);
        }
        return false;
    }

}
