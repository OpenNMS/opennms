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

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;


/**
 * <p>BridgeStpInterface class.</p>
 *
 * @author antonio
 */
@XmlRootElement(name = "stpInterface")
@Entity
@Table(name="stpInterface", uniqueConstraints = {@UniqueConstraint(columnNames={"nodeId", "bridgePort", "stpVlan"})})
public class OnmsStpInterface {

    @Embeddable
    public static class StpPortStatus implements Comparable<StpPortStatus>, Serializable {
        
    	
    	/**
		 * 
		 */
		private static final long serialVersionUID = 7669097061380115150L;

		public static final int STP_PORT_STATUS_UNKNOWN = 0;
    	public static final int STP_PORT_STATUS_DISABLED = 1;
    	public static final int STP_PORT_STATUS_BLOCKING = 2;
    	public static final int STP_PORT_STATUS_LISTENING = 3;
    	public static final int STP_PORT_STATUS_LEARNING = 4;
    	public static final int STP_PORT_STATUS_FORWARDING = 5;
    	public static final int STP_PORT_STATUS_BROKEN = 6;
    	
        private static final Integer[] s_order = {0,1,2,3,4,5,6};

        private Integer m_stpPortStatus;

        private static final Map<Integer, String> stpPortStatusMap = new HashMap<Integer, String>();
        
        static {
            stpPortStatusMap.put(0, "Unknown" );
            stpPortStatusMap.put(1, "disabled" );
            stpPortStatusMap.put(2, "blocking" );
            stpPortStatusMap.put(3, "listening" );
            stpPortStatusMap.put(4, "learning" );
            stpPortStatusMap.put(5, "forwarding" );
            stpPortStatusMap.put(6, "broken" );
        }

        @SuppressWarnings("unused")
        private StpPortStatus() {
        }

        public StpPortStatus(Integer stpPortStatus) {
            m_stpPortStatus = stpPortStatus;
        }

        @Column(name="stpPortState")
        public Integer getIntCode() {
            return m_stpPortStatus;
        }

        public void setIntCode(Integer stpPortStatus) {
            m_stpPortStatus = stpPortStatus;
        }

                @Override
        public int compareTo(StpPortStatus o) {
            return getIndex(m_stpPortStatus) - getIndex(o.m_stpPortStatus);
        }

        private static int getIndex(Integer code) {
            for (int i = 0; i < s_order.length; i++) {
                if (s_order[i] == code) {
                    return i;
                }
            }
            throw new IllegalArgumentException("illegal stpPortStatus code '"+code+"'");
        }

                @Override
        public boolean equals(Object o) {
            if (o instanceof StpPortStatus) {
                return m_stpPortStatus.intValue() == ((StpPortStatus)o).m_stpPortStatus.intValue();
            }
            return false;
        }

                @Override
        public int hashCode() {
            return toString().hashCode();
        }

                @Override
        public String toString() {
            return String.valueOf(m_stpPortStatus);
        }

        public static StpPortStatus get(Integer code) {
            if (code == null)
                return StpPortStatus.UNKNOWN;
            switch (code) {
            case STP_PORT_STATUS_UNKNOWN: return UNKNOWN;
            case STP_PORT_STATUS_DISABLED: return DISABLED;
            case STP_PORT_STATUS_BLOCKING: return BLOCKING;
            case STP_PORT_STATUS_LISTENING: return LISTENING;
            case STP_PORT_STATUS_LEARNING: return LEARNING;
            case STP_PORT_STATUS_FORWARDING: return FORWARDING;
            case STP_PORT_STATUS_BROKEN: return BROKEN;

            default:
                throw new IllegalArgumentException("Cannot create vlanStatus from code "+code);
            }
        }

        /**
         * <p>getPortStatusString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        /**
         */
        public static String getStpPortStatusString(Integer code) {
            if (stpPortStatusMap.containsKey(code))
                    return stpPortStatusMap.get( code);
            return null;
        }
        
        public static StpPortStatus UNKNOWN = new StpPortStatus(STP_PORT_STATUS_UNKNOWN);
        public static StpPortStatus DISABLED = new StpPortStatus(STP_PORT_STATUS_DISABLED);
        public static StpPortStatus BLOCKING = new StpPortStatus(STP_PORT_STATUS_BLOCKING);
        public static StpPortStatus LISTENING = new StpPortStatus(STP_PORT_STATUS_LISTENING);
        public static StpPortStatus LEARNING = new StpPortStatus(STP_PORT_STATUS_LEARNING);
        public static StpPortStatus FORWARDING = new StpPortStatus(STP_PORT_STATUS_FORWARDING);
        public static StpPortStatus BROKEN = new StpPortStatus(STP_PORT_STATUS_BROKEN);


    }

    private Integer m_id;
	private OnmsNode m_node;
	private Integer m_bridgePort;
	private Integer m_ifIndex = -1;
	private StpPortStatus m_stpPortState;
	private Integer m_stpPortPathCost;
	private String m_stpPortDesignatedRoot;
	private Integer m_stpPortDesignatedCost;
	private String m_stpPortDesignatedBridge;
	private String m_stpPortDesignatedPort;
	private StatusType m_status = StatusType.UNKNOWN;
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
	public StpPortStatus getStpPortState() {
		return m_stpPortState;
	}

	public void setStpPortState(final StpPortStatus stpPortState) {
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

