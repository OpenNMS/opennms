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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.core.style.ToStringCreator;


/** 
 * Contains information on nodes discovered and potentially managed by OpenNMS.  
 * sys* properties map to SNMP MIB 2 system table information.
 * 
 * @hibernate.class table="node"
 *     
*/
@Entity
@Table(name="node")
public class OnmsNode extends OnmsEntity implements Serializable {

    private static final long serialVersionUID = -5736397583719151493L;

    /** identifier field */
    private Integer m_id;

    /** persistent field */
    private Date m_createTime = new Date();

    /** nullable persistent field */
    private OnmsNode m_parent;

    /** nullable persistent field */
    private String m_type;

    /** nullable persistent field */
    private String m_sysObjectId;

    /** nullable persistent field */
    private String m_sysName;

    /** nullable persistent field */
    private String m_sysDescription;

    /** nullable persistent field */
    private String m_sysLocation;

    /** nullable persistent field */
    private String m_sysContact;

    /** nullable persistent field */
    private String m_label;

    /** nullable persistent field */
    private String m_labelSource;

    /** nullable persistent field */
    private String m_netBiosName;

    /** nullable persistent field */
    private String m_netBiosDomain;

    /** nullable persistent field */
    private String m_operatingSystem;

    /** nullable persistent field */
    private Date m_lastCapsdPoll;

    /** persistent field */
    private OnmsDistPoller m_distPoller;

    /** persistent field */
    private OnmsAssetRecord m_assetRecord;

    /** persistent field */
    private Set<OnmsIpInterface> m_ipInterfaces = new LinkedHashSet<OnmsIpInterface>();

    /** persistent field */
    private Set<OnmsSnmpInterface> m_snmpInterfaces = new LinkedHashSet<OnmsSnmpInterface>();
    
    private Set<OnmsCategory> m_categories = new LinkedHashSet<OnmsCategory>();

    public OnmsNode() {
        this(null);
    }

    public OnmsNode(OnmsDistPoller distPoller) {
        m_distPoller = distPoller;
        m_assetRecord = new OnmsAssetRecord();
        m_assetRecord.setNode(this);
    }

    /**
     * Unique identifier for node.
     * 
     */
    @Id
    @Column(name="nodeId")
    @SequenceGenerator(name="nodeSequence", sequenceName="nodeNxtId")
    @GeneratedValue(generator="nodeSequence")
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer nodeid) {
        m_id = nodeid;
    }

    /** 
     * Time node was added to the database.
     * 
     * @hibernate.property column="nodecreatetime" length="8" not-null="true"
     *         
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="nodeCreateTime", nullable=false)
    public Date getCreateTime() {
        return m_createTime;
    }

    public void setCreateTime(Date nodecreatetime) {
        m_createTime = nodecreatetime;
    }

    /** 
     * In the case that the node is virtual or an independent device in a chassis
     * that should be reflected as a subcomponent or "child", this field reflects 
     * the nodeID of the chassis/physical node/"parent" device.
     * 
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeParentID")
    public OnmsNode getParent() {
        return m_parent;
    }

    /**
     * 
     */
    public void setParent(OnmsNode parent) {
        m_parent = parent;
    }

    /** 
     * Flag indicating status of node
     * - 'A' - active
     * - 'D' - deleted
     * 
     * TODO: Eventually this will be deprecated and deleted nodes will actually be deleted.
     * 
     */
    @Column(name="nodeType", length=1)
    public String getType() {
        return m_type;
    }

    public void setType(String nodetype) {
        m_type = nodetype;
    }

    /** 
     * SNMP MIB-2 system.sysObjectID.0
     */
    @Column(name="nodeSysOID", length=256)
    public String getSysObjectId() {
        return m_sysObjectId;
    }

    public void setSysObjectId(String nodesysoid) {
        m_sysObjectId = nodesysoid;
    }

    /** 
     * SNMP MIB-2 system.sysName.0
     * 
     */
    @Column(name="nodeSysName", length=256)
    public String getSysName() {
        return m_sysName;
    }

    public void setSysName(String nodesysname) {
        m_sysName = nodesysname;
    }

    /** 
     * SNMP MIB-2 system.sysDescr.0
     */
    @Column(name="nodeSysDescription", length=256)
    public String getSysDescription() {
        return m_sysDescription;
    }

    public void setSysDescription(String nodesysdescription) {
        m_sysDescription = nodesysdescription;
    }

    /** 
     * SNMP MIB-2 system.sysLocation.0
     */
    @Column(name="nodeSysLocation", length=256)
    public String getSysLocation() {
        return m_sysLocation;
    }

    public void setSysLocation(String nodesyslocation) {
        m_sysLocation = nodesyslocation;
    }

    /** 
     * SNMP MIB-2 system.sysContact.0
     */
    @Column(name="nodeSysContact", length=256)
    public String getSysContact() {
        return m_sysContact;
    }

    public void setSysContact(String nodesyscontact) {
        m_sysContact = nodesyscontact;
    }

    /** 
     * User-friendly name associated with the node.
     */
    @Column(name="nodeLabel", length=256)
    public String getLabel() {
        return m_label;
    }

    public void setLabel(String nodelabel) {
        m_label = nodelabel;
    }

    /** 
     * Flag indicating source of nodeLabel
     * - 'U' = user defined
     * - 'H' = IP hostname
     * - 'S' = sysName
     * - 'A' = IP address
     * 
     * TODO: change this to an enum
     */
    @Column(name="nodeLabelSource", length=1)
    public String getLabelSource() {
        return m_labelSource;
    }

    public void setLabelSource(String nodelabelsource) {
        m_labelSource = nodelabelsource;
    }

    /** 
     * NetBIOS workstation name associated with the node.
     */
    @Column(name="nodeNetBIOSName", length=16)
    public String getNetBiosName() {
        return m_netBiosName;
    }

    public void setNetBiosName(String nodenetbiosname) {
        m_netBiosName = nodenetbiosname;
    }

    /**
     * NetBIOS damain name associated with the node.
     */
    @Column(name="nodeDomainName", length=16)
    public String getNetBiosDomain() {
        return m_netBiosDomain;
    }

    public void setNetBiosDomain(String nodedomainname) {
        m_netBiosDomain = nodedomainname;
    }

    /** 
     * Operating system running on the node.
     */
    @Column(name="operatingSystem", length=64)
    public String getOperatingSystem() {
        return m_operatingSystem;
    }

    public void setOperatingSystem(String operatingsystem) {
        m_operatingSystem = operatingsystem;
    }

    /** 
     * Date and time of last Capsd scan.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastCapsdPoll")
    public Date getLastCapsdPoll() {
        return m_lastCapsdPoll;
    }

    public void setLastCapsdPoll(Date lastcapsdpoll) {
        m_lastCapsdPoll = lastcapsdpoll;
    }

    /**
     * Distributed Poller responsible for this node
     * 
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="dpName")
    public OnmsDistPoller getDistPoller() {
        return m_distPoller;
    }

    public void setDistPoller(org.opennms.netmgt.model.OnmsDistPoller distpoller) {
        m_distPoller = distpoller;
    }
    
    /** 
     * The assert record associated with this node
     */
    @OneToOne(mappedBy="node", cascade = CascadeType.ALL)
    public OnmsAssetRecord getAssetRecord() {
        return m_assetRecord;
    }

    public void setAssetRecord(OnmsAssetRecord asset) {
        m_assetRecord = asset;
    }


    /** 
     * The interfaces on this node
     * 
     */
    @OneToMany(mappedBy="node", cascade=CascadeType.ALL)
    public Set<OnmsIpInterface> getIpInterfaces() {
        return m_ipInterfaces;
    }

    public void setIpInterfaces(Set<OnmsIpInterface> ipinterfaces) {
        m_ipInterfaces = ipinterfaces;
    }
    
    public void addIpInterface(OnmsIpInterface iface) {
    	iface.setNode(this);
    	getIpInterfaces().add(iface);
    }

    /**
     * The information from the SNMP interfaces/ipAddrTables for the node
     *  
     */
    @OneToMany(mappedBy="node", cascade=CascadeType.ALL)
    public Set<OnmsSnmpInterface> getSnmpInterfaces() {
        return m_snmpInterfaces;
    }

    public void setSnmpInterfaces(Set<OnmsSnmpInterface> snmpinterfaces) {
        m_snmpInterfaces = snmpinterfaces;
    }
    
    @ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
    		name="category_node",
    		joinColumns={@JoinColumn(name="nodeId")},
    		inverseJoinColumns={@JoinColumn(name="categoryId")}
    )
    public Set<OnmsCategory> getCategories() {
        return m_categories;
    }
    
    public void setCategories(Set<OnmsCategory> categories) {
        m_categories = categories;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("id", getId())
            .append("label", getLabel())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		visitor.visitNode(this);
		
		for (Iterator it = getIpInterfaces().iterator(); it.hasNext();) {
			OnmsIpInterface iface = (OnmsIpInterface) it.next();
			iface.visit(visitor);
		}
		
		for (Iterator it = getSnmpInterfaces().iterator(); it.hasNext();) {
			OnmsSnmpInterface snmpIface = (OnmsSnmpInterface) it.next();
			snmpIface.visit(visitor);
		}
		
		visitor.visitNodeComplete(this);
	}

	public void addSnmpInterface(OnmsSnmpInterface snmpIface) {
    	snmpIface.setNode(this);
    	getSnmpInterfaces().add(snmpIface);
	}

	@Transient
    public boolean isDown() {
        boolean down = true;
        for (Iterator it = m_ipInterfaces.iterator(); it.hasNext();) {
            OnmsIpInterface ipIf = (OnmsIpInterface) it.next();
            if (!ipIf.isDown()) {
                return !down;
            }
        }
        return down;
    }

}
