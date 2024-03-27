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
package org.opennms.netmgt.config.poller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Monitor for a service
 */

@XmlRootElement(name="monitor")
@XmlAccessorType(XmlAccessType.NONE)
public class Monitor implements Serializable {
    private static final long serialVersionUID = 482336190022782148L;

    /**
     * Service name
     */
    @XmlAttribute(name="service")
    private String m_service;

    /**
     * Java class used to monitor/poll the service. The class must implement
     * the org.opennms.netmgt.poller.monitors.ServiceMonitor interface.
     */
    @XmlAttribute(name="class-name")
    private String m_className;

    /**
     * Parameters to be used for polling this service. E.g.: for polling HTTP,
     * the URL to hit is configurable via a parameter. Parameters are specfic
     * to the service monitor.
     */
    @XmlElement(name="parameter")
    private List<Parameter> m_parameters = new ArrayList<>();

    public Monitor() {
        super();
    }

    public Monitor(final String service, final String className) {
        this();
        setService(service);
        setClassName(className);
    }

    /**
     * Service name
     */
    public String getService() {
        return m_service;
    }

    public void setService(final String service) {
        m_service = service;
    }

    /**
     * Java class used to monitor/poll the service. The class must implement
     * the org.opennms.netmgt.poller.monitors.ServiceMonitor interface.
     */
    public String getClassName() {
        return m_className;
    }

    public void setClassName(final String className) {
        m_className = className;
    }

    public List<Parameter> getParameters() {
        if (m_parameters == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_parameters);
        }
    }

    public void setParameters(final List<Parameter> parameters) {
        m_parameters = new ArrayList<Parameter>(parameters);
    }

    public void addParameter(final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_className == null) ? 0 : m_className.hashCode());
        result = prime * result + ((m_parameters == null) ? 0 : m_parameters.hashCode());
        result = prime * result + ((m_service == null) ? 0 : m_service.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Monitor)) {
            return false;
        }
        final Monitor other = (Monitor) obj;
        if (m_className == null) {
            if (other.m_className != null) {
                return false;
            }
        } else if (!m_className.equals(other.m_className)) {
            return false;
        }
        if (m_parameters == null) {
            if (other.m_parameters != null) {
                return false;
            }
        } else if (!m_parameters.equals(other.m_parameters)) {
            return false;
        }
        if (m_service == null) {
            if (other.m_service != null) {
                return false;
            }
        } else if (!m_service.equals(other.m_service)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Monitor[service=" + m_service + ",className=" + m_className + ",parameters=" + m_parameters + "]";
    }
}
