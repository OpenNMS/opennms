/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

/*
 * Created on 9-mar-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * <p>BridgeStpInterface class.</p>
 *
 * @author antonio
 */
@XmlRootElement(name = "stpInterface")
@Entity
@Table(name="stpInterface", uniqueConstraints = {@UniqueConstraint(columnNames={"nodeId", "bridgePort", "stpVlan"})})
public class OnmsStpInterface {

    private Integer m_id;
	private OnmsNode m_node;
	private Integer m_bridgePort;
	private Integer m_ifIndex = -1;
	private Integer m_stpPortState;
	private Integer m_stpPortPathCost;
	private String m_stpPortDesignatedRoot;
	private Integer m_stpPortDesignatedCost;
	private String m_stpPortDesignatedBridge;
	private String m_stpPortDesignatedPort;
	private Character m_status;
	private Date m_lastPollTime;
	private Integer m_vlan;

	public OnmsStpInterface() {
	}

	public OnmsStpInterface(final Integer bridgePort, final Integer vlanIndex) {
		m_bridgePort = bridgePort;
		m_vlan = vlanIndex;
	}

    public OnmsStpInterface(final OnmsNode node, final Integer bridgePort, final Integer vlanIndex) {
        m_node = node;
        m_bridgePort = bridgePort;
        m_vlan = vlanIndex;
    }

    @Id
    @Column(nullable=false)
    @XmlTransient
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    
    @XmlID
    @XmlAttribute(name="id")
    @Transient
    public String getInterfaceId() {
        return getId().toString();
    }

    public void setId(final Integer id) {
        m_id = id;
    }
    
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    @XmlElement(name="nodeId")
    @XmlIDREF
    public OnmsNode getNode() {
        return m_node;
    }

    public void setNode(final OnmsNode node) {
        m_node = node;
    }

    @XmlAttribute
    @Column(nullable=false)
	public Integer getIfIndex() {
		return m_ifIndex;
	}

	public void setIfIndex(final Integer ifIndex) {
		m_ifIndex = ifIndex;
	}

	@XmlElement
	@Column
	public Integer getStpPortState() {
		return m_stpPortState;
	}

	public void setStpPortState(final Integer stpPortState) {
		m_stpPortState = stpPortState;
	}

	@XmlElement
	@Column
	public Integer getStpPortPathCost() {
		return m_stpPortPathCost;
	}

	public void setStpPortPathCost(final Integer stpPortPathCost) {
		m_stpPortPathCost = stpPortPathCost;
	}

	@XmlElement
	@Column(length=16)
	public String getStpPortDesignatedRoot() {
		return m_stpPortDesignatedRoot;
	}

	public void setStpPortDesignatedRoot(final String stpPortDesignatedRoot) {
		m_stpPortDesignatedRoot = stpPortDesignatedRoot;
	}

	@XmlElement
	@Column
	public Integer getStpPortDesignatedCost() {
		return m_stpPortDesignatedCost;
	}

	public void setStpPortDesignatedCost(final Integer stpPortDesignatedCost) {
		m_stpPortDesignatedCost = stpPortDesignatedCost;
	}

	@XmlAttribute
	@Column(nullable=false)
	public Character getStatus() {
		return m_status;
	}

	public void setStatus(final Character status) {
		m_status = status;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable=false)
    @XmlElement
	public Date getLastPollTime() {
		return m_lastPollTime;
	}

	public void setLastPollTime(final Date lastPollTime) {
		m_lastPollTime = lastPollTime;
	}

	@XmlElement
	@Column(nullable=false)
	public Integer getBridgePort() {
		return m_bridgePort;
	}

	public void setBridgePort(final Integer bridgePort) {
		m_bridgePort = bridgePort;
	}

	@XmlElement
	@Column(length=16)
	public String getStpPortDesignatedBridge() {
		return m_stpPortDesignatedBridge;
	}

	public void setStpPortDesignatedBridge(final String stpPortDesignatedBridge) {
		m_stpPortDesignatedBridge = stpPortDesignatedBridge;
	}

	@XmlElement
	@Column(length=4)
	public String getStpPortDesignatedPort() {
		return m_stpPortDesignatedPort;
	}

	public void setStpPortDesignatedPort(final String stpPortDesignatedPort) {
		m_stpPortDesignatedPort = stpPortDesignatedPort;
	}

	@XmlElement(name="stpVlan")
	@Column(name="stpVlan", nullable=false)
	public Integer getVlan() {
		return m_vlan;
	}
	
	public void setVlan(final Integer vlan) {
		m_vlan = vlan;
	}
}

