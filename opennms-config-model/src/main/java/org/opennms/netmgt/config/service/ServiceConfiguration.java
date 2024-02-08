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
