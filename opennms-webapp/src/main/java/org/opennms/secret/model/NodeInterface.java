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
 * <p>NodeInterface class.</p>
 *
 * @author David Hustace
 * @hibernate.class table="Ipinterface"
 * @version $Id: $
 * @since 1.6.12
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
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return Returns the id.
     * @hibernate.id generator-class="native"
     */
    public Long getId() {
        return id;
    }
    
    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id a {@link java.lang.Long} object.
     */
    public void setId(Long id) {
        this.id = id;
    }
 
    /**
     * <p>Getter for the field <code>ifIndex</code>.</p>
     *
     * @return Returns the ifIndex.
     */
    public Long getIfIndex() {
        return ifIndex;
    }
    /**
     * <p>Setter for the field <code>ifIndex</code>.</p>
     *
     * @param ifIndex The ifIndex to set.
     */
    public void setIfIndex(Long ifIndex) {
        this.ifIndex = ifIndex;
    }
    /**
     * <p>Getter for the field <code>ipAddr</code>.</p>
     *
     * @return Returns the ipAddr.
     */
    public String getIpAddr() {
        return ipAddr;
    }
    /**
     * <p>Setter for the field <code>ipAddr</code>.</p>
     *
     * @param ipAddr The ipAddr to set.
     */
    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }
    /**
     * <p>Getter for the field <code>ipHostName</code>.</p>
     *
     * @return Returns the ipHostName.
     */
    public String getIpHostName() {
        return ipHostName;
    }
    /**
     * <p>Setter for the field <code>ipHostName</code>.</p>
     *
     * @param ipHostName The ipHostName to set.
     */
    public void setIpHostName(String ipHostName) {
        this.ipHostName = ipHostName;
    }
    /**
     * <p>Getter for the field <code>ipLastCapsdPoll</code>.</p>
     *
     * @return Returns the ipLastCapsdPoll.
     */
    public Date getIpLastCapsdPoll() {
        return ipLastCapsdPoll;
    }
    /**
     * <p>Setter for the field <code>ipLastCapsdPoll</code>.</p>
     *
     * @param ipLastCapsdPoll The ipLastCapsdPoll to set.
     */
    public void setIpLastCapsdPoll(Date ipLastCapsdPoll) {
        this.ipLastCapsdPoll = ipLastCapsdPoll;
    }
    /**
     * <p>Getter for the field <code>ipStatus</code>.</p>
     *
     * @return Returns the ipStatus.
     */
    public Long getIpStatus() {
        return ipStatus;
    }
    /**
     * <p>Setter for the field <code>ipStatus</code>.</p>
     *
     * @param ipStatus The ipStatus to set.
     */
    public void setIpStatus(Long ipStatus) {
        this.ipStatus = ipStatus;
    }
    /**
     * <p>Getter for the field <code>isManaged</code>.</p>
     *
     * @return Returns the isManaged.
     */
    public String getIsManaged() {
        return isManaged;
    }
    /**
     * <p>Setter for the field <code>isManaged</code>.</p>
     *
     * @param isManaged The isManaged to set.
     */
    public void setIsManaged(String isManaged) {
        this.isManaged = isManaged;
    }
    /**
     * <p>Getter for the field <code>isSnmpPrimary</code>.</p>
     *
     * @return Returns the isSnmpPrimary.
     */
    public String getIsSnmpPrimary() {
        return isSnmpPrimary;
    }
    /**
     * <p>Setter for the field <code>isSnmpPrimary</code>.</p>
     *
     * @param isSnmpPrimary The isSnmpPrimary to set.
     */
    public void setIsSnmpPrimary(String isSnmpPrimary) {
        this.isSnmpPrimary = isSnmpPrimary;
    }
    /**
     * <p>Getter for the field <code>nodeId</code>.</p>
     *
     * @return Returns the nodeId.
     */
    public Long getNodeId() {
        return nodeId;
    }
    /**
     * <p>Setter for the field <code>nodeId</code>.</p>
     *
     * @param nodeId The nodeId to set.
     */
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * <p>hasPerformanceDataSource</p>
     *
     * @return a boolean.
     */
    public boolean hasPerformanceDataSource() {
        return true;
        
    }
}
