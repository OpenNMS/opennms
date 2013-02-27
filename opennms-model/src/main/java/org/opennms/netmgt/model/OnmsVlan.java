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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
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

import org.apache.commons.lang.builder.ToStringBuilder;

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;

@XmlRootElement(name = "vlan")
@Entity
@Table(name="vlan", uniqueConstraints = {@UniqueConstraint(columnNames={"nodeId", "vlanId"})})
public class OnmsVlan {
    
    @Embeddable
    public static class VlanType implements Comparable<VlanType>, Serializable {
        private static final long serialVersionUID = -4784344871599250528L;
        
        /**
         * <p>String identifiers for the enumeration of values:</p>
         */ 
        public static final int VLAN_TYPE_UNKNOWN = 0;
        public static final int VLAN_TYPE_VTP_ETHERNET = 1;
        public static final int VLAN_TYPE_VTP_FDDI = 2;
        public static final int VLAN_TYPE_VTP_TOKENRING = 3;
        public static final int VLAN_TYPE_VTP_FDDINET = 4;
        public static final int VLAN_TYPE_VTP_TRNET = 5;
        public static final int VLAN_TYPE_VTP_DEPRECATED = 6;

        
        private static final Integer[] s_order = {0,1,2,3,4,5,6};

        private Integer m_vlanType;

        private static final Map<Integer, String> vlanTypeMap = new HashMap<Integer, String>();
        
        static {
            vlanTypeMap.put(0, "Unknown" );
            vlanTypeMap.put(1, "CiscoVtp/Ethernet" );
            vlanTypeMap.put(2, "CiscoVtp/FDDI" );
            vlanTypeMap.put(3, "CiscoVtp/TokenRing" );
            vlanTypeMap.put(4, "CiscoVtp/FDDINet" );
            vlanTypeMap.put(5, "CiscoVtp/TRNet" );
            vlanTypeMap.put(6, "CiscoVtp/Deprecated" );
        }

        @SuppressWarnings("unused")
        private VlanType() {
        }

        public VlanType(Integer vlanType) {
            m_vlanType = vlanType;
        }

        @Column(name="vlanType")
        public Integer getIntCode() {
            return m_vlanType;
        }

        public void setIntCode(Integer vlanType) {
            m_vlanType = vlanType;
        }

        public int compareTo(VlanType o) {
            return getIndex(m_vlanType) - getIndex(o.m_vlanType);
        }

        private static int getIndex(Integer code) {
            for (int i = 0; i < s_order.length; i++) {
                if (s_order[i] == code) {
                    return i;
                }
            }
            throw new IllegalArgumentException("illegal vlanType code '"+code+"'");
        }

        public boolean equals(Object o) {
            if (o instanceof VlanType) {
                return m_vlanType == ((VlanType)o).m_vlanType;
            }
            return false;
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public String toString() {
            return String.valueOf(m_vlanType);
        }

        public static VlanType get(Integer code) {
            if (code == null)
                return null;
            switch (code) {
            case VLAN_TYPE_UNKNOWN: return UNKNOWN;
            case VLAN_TYPE_VTP_ETHERNET: return CISCO_VTP_ETHERNET;
            case VLAN_TYPE_VTP_FDDI: return CISCO_VTP_FDDI;
            case VLAN_TYPE_VTP_TOKENRING: return CISCO_VTP_TOKENRING;
            case VLAN_TYPE_VTP_FDDINET: return CISCO_VTP_FDDINET;
            case VLAN_TYPE_VTP_TRNET: return CISCO_VTP_TRNET;
            case VLAN_TYPE_VTP_DEPRECATED: return CISCO_VTP_DEPRECATED;
            default:
                throw new IllegalArgumentException("Cannot create vlanType from code "+code);
            }
        }

        /**
         * <p>getRouteTypeString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        /**
         */
        public static String getVlanTypeString(Integer code) {
            if (vlanTypeMap.containsKey(code))
                    return vlanTypeMap.get( code);
            return null;
        }
        
        public static VlanType UNKNOWN = new VlanType(VLAN_TYPE_UNKNOWN);
        public static VlanType CISCO_VTP_ETHERNET = new VlanType(VLAN_TYPE_VTP_ETHERNET);
        public static VlanType CISCO_VTP_FDDI = new VlanType(VLAN_TYPE_VTP_FDDI);
        public static VlanType CISCO_VTP_TOKENRING = new VlanType(VLAN_TYPE_VTP_TOKENRING);
        public static VlanType CISCO_VTP_FDDINET = new VlanType(VLAN_TYPE_VTP_FDDINET);
        public static VlanType CISCO_VTP_TRNET = new VlanType(VLAN_TYPE_VTP_TRNET);
        public static VlanType CISCO_VTP_DEPRECATED = new VlanType(VLAN_TYPE_VTP_DEPRECATED);


    }

    private Integer m_id;
    private OnmsNode m_node;
    private Integer m_vlanId;
    private String m_vlanName;
    private VlanType m_vlanType = VlanType.UNKNOWN;
    private Integer m_vlanStatus = -1;
    private StatusType m_status = StatusType.UNKNOWN;
    private Date m_lastPollTime;	

    public OnmsVlan() {
    }
	
    public OnmsVlan(final int index, final String name, final int status, final VlanType type) {
	m_vlanId = index;
	m_vlanName = name;
	m_vlanStatus = status;
	m_vlanType = type;
    }

    public OnmsVlan(final int index, final String name, final int status) {
	m_vlanId = index;
	m_vlanName = name;
	m_vlanStatus = status;
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
    
    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    @XmlElement(name="nodeId")
    @XmlIDREF
    public OnmsNode getNode() {
        return m_node;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(final OnmsNode node) {
        m_node = node;
    }
    
    @XmlAttribute
    @Column(nullable=false)
    public Integer getVlanId() {
		return m_vlanId;
	}

	public void setVlanId(final Integer vlanId) {
		m_vlanId = vlanId;
	}

	@XmlAttribute(name="name")
	@Column(nullable=false)
	public String getVlanName() {
		return m_vlanName;
	}

	public void setVlanName(final String vlanName) {
		m_vlanName = vlanName;
	}

	@XmlAttribute(name="type")
	@Column
	public VlanType getVlanType() {
		return m_vlanType;
	}

	public void setVlanType(final VlanType vlanType) {
		m_vlanType = vlanType;
	}

	@XmlAttribute
	@Column
	public Integer getVlanStatus() {
		return m_vlanStatus;
	}

	public void setVlanStatus(final Integer vlanStatus) {
		m_vlanStatus = vlanStatus;
	}

	@XmlAttribute
	@Column(nullable=false)
	public StatusType getStatus() {
		return m_status;
	}

	public void setStatus(final StatusType status) {
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

	@Override
	public String toString() {
	    return new ToStringBuilder(this)
	        .append("dbId", m_id)
	        .append("node", m_node)
	        .append("id", m_vlanId)
	        .append("name", m_vlanName)
	        .append("type", m_vlanType)
	        .append("status", m_vlanStatus)
	        .append("dbStatus", m_status)
	        .append("lastPollTime", m_lastPollTime)
	        .toString();
	}
}
