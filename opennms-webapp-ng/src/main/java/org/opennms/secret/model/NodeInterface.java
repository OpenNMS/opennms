//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.secret.model;

import java.util.Date;

/**

 * @author David Hustace
 * 
 * @hibernate.class table="Ipinterface"
 *
 */
public class NodeInterface {
    
    Long id;
    Long nodeId;
    String ipAddr;
    Long ifIndex;
    String ipHostName;
    String isManaged;
    Long ipStatus;
    Date ipLastCapsdPoll;
    String isSnmpPrimary;
    DataSource[] dataSources;
    InterfaceService[] services;
    
    /**
     * @return Returns the id.
     * @hibernate.id generator-class="native"
     */
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
 
    /**
     * @return Returns the ifIndex.
     * @hibernate.property
     */
    public Long getIfIndex() {
        return ifIndex;
    }
    /**
     * @param ifIndex The ifIndex to set.
     */
    public void setIfIndex(Long ifIndex) {
        this.ifIndex = ifIndex;
    }
    /**
     * @return Returns the ipAddr.
     * @hibernate.property
     */
    public String getIpAddr() {
        return ipAddr;
    }
    /**
     * @param ipAddr The ipAddr to set.
     */
    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }
    /**
     * @return Returns the ipHostName.
     * @hibernate.property
     */
    public String getIpHostName() {
        return ipHostName;
    }
    /**
     * @param ipHostName The ipHostName to set.
     */
    public void setIpHostName(String ipHostName) {
        this.ipHostName = ipHostName;
    }
    /**
     * @return Returns the ipLastCapsdPoll.
     * @hibernate.property
     */
    public Date getIpLastCapsdPoll() {
        return ipLastCapsdPoll;
    }
    /**
     * @param ipLastCapsdPoll The ipLastCapsdPoll to set.
     */
    public void setIpLastCapsdPoll(Date ipLastCapsdPoll) {
        this.ipLastCapsdPoll = ipLastCapsdPoll;
    }
    /**
     * @return Returns the ipStatus.
     * @hibernate.property
     */
    public Long getIpStatus() {
        return ipStatus;
    }
    /**
     * @param ipStatus The ipStatus to set.
     */
    public void setIpStatus(Long ipStatus) {
        this.ipStatus = ipStatus;
    }
    /**
     * @return Returns the isManaged.
     * @hibernate.property
     */
    public String getIsManaged() {
        return isManaged;
    }
    /**
     * @param isManaged The isManaged to set.
     */
    public void setIsManaged(String isManaged) {
        this.isManaged = isManaged;
    }
    /**
     * @return Returns the isSnmpPrimary.
     * @hibernate.property
     */
    public String getIsSnmpPrimary() {
        return isSnmpPrimary;
    }
    /**
     * @param isSnmpPrimary The isSnmpPrimary to set.
     */
    public void setIsSnmpPrimary(String isSnmpPrimary) {
        this.isSnmpPrimary = isSnmpPrimary;
    }
    /**
     * @return Returns the nodeId.
     * @hibernate.property
     */
    public Long getNodeId() {
        return nodeId;
    }
    /**
     * @param nodeId The nodeId to set.
     */
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public boolean hasPerformanceDataSource() {
        return true;
        
    }
}
