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
package org.opennms.netmgt.config.snmpinterfacepoller;


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Interfaces to be polled for addresses in this
 *  package.
 */
@XmlRootElement(name = "interface")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("snmp-interface-poller-configuration.xsd")
public class Interface implements Serializable {
    private static final List<String> STATUS_CHOICES = Arrays.asList("on", "off");

    private static final long serialVersionUID = 1L;

    /**
     * This represents the SQL criteria that is performed to select interfaces to
     * be polled
     *  example: (snmpifname like '%eth%' and snmpiftype=6)
     */
    @XmlAttribute(name = "criteria")
    private String m_criteria;

    /**
     * Interfaces group name
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * Interval at which the interfaces are to be
     *  polled
     */
    @XmlAttribute(name = "interval", required = true)
    private Long m_interval;

    /**
     * Specifies if the service is user defined. Used
     *  specifically for UI purposes.
     */
    @XmlAttribute(name = "user-defined")
    private Boolean m_userDefined = Boolean.FALSE;

    /**
     * Status of the interfaces. The interfaces are polled only if
     *  this is set to 'on'.
     */
    @XmlAttribute(name = "status")
    private String m_status = "on";

    /**
     * If set, overrides UDP port 161 as the port where SNMP
     *  GET/GETNEXT/GETBULK requests are sent.
     */
    @XmlAttribute(name = "port")
    private Integer m_port;

    /**
     * Default number of retries
     */
    @XmlAttribute(name = "retry")
    private Integer m_retry;

    /**
     * Default timeout (in milliseconds)
     */
    @XmlAttribute(name = "timeout")
    private Integer m_timeout;

    /**
     * Number of variables to send per SNMP request.
     *  
     */
    @XmlAttribute(name = "max-vars-per-pdu")
    private Integer m_maxVarsPerPdu = 10;

    /**
     * Max Number of Interface per runnable. This is deprecated and will be
     * ignored in the code!
     *  
     */
    @XmlAttribute(name = "max-interface-per-pdu")
    private Integer m_maxInterfacePerPdu = 0;

    /**
     * Values of ifAdminStatus and ifOperStatus to treat as up values.
     * Expects a comma separated list of discrete values i.e. '1,3'.
     *
     */
    @XmlAttribute(name = "up-values")
    private String m_upValues;

    /**
     * Values of ifAdminStatus and ifOperStatus to treat as down values.
     * Expects a comma separated list of discrete values i.e. '2,3,5,7'.
     *
     */
    @XmlAttribute(name = "down-values")
    private String m_downValues;

    public Interface() {
    }

    public Optional<String> getCriteria() {
        return Optional.ofNullable(m_criteria);
    }

    public void setCriteria(final String criteria) {
        m_criteria = ConfigUtils.normalizeString(criteria);
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

    public String getStatus() {
        return m_status;
    }

    public void setStatus(final String status) {
        m_status = ConfigUtils.assertOnlyContains(status, STATUS_CHOICES, "status");
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(m_port);
    }

    public void setPort(final Integer port) {
        m_port = ConfigUtils.assertMinimumInclusive(port, 1, "port");
    }

    public Optional<Integer> getRetry() {
        return Optional.ofNullable(m_retry);
    }

    public void setRetry(final Integer retry) {
        m_retry = retry;
    }

    public Optional<Integer> getTimeout() {
        return Optional.ofNullable(m_timeout);
    }

    public void setTimeout(final Integer timeout) {
        m_timeout = timeout;
    }

    public Integer getMaxVarsPerPdu() {
        return m_maxVarsPerPdu;
    }

    public void setMaxVarsPerPdu(final Integer maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

    public Integer getMaxInterfacePerPdu() {
        return m_maxInterfacePerPdu;
    }

    public void setMaxInterfacePerPdu(final Integer maxInterfacePerPdu) {
        m_maxInterfacePerPdu = maxInterfacePerPdu;
    }

    public Optional<String> getUpValues() { return Optional.ofNullable(m_upValues); }

    public void setUpValues(final String upValues) { m_upValues = upValues; }

    public Optional<String> getDownValues() { return Optional.ofNullable(m_downValues); }

    public void setDownValues(final String downValues) { m_downValues = downValues; }

    @Override
    public int hashCode() {
        return Objects.hash(m_criteria, 
                            m_name, 
                            m_interval, 
                            m_userDefined, 
                            m_status, 
                            m_port, 
                            m_retry, 
                            m_timeout, 
                            m_maxVarsPerPdu, 
                            m_upValues,
                            m_downValues);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Interface) {
            final Interface that = (Interface)obj;
            return Objects.equals(this.m_criteria, that.m_criteria)
                    && Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_interval, that.m_interval)
                    && Objects.equals(this.m_userDefined, that.m_userDefined)
                    && Objects.equals(this.m_status, that.m_status)
                    && Objects.equals(this.m_port, that.m_port)
                    && Objects.equals(this.m_retry, that.m_retry)
                    && Objects.equals(this.m_timeout, that.m_timeout)
                    && Objects.equals(this.m_maxVarsPerPdu, that.m_maxVarsPerPdu)
                    && Objects.equals(this.m_upValues, that.m_upValues)
                    && Objects.equals(this.m_downValues, that.m_downValues);
        }
        return false;
    }

}
