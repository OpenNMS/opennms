/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the service-configuration.xml configuration file.
 */
@XmlRootElement(name = "service-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("service-configuration.xsd")
public class ServiceConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Service to be launched by the manager.
     */
    @XmlElement(name = "service")
    private List<Service> m_services = new ArrayList<>();

    public ServiceConfiguration() {
    }

    public ServiceConfiguration(final List<Service> services) {
        setServices(services);
    }

    public List<Service> getServices() {
        return m_services;
    }

    public void setServices(final List<Service> services) {
        if (services == m_services) return;
        m_services.clear();
        if (services != null) m_services.addAll(services);
    }

    public void addService(final Service service) {
        m_services.add(service);
    }

    public boolean removeService(final Service service) {
        return m_services.remove(service);
    }

    public int hashCode() {
        return Objects.hash(m_services);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ServiceConfiguration) {
            final ServiceConfiguration that = (ServiceConfiguration) obj;
            return Objects.equals(this.m_services, that.m_services);
        }
        return false;
    }
}
