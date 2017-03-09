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


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Interfaces to be polled for addresses in this
 *  package.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "interface")
@XmlAccessorType(XmlAccessType.FIELD)

@SuppressWarnings("all") public class Interface implements java.io.Serializable {


    /**
     * This represents the SQL criteria that is performed to select interfaces to
     * be polled
     *  example: (snmpifname like '%eth%' and snmpiftype=6)
     */
    @XmlAttribute(name = "criteria")
    private String criteria;

    /**
     * Interfaces group name
     */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * Interval at which the interfaces are to be
     *  polled
     */
    @XmlAttribute(name = "interval", required = true)
    private Long interval;

    /**
     * Specifies if the service is user defined. Used
     *  specifically for UI purposes.
     */
    @XmlAttribute(name = "user-defined")
    private String userDefined;

    /**
     * Status of the interfaces. The interfaces are polled only if
     *  this is set to 'on'.
     */
    @XmlAttribute(name = "status")
    private String status;

    /**
     * If set, overrides UDP port 161 as the port where SNMP
     *  GET/GETNEXT/GETBULK requests are sent.
     */
    @XmlAttribute(name = "port")
    private Integer port;

    /**
     * Default number of retries
     */
    @XmlAttribute(name = "retry")
    private Integer retry;

    /**
     * Default timeout (in milliseconds)
     */
    @XmlAttribute(name = "timeout")
    private Integer timeout;

    /**
     * Number of variables to send per SNMP request.
     *  
     */
    @XmlAttribute(name = "max-vars-per-pdu")
    private Integer maxVarsPerPdu;

    /**
     * Max Number of Interface per runnable. This is deprecated and will be
     * ignored in the code!
     *  
     */
    @XmlAttribute(name = "max-interface-per-pdu")
    private Integer maxInterfacePerPdu;

    public Interface() {
        setUserDefined("false");
        setStatus("on");
    }

    /**
     */
    public void deleteInterval() {
        this.interval= null;
    }

    /**
     */
    public void deleteMaxInterfacePerPdu() {
        this.maxInterfacePerPdu= null;
    }

    /**
     */
    public void deleteMaxVarsPerPdu() {
        this.maxVarsPerPdu= null;
    }

    /**
     */
    public void deletePort() {
        this.port= null;
    }

    /**
     */
    public void deleteRetry() {
        this.retry= null;
    }

    /**
     */
    public void deleteTimeout() {
        this.timeout= null;
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Interface) {
            Interface temp = (Interface)obj;
            boolean equals = Objects.equals(temp.criteria, criteria)
                && Objects.equals(temp.name, name)
                && Objects.equals(temp.interval, interval)
                && Objects.equals(temp.userDefined, userDefined)
                && Objects.equals(temp.status, status)
                && Objects.equals(temp.port, port)
                && Objects.equals(temp.retry, retry)
                && Objects.equals(temp.timeout, timeout)
                && Objects.equals(temp.maxVarsPerPdu, maxVarsPerPdu)
                && Objects.equals(temp.maxInterfacePerPdu, maxInterfacePerPdu);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'criteria'. The field 'criteria' has the
     * following description: This represents the SQL criteria that is performed
     * to select interfaces to be polled
     *  example: (snmpifname like '%eth%' and snmpiftype=6)
     * 
     * @return the value of field 'Criteria'.
     */
    public String getCriteria() {
        return this.criteria;
    }

    /**
     * Returns the value of field 'interval'. The field 'interval' has the
     * following description: Interval at which the interfaces are to be
     *  polled
     * 
     * @return the value of field 'Interval'.
     */
    public Long getInterval() {
        return this.interval;
    }

    /**
     * Returns the value of field 'maxInterfacePerPdu'. The field
     * 'maxInterfacePerPdu' has the following description: Max Number of Interface
     * per runnable. This is deprecated and will be ignored in the code!
     *  
     * 
     * @return the value of field 'MaxInterfacePerPdu'.
     */
    public Integer getMaxInterfacePerPdu() {
        return this.maxInterfacePerPdu != null ? this.maxInterfacePerPdu : Integer.valueOf("0");
    }

    /**
     * Returns the value of field 'maxVarsPerPdu'. The field 'maxVarsPerPdu' has
     * the following description: Number of variables to send per SNMP request.
     *  
     * 
     * @return the value of field 'MaxVarsPerPdu'.
     */
    public Integer getMaxVarsPerPdu() {
        return this.maxVarsPerPdu != null ? this.maxVarsPerPdu : Integer.valueOf("10");
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the following
     * description: Interfaces group name
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of field 'port'. The field 'port' has the following
     * description: If set, overrides UDP port 161 as the port where SNMP
     *  GET/GETNEXT/GETBULK requests are sent.
     * 
     * @return the value of field 'Port'.
     */
    public Integer getPort() {
        return this.port;
    }

    /**
     * Returns the value of field 'retry'. The field 'retry' has the following
     * description: Default number of retries
     * 
     * @return the value of field 'Retry'.
     */
    public Integer getRetry() {
        return this.retry;
    }

    /**
     * Returns the value of field 'status'. The field 'status' has the following
     * description: Status of the interfaces. The interfaces are polled only if
     *  this is set to 'on'.
     * 
     * @return the value of field 'Status'.
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Returns the value of field 'timeout'. The field 'timeout' has the following
     * description: Default timeout (in milliseconds)
     * 
     * @return the value of field 'Timeout'.
     */
    public Integer getTimeout() {
        return this.timeout;
    }

    /**
     * Returns the value of field 'userDefined'. The field 'userDefined' has the
     * following description: Specifies if the service is user defined. Used
     *  specifically for UI purposes.
     * 
     * @return the value of field 'UserDefined'.
     */
    public String getUserDefined() {
        return this.userDefined;
    }

    /**
     * Method hasInterval.
     * 
     * @return true if at least one Interval has been added
     */
    public boolean hasInterval() {
        return this.interval != null;
    }

    /**
     * Method hasMaxInterfacePerPdu.
     * 
     * @return true if at least one MaxInterfacePerPdu has been added
     */
    public boolean hasMaxInterfacePerPdu() {
        return this.maxInterfacePerPdu != null;
    }

    /**
     * Method hasMaxVarsPerPdu.
     * 
     * @return true if at least one MaxVarsPerPdu has been added
     */
    public boolean hasMaxVarsPerPdu() {
        return this.maxVarsPerPdu != null;
    }

    /**
     * Method hasPort.
     * 
     * @return true if at least one Port has been added
     */
    public boolean hasPort() {
        return this.port != null;
    }

    /**
     * Method hasRetry.
     * 
     * @return true if at least one Retry has been added
     */
    public boolean hasRetry() {
        return this.retry != null;
    }

    /**
     * Method hasTimeout.
     * 
     * @return true if at least one Timeout has been added
     */
    public boolean hasTimeout() {
        return this.timeout != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            criteria, 
            name, 
            interval, 
            userDefined, 
            status, 
            port, 
            retry, 
            timeout, 
            maxVarsPerPdu, 
            maxInterfacePerPdu);
        return hash;
    }

    /**
     * Sets the value of field 'criteria'. The field 'criteria' has the following
     * description: This represents the SQL criteria that is performed to select
     * interfaces to be polled
     *  example: (snmpifname like '%eth%' and snmpiftype=6)
     * 
     * @param criteria the value of field 'criteria'.
     */
    public void setCriteria(final String criteria) {
        this.criteria = criteria;
    }

    /**
     * Sets the value of field 'interval'. The field 'interval' has the following
     * description: Interval at which the interfaces are to be
     *  polled
     * 
     * @param interval the value of field 'interval'.
     */
    public void setInterval(final Long interval) {
        this.interval = interval;
    }

    /**
     * Sets the value of field 'maxInterfacePerPdu'. The field
     * 'maxInterfacePerPdu' has the following description: Max Number of Interface
     * per runnable. This is deprecated and will be ignored in the code!
     *  
     * 
     * @param maxInterfacePerPdu the value of field 'maxInterfacePerPdu'.
     */
    public void setMaxInterfacePerPdu(final Integer maxInterfacePerPdu) {
        this.maxInterfacePerPdu = maxInterfacePerPdu;
    }

    /**
     * Sets the value of field 'maxVarsPerPdu'. The field 'maxVarsPerPdu' has the
     * following description: Number of variables to send per SNMP request.
     *  
     * 
     * @param maxVarsPerPdu the value of field 'maxVarsPerPdu'.
     */
    public void setMaxVarsPerPdu(final Integer maxVarsPerPdu) {
        this.maxVarsPerPdu = maxVarsPerPdu;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the following
     * description: Interfaces group name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the value of field 'port'. The field 'port' has the following
     * description: If set, overrides UDP port 161 as the port where SNMP
     *  GET/GETNEXT/GETBULK requests are sent.
     * 
     * @param port the value of field 'port'.
     */
    public void setPort(final Integer port) {
        this.port = port;
    }

    /**
     * Sets the value of field 'retry'. The field 'retry' has the following
     * description: Default number of retries
     * 
     * @param retry the value of field 'retry'.
     */
    public void setRetry(final Integer retry) {
        this.retry = retry;
    }

    /**
     * Sets the value of field 'status'. The field 'status' has the following
     * description: Status of the interfaces. The interfaces are polled only if
     *  this is set to 'on'.
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Sets the value of field 'timeout'. The field 'timeout' has the following
     * description: Default timeout (in milliseconds)
     * 
     * @param timeout the value of field 'timeout'.
     */
    public void setTimeout(final Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the value of field 'userDefined'. The field 'userDefined' has the
     * following description: Specifies if the service is user defined. Used
     *  specifically for UI purposes.
     * 
     * @param userDefined the value of field 'userDefined'.
     */
    public void setUserDefined(final String userDefined) {
        this.userDefined = userDefined;
    }

}
