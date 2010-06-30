//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/**
 * 
 */
package org.opennms.secret.model;

import java.util.Date;
import java.util.List;

/**
 * <p>Node class.</p>
 *
 * @author Ted Kazmarak
 * @author David Hustace
 * @hibernate.class table="node"
 * @version $Id: $
 * @since 1.6.12
 */
public class Node {
	
	Long nodeId;
	String dpName;
	Date nodeCreateTime;
	Long nodeParentId;
	String nodeType;
	String nodeSysOid;
	String nodeSysName;
	String nodeSysDescription;
	String nodeSysLocation;
	String nodeSysContact;
	String nodeLabel;
	String nodeLabelSource;
	String nodeNetBiosName;
	String nodeDomainName;
	String operatingSystem;
	Date lastCapsdPoll;
    List dataSources;
    NodeInterface[] interfaces;
	
	
	/**
	 * <p>Getter for the field <code>dpName</code>.</p>
	 *
	 * @return Returns the dbName.
	 */
	public String getDpName() {
		return dpName;
	}
	
	/**
	 * <p>Setter for the field <code>dpName</code>.</p>
	 *
	 * @param dbName The dbName to set.
	 */
	public void setDpName(String dbName) {
		this.dpName = dbName;
	}
	
	/**
	 * <p>Getter for the field <code>lastCapsdPoll</code>.</p>
	 *
	 * @return Returns the lastCapsdPoll.
	 */
	public Date getLastCapsdPoll() {
		return lastCapsdPoll;
	}
	
	/**
	 * <p>Setter for the field <code>lastCapsdPoll</code>.</p>
	 *
	 * @param lastCapsdPoll The lastCapsdPoll to set.
	 */
	public void setLastCapsdPoll(Date lastCapsdPoll) {
		this.lastCapsdPoll = lastCapsdPoll;
	}
	
	/**
	 * <p>Getter for the field <code>nodeCreateTime</code>.</p>
	 *
	 * @return Returns the nodeCreateTime.
	 */
	public Date getNodeCreateTime() {
		return nodeCreateTime;
	}
	
	/**
	 * <p>Setter for the field <code>nodeCreateTime</code>.</p>
	 *
	 * @param nodeCreateTime The nodeCreateTime to set.
	 */
	public void setNodeCreateTime(Date nodeCreateTime) {
		this.nodeCreateTime = nodeCreateTime;
	}
	
	/**
	 * <p>Getter for the field <code>nodeDomainName</code>.</p>
	 *
	 * @return Returns the nodeDomainName.
	 */
	public String getNodeDomainName() {
		return nodeDomainName;
	}
	
	/**
	 * <p>Setter for the field <code>nodeDomainName</code>.</p>
	 *
	 * @param nodeDomainName The nodeDomainName to set.
	 */
	public void setNodeDomainName(String nodeDomainName) {
		this.nodeDomainName = nodeDomainName;
	}
	
	/**
	 * <p>Getter for the field <code>nodeId</code>.</p>
	 *
	 * @return Returns the nodeId.
	 * @hibernate.id generator-class="native"
	 */
	public Long getNodeId() {
		return nodeId;
	}
    
    /**
     * <p>Setter for the field <code>nodeId</code>.</p>
     *
     * @param nodeId a {@link java.lang.Long} object.
     */
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * <p>Getter for the field <code>nodeLabel</code>.</p>
     *
     * @return Returns the nodeLabel.
     */
    public String getNodeLabel() {
		return nodeLabel;
	}
    
	/**
	 * <p>Setter for the field <code>nodeLabel</code>.</p>
	 *
	 * @param nodeLabel The nodeLabel to set.
	 */
	public void setNodeLabel(String nodeLabel) {
		this.nodeLabel = nodeLabel;
	}
    
	/**
	 * <p>Getter for the field <code>nodeLabelSource</code>.</p>
	 *
	 * @return Returns the nodeLabelSource.
	 */
	public String getNodeLabelSource() {
		return nodeLabelSource;
	}
    
	/**
	 * <p>Setter for the field <code>nodeLabelSource</code>.</p>
	 *
	 * @param nodeLabelSource The nodeLabelSource to set.
	 */
	public void setNodeLabelSource(String nodeLabelSource) {
		this.nodeLabelSource = nodeLabelSource;
	}
    
	/**
	 * <p>Getter for the field <code>nodeNetBiosName</code>.</p>
	 *
	 * @return Returns the nodeNetBiosName.
	 */
	public String getNodeNetBiosName() {
		return nodeNetBiosName;
	}
    
	/**
	 * <p>Setter for the field <code>nodeNetBiosName</code>.</p>
	 *
	 * @param nodeNetBiosName The nodeNetBiosName to set.
	 */
	public void setNodeNetBiosName(String nodeNetBiosName) {
		this.nodeNetBiosName = nodeNetBiosName;
	}
    
	/**
	 * <p>Getter for the field <code>nodeParentId</code>.</p>
	 *
	 * @return Returns the nodeParentId.
	 */
	public Long getNodeParentId() {
		return nodeParentId;
	}
    
	/**
	 * <p>Setter for the field <code>nodeParentId</code>.</p>
	 *
	 * @param nodeParentId The nodeParentId to set.
	 */
	public void setNodeParentId(Long nodeParentId) {
		this.nodeParentId = nodeParentId;
	}
    
	/**
	 * <p>Getter for the field <code>nodeSysContact</code>.</p>
	 *
	 * @return Returns the nodeSysContact.
	 */
	public String getNodeSysContact() {
		return nodeSysContact;
	}
    
	/**
	 * <p>Setter for the field <code>nodeSysContact</code>.</p>
	 *
	 * @param nodeSysContact The nodeSysContact to set.
	 */
	public void setNodeSysContact(String nodeSysContact) {
		this.nodeSysContact = nodeSysContact;
	}
    
	/**
	 * <p>Getter for the field <code>nodeSysDescription</code>.</p>
	 *
	 * @return Returns the nodeSysDescription.
	 */
	public String getNodeSysDescription() {
		return nodeSysDescription;
	}
    
	/**
	 * <p>Setter for the field <code>nodeSysDescription</code>.</p>
	 *
	 * @param nodeSysDescription The nodeSysDescription to set.
	 */
	public void setNodeSysDescription(String nodeSysDescription) {
		this.nodeSysDescription = nodeSysDescription;
	}
    
	/**
	 * <p>Getter for the field <code>nodeSysLocation</code>.</p>
	 *
	 * @return Returns the nodeSysLocation.
	 */
	public String getNodeSysLocation() {
		return nodeSysLocation;
	}
    
	/**
	 * <p>Setter for the field <code>nodeSysLocation</code>.</p>
	 *
	 * @param nodeSysLocation The nodeSysLocation to set.
	 */
	public void setNodeSysLocation(String nodeSysLocation) {
		this.nodeSysLocation = nodeSysLocation;
	}
    
	/**
	 * <p>Getter for the field <code>nodeSysName</code>.</p>
	 *
	 * @return Returns the nodeSysName.
	 */
	public String getNodeSysName() {
		return nodeSysName;
	}
    
	/**
	 * <p>Setter for the field <code>nodeSysName</code>.</p>
	 *
	 * @param nodeSysName The nodeSysName to set.
	 */
	public void setNodeSysName(String nodeSysName) {
		this.nodeSysName = nodeSysName;
	}
    
	/**
	 * <p>Getter for the field <code>nodeSysOid</code>.</p>
	 *
	 * @return Returns the nodeSysOid.
	 */
	public String getNodeSysOid() {
		return nodeSysOid;
	}
    
	/**
	 * <p>Setter for the field <code>nodeSysOid</code>.</p>
	 *
	 * @param nodeSysOid The nodeSysOid to set.
	 */
	public void setNodeSysOid(String nodeSysOid) {
		this.nodeSysOid = nodeSysOid;
	}
    
	/**
	 * <p>Getter for the field <code>nodeType</code>.</p>
	 *
	 * @return Returns the nodeType.
	 */
	public String getNodeType() {
		return nodeType;
	}
    
	/**
	 * <p>Setter for the field <code>nodeType</code>.</p>
	 *
	 * @param nodeType The nodeType to set.
	 */
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
    
	/**
	 * <p>Getter for the field <code>operatingSystem</code>.</p>
	 *
	 * @return Returns the operatingSystem.
	 */
	public String getOperatingSystem() {
		return operatingSystem;
	}
    
	/**
	 * <p>Setter for the field <code>operatingSystem</code>.</p>
	 *
	 * @param operatingSystem The operatingSystem to set.
	 */
	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
    
    
    /**
     * <p>hasPerformanceDataSource</p>
     *
     * @return a boolean.
     */
    public boolean hasPerformanceDataSource() {
        return true;
        
    }
/*    
    public List getDataSources() {
        return dataSources;
    }
    
    public void setDataSources(List dataSources) {
        this.dataSources = dataSources;
    }
    
    public NodeInterface[] getInterfaces() {
        return interfaces;
    }
    
    public void setInterfaces(NodeInterface[] interfaces) {
        this.interfaces = interfaces;
    }
    */
}
