/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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
    private Integer m_maxVarsPerPdu;

    /**
     * Max Number of Interface per runnable. This is deprecated and will be
     * ignored in the code!
     *  
     */
    @XmlAttribute(name = "max-interface-per-pdu")
    private Integer m_maxInterfacePerPdu;

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
        return m_maxVarsPerPdu != null ? m_maxVarsPerPdu : 10;
    }

    public void setMaxVarsPerPdu(final Integer maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

    public Integer getMaxInterfacePerPdu() {
        return m_maxInterfacePerPdu != null ? m_maxInterfacePerPdu : 0;
    }

    public void setMaxInterfacePerPdu(final Integer maxInterfacePerPdu) {
        m_maxInterfacePerPdu = maxInterfacePerPdu;
    }

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
                            m_maxInterfacePerPdu);
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
                    && Objects.equals(this.m_maxVarsPerPdu, m_maxVarsPerPdu)
                    && Objects.equals(this.m_maxInterfacePerPdu, m_maxInterfacePerPdu);
        }
        return false;
    }

}
