//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Feb 27: Updated to be aware of the snmpInterface id and snmpCollect flags
// 2002 Sep 24: Added the ability to select SNMP interfaces for collection.
//              Code based on original manage/unmanage code.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

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
    private static final long serialVersionUID = 1L;

    protected int m_id;
    protected String m_address;
    protected int m_nodeId;
    protected int m_ifIndex;
    protected String m_ipHostname;
    protected String m_snmpStatus;
    protected String m_ifDescr;
    protected int m_ifType;
    protected String m_ifName;
    protected String m_ifAlias;
    protected String m_collectFlag;

    public void setSnmpInterfaceId(int newId) {
        m_id = newId;
    }
    public int getSnmpInterfaceId() {
        return m_id;
    }
    public void setAddress(String newAddress) {
        m_address = newAddress;
    }

    public String getAddress() {
        return m_address;
    }

    public void setNodeid(int id) {
        m_nodeId = id;
    }

    public int getNodeid() {
        return m_nodeId;
    }

    public void setIfIndex(int index) {
        m_ifIndex = index;
    }

    public int getIfIndex() {
        return m_ifIndex;
    }

    public void setIpHostname(String newIpHostname) {
        m_ipHostname = newIpHostname;
    }

    public String getIpHostname() {
        return m_ipHostname;
    }

    public void setStatus(String newStatus) {
        m_snmpStatus = newStatus;
    }

    public String getStatus() {
        return m_snmpStatus;
    }

    public void setIfDescr(String newIfDescr) {
        m_ifDescr = newIfDescr;
    }

    public String getIfDescr() {
        return m_ifDescr;
    }

    public void setIfType(int newIfType) {
        m_ifType = newIfType;
    }

    public int getIfType() {
        return m_ifType;
    }

    public void setIfName(String newIfName) {
        m_ifName = newIfName;
    }

    public String getIfName() {
        return m_ifName;
    }

    public String getIfAlias() {
        return m_ifAlias;
    }

    public void setIfAlias(String newIfAlias) {
        m_ifAlias = newIfAlias;
    }

    public String getCollectFlag() {
        return m_collectFlag;
    }
    
    public void setCollectFlag(String newCollectFlag) {
        m_collectFlag = newCollectFlag;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getSnmpInterfaceId())
            .append("ifIndex", getIfIndex())
            .append("ipAddress", getAddress())
            .append("ipHostname", getIpHostname())
            .append("ifType", getIfType())
            .append("ifDescr", getIfDescr())
            .append("ifName", getIfName())
            .append("ifAlias", getIfAlias())
            .append("status", getStatus())
            .append("collect", getCollectFlag())
            .toString();
    }

    public int compareTo(SnmpManagedInterface obj) {
        return new CompareToBuilder()
            .append(getSnmpInterfaceId(), obj.getSnmpInterfaceId())
            .append(getIfIndex(), obj.getIfIndex())
            .append(getAddress(), obj.getAddress())
            .append(getIpHostname(), obj.getIpHostname())
            .append(getIfType(), obj.getIfType())
            .append(getIfDescr(), obj.getIfDescr())
            .append(getIfName(), obj.getIfName())
            .append(getIfAlias(), obj.getIfAlias())
            .append(getStatus(), obj.getStatus())
            .append(getCollectFlag(), obj.getCollectFlag())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SnmpManagedInterface) {
            SnmpManagedInterface other = (SnmpManagedInterface) obj;
            return new EqualsBuilder()
                .append(getSnmpInterfaceId(), other.getSnmpInterfaceId())
                .append(getIfIndex(), other.getIfIndex())
                .append(getAddress(), other.getAddress())
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(617, 2677)
            .append(getSnmpInterfaceId())
            .append(getIfIndex())
            .append(getAddress())
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
