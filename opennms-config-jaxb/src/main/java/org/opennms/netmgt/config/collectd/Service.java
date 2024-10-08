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
package org.opennms.netmgt.config.collectd;

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
 * Service to be collected for addresses in this
 *  package
 */

@XmlRootElement(name="service")
@XmlAccessorType(XmlAccessType.NONE)
public class Service implements Serializable {
    private static final long serialVersionUID = -8462778654204715732L;

    /**
     * the service name
     */
    @XmlAttribute(name="name")
    private String m_name;

    /**
     * the interval at which the service is to be collected
     */
    @XmlAttribute(name="interval")
    private Long m_interval;

    /**
     * marker to say if service is user defined, used specifically for UI
     * purposes
     */
    @XmlAttribute(name="user-defined")
    private String m_userDefined;

    /**
     * status of the service, service is collected only if on
     */
    @XmlAttribute(name="status")
    private String m_status;

    /**
     * Parameters to be used for collecting data via this service.
     * "collection": name of data collection in datacollection-config.xml
     * ("SNMP" specific); SNMP parameters ("read-community", "version", etc)
     * will override defaults set in snmp-config.xml
     */
    @XmlElement(name="parameter")
    private List<Parameter> m_parameters = new ArrayList<>();

    public Service() {
        super();
    }

    /**
     * the service name
     */
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    /**
     * the interval at which the service is to be collected
     */
    public Long getInterval() {
        return m_interval == null? 0 : m_interval;
    }

    public void setInterval(final Long interval) {
        m_interval = interval;
    }

    /**
     * marker to say if service is user defined, used specifically for UI
     * purposes
     */
    public String getUserDefined() {
        return m_userDefined;
    }

    public void setUserDefined(final String userDefined) {
        m_userDefined = userDefined;
    }

    /**
     * status of the service, service is collected only if on
     */
    public String getStatus() {
        return m_status;
    }

    public void setStatus(final String status) {
        m_status = status;
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

    public void addParameter(final String key, final String value) {
        m_parameters.add(new Parameter(key, value));
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    public String getParameter(final String key) {
        for (final Parameter parm : m_parameters) {
            if (key.equals(parm.getKey())) {
                return parm.getValue();
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_interval == null) ? 0 : m_interval.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_parameters == null) ? 0 : m_parameters.hashCode());
        result = prime * result + ((m_status == null) ? 0 : m_status.hashCode());
        result = prime * result + ((m_userDefined == null) ? 0 : m_userDefined.hashCode());
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
        if (!(obj instanceof Service)) {
            return false;
        }
        final Service other = (Service) obj;
        if (m_interval == null) {
            if (other.m_interval != null) {
                return false;
            }
        } else if (!m_interval.equals(other.m_interval)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_parameters == null) {
            if (other.m_parameters != null) {
                return false;
            }
        } else if (!m_parameters.equals(other.m_parameters)) {
            return false;
        }
        if (m_status == null) {
            if (other.m_status != null) {
                return false;
            }
        } else if (!m_status.equals(other.m_status)) {
            return false;
        }
        if (m_userDefined == null) {
            if (other.m_userDefined != null) {
                return false;
            }
        } else if (!m_userDefined.equals(other.m_userDefined)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Service [name=" + m_name + ", interval=" + m_interval + ", userDefined=" + m_userDefined + ", status=" + m_status + ", parameters=" + m_parameters + "]";
    }

}
