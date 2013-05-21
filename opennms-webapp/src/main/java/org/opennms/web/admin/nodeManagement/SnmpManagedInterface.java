/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.nodeManagement;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A servlet that stores interface information used in setting up SNMP Data
 * Collection
 *
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public class SnmpManagedInterface implements Serializable, Comparable<SnmpManagedInterface> {

    private static final long serialVersionUID = 7080084239250489410L;

    protected int m_id;
    protected int m_nodeId;
    protected int m_ifIndex;
    protected String m_ipHostname;
    protected String m_snmpStatus;
    protected String m_ifDescr;
    protected int m_ifType;
    protected String m_ifName;
    protected String m_ifAlias;
    protected String m_collectFlag;

    /**
     * <p>setSnmpInterfaceId</p>
     *
     * @param newId a int.
     */
    public void setSnmpInterfaceId(int newId) {
        m_id = newId;
    }
    /**
     * <p>getSnmpInterfaceId</p>
     *
     * @return a int.
     */
    public int getSnmpInterfaceId() {
        return m_id;
    }

    /**
     * <p>setNodeid</p>
     *
     * @param id a int.
     */
    public void setNodeid(int id) {
        m_nodeId = id;
    }

    /**
     * <p>getNodeid</p>
     *
     * @return a int.
     */
    public int getNodeid() {
        return m_nodeId;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param index a int.
     */
    public void setIfIndex(int index) {
        m_ifIndex = index;
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    public int getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>setIpHostname</p>
     *
     * @param newIpHostname a {@link java.lang.String} object.
     */
    public void setIpHostname(String newIpHostname) {
        m_ipHostname = newIpHostname;
    }

    /**
     * <p>getIpHostname</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpHostname() {
        return m_ipHostname;
    }

    /**
     * <p>setStatus</p>
     *
     * @param newStatus a {@link java.lang.String} object.
     */
    public void setStatus(String newStatus) {
        m_snmpStatus = newStatus;
    }

    /**
     * <p>getStatus</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatus() {
        return m_snmpStatus;
    }

    /**
     * <p>setIfDescr</p>
     *
     * @param newIfDescr a {@link java.lang.String} object.
     */
    public void setIfDescr(String newIfDescr) {
        m_ifDescr = newIfDescr;
    }

    /**
     * <p>getIfDescr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfDescr() {
        return m_ifDescr;
    }

    /**
     * <p>setIfType</p>
     *
     * @param newIfType a int.
     */
    public void setIfType(int newIfType) {
        m_ifType = newIfType;
    }

    /**
     * <p>getIfType</p>
     *
     * @return a int.
     */
    public int getIfType() {
        return m_ifType;
    }

    /**
     * <p>setIfName</p>
     *
     * @param newIfName a {@link java.lang.String} object.
     */
    public void setIfName(String newIfName) {
        m_ifName = newIfName;
    }

    /**
     * <p>getIfName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfName() {
        return m_ifName;
    }

    /**
     * <p>getIfAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfAlias() {
        return m_ifAlias;
    }

    /**
     * <p>setIfAlias</p>
     *
     * @param newIfAlias a {@link java.lang.String} object.
     */
    public void setIfAlias(String newIfAlias) {
        m_ifAlias = newIfAlias;
    }

    /**
     * <p>getCollectFlag</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCollectFlag() {
        return m_collectFlag;
    }
    
    /**
     * <p>setCollectFlag</p>
     *
     * @param newCollectFlag a {@link java.lang.String} object.
     */
    public void setCollectFlag(String newCollectFlag) {
        m_collectFlag = newCollectFlag;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getSnmpInterfaceId())
            .append("ifIndex", getIfIndex())
            .append("ipHostname", getIpHostname())
            .append("ifType", getIfType())
            .append("ifDescr", getIfDescr())
            .append("ifName", getIfName())
            .append("ifAlias", getIfAlias())
            .append("status", getStatus())
            .append("collect", getCollectFlag())
            .toString();
    }

    /**
     * <p>compareTo</p>
     *
     * @param obj a {@link org.opennms.web.admin.nodeManagement.SnmpManagedInterface} object.
     * @return a int.
     */
    @Override
    public int compareTo(SnmpManagedInterface obj) {
        return new CompareToBuilder()
            .append(getSnmpInterfaceId(), obj.getSnmpInterfaceId())
            .append(getIfIndex(), obj.getIfIndex())
            .append(getIpHostname(), obj.getIpHostname())
            .append(getIfType(), obj.getIfType())
            .append(getIfDescr(), obj.getIfDescr())
            .append(getIfName(), obj.getIfName())
            .append(getIfAlias(), obj.getIfAlias())
            .append(getStatus(), obj.getStatus())
            .append(getCollectFlag(), obj.getCollectFlag())
            .toComparison();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SnmpManagedInterface) {
            SnmpManagedInterface other = (SnmpManagedInterface) obj;
            return new EqualsBuilder()
                .append(getSnmpInterfaceId(), other.getSnmpInterfaceId())
                .append(getIfIndex(), other.getIfIndex())
                .append(getIpHostname(), other.getIpHostname())
                .append(getIfType(), other.getIfType())
                .append(getIfDescr(), other.getIfDescr())
                .append(getIfName(), other.getIfName())
                .append(getIfAlias(), other.getIfAlias())
                .append(getStatus(), other.getStatus())
                .append(getCollectFlag(), other.getCollectFlag())
                .isEquals();
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(617, 2677)
            .append(getSnmpInterfaceId())
            .append(getIfIndex())
            .append(getIpHostname())
            .append(getIfType())
            .append(getIfDescr())
            .append(getIfName())
            .append(getIfAlias())
            .append(getStatus())
            .append(getCollectFlag())
            .toHashCode();
      }
}
