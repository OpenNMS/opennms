/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name="bridgeBridgeLink")
public class BridgeBridgeLink {

	private Integer m_id;
	private OnmsNode m_node;
	private Integer m_bridgePort;
	private Integer m_bridgePortIfIndex;
	private String  m_bridgePortIfName;
	private Integer m_vlan;
	private OnmsNode m_designatedNode;
	private Integer m_designatedPort;
	private Integer m_designatedPortIfIndex;
	private String  m_designatedPortIfName;
	private Integer m_designatedVlan;
    private Date m_bridgeBridgeLinkCreateTime = new Date();
    private Date m_bridgeBridgeLinkLastPollTime;
	
	public BridgeBridgeLink() {}


    @Id
    @Column(nullable = false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
	public Integer getId() {
		return m_id;
	}


	public void setId(Integer id) {
		m_id = id;
	}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
	public OnmsNode getNode() {
		return m_node;
	}


	public void setNode(OnmsNode node) {
		m_node = node;
	}

    @Column(name="bridgePort", nullable = true)
	public Integer getBridgePort() {
		return m_bridgePort;
	}

	public void setBridgePort(Integer bridgePort) {
		m_bridgePort = bridgePort;
	}

    @Column(name="bridgePortIfIndex", nullable = true)
	public Integer getBridgePortIfIndex() {
		return m_bridgePortIfIndex;
	}


	public void setBridgePortIfIndex(Integer bridgePortIfIndex) {
		m_bridgePortIfIndex = bridgePortIfIndex;
	}


    @Column(name = "bridgePortIfName", length = 32, nullable = true)
	public String getBridgePortIfName() {
		return m_bridgePortIfName;
	}


	public void setBridgePortIfName(String bridgePortIfName) {
		m_bridgePortIfName = bridgePortIfName;
	}

	@Column(name="vlan", nullable = true)
	public Integer getVlan() {
		return m_vlan;
	}


	public void setVlan(Integer vlan) {
		m_vlan = vlan;
	}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designatednodeId", referencedColumnName="nodeId")
	public OnmsNode getDesignatedNode() {
		return m_designatedNode;
	}


	public void setDesignatedNode(OnmsNode designatedNode) {
		m_designatedNode = designatedNode;
	}

    @Column(name="designatedBridgePort", nullable = true)
	public Integer getDesignatedPort() {
		return m_designatedPort;
	}

	public void setDesignatedPort(Integer bridgePort) {
		m_designatedPort = bridgePort;
	}

    @Column(name="designatedBridgePortIfIndex", nullable = true)
	public Integer getDesignatedPortIfIndex() {
		return m_designatedPortIfIndex;
	}


	public void setDesignatedPortIfIndex(Integer bridgePortIfIndex) {
		m_designatedPortIfIndex = bridgePortIfIndex;
	}


    @Column(name = "designatedBridgePortIfName", length = 32, nullable = true)
	public String getDesignatedPortIfName() {
		return m_designatedPortIfName;
	}


	public void setDesignatedPortIfName(String bridgePortIfName) {
		m_designatedPortIfName = bridgePortIfName;
	}

	@Column(name="designatedVlan", nullable = true)
	public Integer getDesignatedVlan() {
		return m_designatedVlan;
	}


	public void setDesignatedVlan(Integer vlan) {
		m_designatedVlan = vlan;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="bridgeBridgeLinkCreateTime", nullable=false)
	public Date getBridgeBridgeLinkCreateTime() {
		return m_bridgeBridgeLinkCreateTime;
	}


	public void setBridgeBridgeLinkCreateTime(Date bridgeLinkCreateTime) {
		m_bridgeBridgeLinkCreateTime = bridgeLinkCreateTime;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="bridgeBridgeLinkLastPollTime", nullable=false)
	public Date getBridgeBridgeLinkLastPollTime() {
		return m_bridgeBridgeLinkLastPollTime;
	}


	public void setBridgeBridgeLinkLastPollTime(Date bridgeLinkLastPollTime) {
		m_bridgeBridgeLinkLastPollTime = bridgeLinkLastPollTime;
	}
	
	@Override
	public String toString() {
		Integer designatedNodeid = null;
		if (m_designatedNode != null)
			designatedNodeid = m_designatedNode.getId();
		return new ToStringBuilder(this)
				.append("Nodeid", m_node.getId())
				.append("bridgePort", m_bridgePort)
				.append("bridgePortIfIndex", m_bridgePortIfIndex)
				.append("bridgePortIfName", m_bridgePortIfName)
				.append("vlan", m_vlan)
                .append("Nodeid", designatedNodeid)
				.append("designatedPort", m_designatedPort)
				.append("designatedPortIfIndex", m_designatedPortIfIndex)
				.append("designatedPortIfName", m_designatedPortIfName)
				.append("designatedVlan", m_designatedVlan)
				.append("m_bridgeBridgeLinkCreateTime", m_bridgeBridgeLinkCreateTime)
				.append("m_bridgeBridgeLinkLastPollTime", m_bridgeBridgeLinkLastPollTime)
				.toString();
	}
	
	public void merge(BridgeBridgeLink element) {
		if (element == null)
			return;
		
		setBridgePortIfIndex(element.getBridgePortIfIndex());
		setBridgePortIfName(element.getBridgePortIfName());
		setVlan(element.getVlan());

		setDesignatedNode(element.getDesignatedNode());
		setDesignatedPort(element.getDesignatedPort());
		setDesignatedPortIfIndex(element.getDesignatedPortIfIndex());
		setDesignatedPortIfName(element.getDesignatedPortIfName());
		setDesignatedVlan(element.getDesignatedVlan());
		if (element.getBridgeBridgeLinkLastPollTime() == null)
		    setBridgeBridgeLinkLastPollTime(element.getBridgeBridgeLinkCreateTime());
		else
		   setBridgeBridgeLinkLastPollTime(element.getBridgeBridgeLinkLastPollTime()); 
	}

	@Transient
	public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();

    	strbfr.append("bridge link:[nodeid:["); 
    	strbfr.append(getNode().getId());
    	strbfr.append("], bridgeport:");
    	strbfr.append(getBridgePort());
    	strbfr.append(", designatednodeid:[");
    	strbfr.append(getDesignatedNode().getId());
    	strbfr.append("], designatedbridgeport: ");
    	strbfr.append(getDesignatedPort());
        strbfr.append("]\n");

    	return strbfr.toString();
	}
	
	@Transient
	public BridgeBridgeLink getReverseBridgeBridgeLink() {
		
		BridgeBridgeLink link = new BridgeBridgeLink();
		link.setNode(getDesignatedNode());
		link.setBridgePort(getDesignatedPort());
		link.setBridgePortIfIndex(getDesignatedPortIfIndex());
		link.setBridgePortIfName(getDesignatedPortIfName());
		link.setVlan(getDesignatedVlan());
		
		link.setDesignatedNode(getNode());
		link.setDesignatedPort(getBridgePort());
		link.setDesignatedPortIfIndex(getBridgePortIfIndex());
		link.setDesignatedPortIfName(getBridgePortIfName());
		link.setDesignatedVlan(getVlan());

		return link;
	}
	
}
