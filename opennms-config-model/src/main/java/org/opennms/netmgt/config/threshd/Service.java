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
package org.opennms.netmgt.config.threshd;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Service for which thresholding is to be performed for
 *  addresses in this package
 */
@XmlRootElement(name = "service")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("thresholding.xsd")
public class Service implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Service name
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * Interval at which the service is to be threshold
     *  checked
     */
    @XmlAttribute(name = "interval", required = true)
    private Long m_interval;

    /**
     * Specifies if this is a user-defined service. Used
     *  specifically for UI purposes.
     */
    @XmlAttribute(name = "user-defined")
    private Boolean m_userDefined;

    /**
     * Thresholding status for this service. Service is
     *  checked against thresholds only if set to 'on'.
     */
    @XmlAttribute(name = "status")
    private ServiceStatus m_status;

    /**
     * Parameters to be used for threshold checking this
     *  service. Parameters are specfic to the service
     *  thresholder.
     */
    @XmlElement(name = "parameter")
    private List<Parameter> m_parameters = new ArrayList<>();

    public Service() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Long getInterval() {
        return m_interval;
    }

    public void setInterval(final Long interval) {
        m_interval = ConfigUtils.assertNotNull(interval, "interval");
    }

    public Boolean getUserDefined() {
        return m_userDefined;
    }

    public void setUserDefined(final Boolean userDefined) {
        m_userDefined = userDefined;
    }

    public Optional<ServiceStatus> getStatus() {
        return Optional.ofNullable(m_status);
    }

    public void setStatus(final ServiceStatus status) {
        m_status = status;
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
        return Objects.hash(m_name, 
                            m_interval, 
                            m_userDefined, 
                            m_status, 
                            m_parameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Service) {
            final Service that = (Service)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_interval, that.m_interval)
                    && Objects.equals(this.m_userDefined, that.m_userDefined)
                    && Objects.equals(this.m_status, that.m_status)
                    && Objects.equals(this.m_parameters, that.m_parameters);
        }
        return false;
    }

}
