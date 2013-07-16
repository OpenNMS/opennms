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

package org.opennms.netmgt.model;

import java.net.InetAddress;
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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.opennms.core.xml.bind.InetAddressXmlAdapter;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;

/**
 * <p>AtInterface class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
@XmlRootElement(name = "atInterface")
@Entity
@Table(name="atInterface", uniqueConstraints = {@UniqueConstraint(columnNames={"nodeId", "ipAddr", "atPhysAddr"})})
public class OnmsAtInterface {
	private Integer m_id;
	private OnmsNode m_node;
	private InetAddress m_ipAddress;
	private String m_macAddress;
	private StatusType m_status = StatusType.UNKNOWN;
	private Integer m_sourceNodeId;
	private Integer m_ifIndex;
	private Date m_lastPollTime;

	OnmsAtInterface() {
	}

	/**
	 * <p>Constructor for AtInterface.</p>
	 * @param node TODO
	 * @param ipAddress a {@link java.lang.String} object.
	 * @param macAddress a {@link java.lang.String} object.
	 */
	public OnmsAtInterface(final OnmsNode node, final InetAddress ipAddress, final String macAddress) {
	    m_node = node;
		m_macAddress = macAddress;
		m_ipAddress = ipAddress;
		m_ifIndex=-1;
	}

	/**
	 * <p>Constructor for AtInterface.</p>
	 * @param node TODO
	 * @param ipAddress a {@link java.lang.String} object.
	 */
	public OnmsAtInterface(final OnmsNode node, final InetAddress ipAddress) {
	    m_node = node;
		m_macAddress = "";
		m_ipAddress = ipAddress;
		m_ifIndex=-1;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("node", m_node)
			.append("ipAddress", m_ipAddress)
			.append("macAddress", m_macAddress)
			.append("status", m_status)
			.append("sourceNodeId", m_sourceNodeId)
			.append("ifIndex", m_ifIndex)
			.append("lastPollTime", m_lastPollTime)
			.toString();
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

    public void setNode(OnmsNode node) {
        m_node = node;
    }

	/**
	 * <p>Getter for the field <code>ipAddress</code>.</p>
	 *
	 * @return Returns the ipAddress.
	 */
	@Column(name="ipAddr", nullable=false)
	@XmlElement(name="ipAddress")
	@Type(type="org.opennms.netmgt.model.InetAddressUserType")
	@XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
	public InetAddress getIpAddress() {
		return m_ipAddress;
	}

	public void setIpAddress(final InetAddress ipAddress) {
		m_ipAddress = ipAddress;
	}

	/**
	 * <p>Getter for the field <code>macAddress</code>.</p>
	 *
	 * @return Returns the MAC address.
	 */
	@XmlElement
	@Column(name="atPhysAddr", nullable=false)
	public String getMacAddress() {
		return m_macAddress;
	}
	
	/**
	 * <p>Setter for the field <code>macAddress</code>.</p>
	 *
	 * @param macAddress a {@link java.lang.String} object.
	 */
	public void setMacAddress(final String macAddress) {
		this.m_macAddress = macAddress;
	}

	@XmlAttribute
	@Column(nullable=false)
	public StatusType getStatus() {
		return m_status;
	}

	public void setStatus(final StatusType status) {
		m_status = status;
	}

	@XmlAttribute
	@Column(nullable=false)
	public Integer getSourceNodeId() {
		return m_sourceNodeId;
	}
	
	public void setSourceNodeId(final Integer sourceNodeId) {
		m_sourceNodeId = sourceNodeId;
	}
	
	/**
	 * <p>Getter for the field <code>ifindex</code>.</p>
	 *
	 * @return a int.
	 */
	@XmlAttribute
	@Column(nullable=false)
	public Integer getIfIndex() {
		return m_ifIndex;
	}

	/**
	 * <p>Setter for the field <code>ifindex</code>.</p>
	 *
	 * @param ifIndex a int.
	 */
	public void setIfIndex(final Integer ifIndex) {
	    m_ifIndex = ifIndex;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastPollTime", nullable=false)
    @XmlElement(name="lastPollTime")
	public Date getLastPollTime() {
		return m_lastPollTime;
	}
	
	public void setLastPollTime(final Date lastPollTime) {
		m_lastPollTime = lastPollTime;
	}
}
