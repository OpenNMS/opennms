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

/**
 * <p>AtInterface class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
@XmlRootElement(name = "stpNode")
@Entity
@Table(name="stpNode", uniqueConstraints = {@UniqueConstraint(columnNames={"nodeId", "baseVlan"})})
public class OnmsStpNode {
	
    @Embeddable
    public static class BridgeBaseType implements Comparable<BridgeBaseType>, Serializable {
            	
		private static final long serialVersionUID = 4211573691385106051L;
		public static final int BASE_TYPE_UNKNOWN = 1;
		public static final int BASE_TYPE_TRANSPARENT_ONLY = 2;
		public static final int BASE_TYPE_SOURCEROUTE_ONLY = 3;
		public static final int BASE_TYPE_SRT = 4;
    	
        private static final Integer[] s_order = {1,2,3,4};

        private Integer m_basebridgetype;

        private static final Map<Integer, String> baseBridgeTypeMap = new HashMap<Integer, String>();
        
        static {
            baseBridgeTypeMap.put(1, "unknown" );
            baseBridgeTypeMap.put(2, "transparent-only" );
            baseBridgeTypeMap.put(3, "sourceroute-only" );
            baseBridgeTypeMap.put(4, "srt" );
        }

        @SuppressWarnings("unused")
        private BridgeBaseType() {
        }

        public BridgeBaseType(Integer bridgeBaseType) {
            m_basebridgetype = bridgeBaseType;
        }

        @Column(name="baseType")
        public Integer getIntCode() {
            return m_basebridgetype;
        }

        public void setIntCode(Integer baseBridgeType) {
            m_basebridgetype = baseBridgeType;
        }

                @Override
        public int compareTo(BridgeBaseType o) {
            return getIndex(m_basebridgetype) - getIndex(o.m_basebridgetype);
        }

        private static int getIndex(Integer code) {
            for (int i = 0; i < s_order.length; i++) {
                if (s_order[i] == code) {
                    return i;
                }
            }
            throw new IllegalArgumentException("illegal baseBridgeType code '"+code+"'");
        }

                @Override
        public boolean equals(Object o) {
            if (o instanceof BridgeBaseType) {
                return m_basebridgetype.intValue() == ((BridgeBaseType)o).m_basebridgetype.intValue();
            }
            return false;
        }

                @Override
        public int hashCode() {
            return toString().hashCode();
        }

                @Override
        public String toString() {
            return String.valueOf(m_basebridgetype);
        }

        public static BridgeBaseType get(Integer code) {
            if (code == null)
                return BridgeBaseType.UNKNOWN;
            switch (code) {
            case BASE_TYPE_UNKNOWN: return UNKNOWN;
            case BASE_TYPE_TRANSPARENT_ONLY: return TRANSPARENT_ONLY;
            case BASE_TYPE_SOURCEROUTE_ONLY: return SOURCEROUTE_ONLY;
            case BASE_TYPE_SRT: return SRT;
            default:
                throw new IllegalArgumentException("Cannot create BridgeBaseType from code "+code);
            }
        }

        /**
         * <p>getBridgeBaseTypeString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        /**
         */
        public static String getBridgeBaseTypeString(Integer code) {
            if (baseBridgeTypeMap.containsKey(code))
                    return baseBridgeTypeMap.get( code);
            return null;
        }
        
        public static BridgeBaseType UNKNOWN = new BridgeBaseType(BASE_TYPE_UNKNOWN);
        public static BridgeBaseType TRANSPARENT_ONLY = new BridgeBaseType(BASE_TYPE_TRANSPARENT_ONLY);
        public static BridgeBaseType SOURCEROUTE_ONLY = new BridgeBaseType(BASE_TYPE_SOURCEROUTE_ONLY);
        public static BridgeBaseType SRT = new BridgeBaseType(BASE_TYPE_SRT);


    }

    @Embeddable
    public static class StpProtocolSpecification implements Comparable<StpProtocolSpecification>, Serializable {
            	
		/**
		 * 
		 */
		private static final long serialVersionUID = -1815947324977781143L;
		public static final int STP_PROTOCOL_SPECIFICATION_UNKNOWN = 1;
		public static final int STP_PROTOCOL_SPECIFICATION_DECLB100 = 2;
		public static final int STP_PROTOCOL_SPECIFICATION_IEEE8021D = 3;
    	
        private static final Integer[] s_order = {1,2,3};

        private Integer m_stpprotocolspecification;

        private static final Map<Integer, String> stpProtocolSpecificationMap = new HashMap<Integer, String>();
        
        static {
            stpProtocolSpecificationMap.put(1, "unknown" );
            stpProtocolSpecificationMap.put(2, "decLb100" );
            stpProtocolSpecificationMap.put(3, "ieee8021d" );
        }

        @SuppressWarnings("unused")
        private StpProtocolSpecification() {
        }

        public StpProtocolSpecification(Integer stpprotocolspecification) {
            m_stpprotocolspecification = stpprotocolspecification;
        }

        @Column(name="stpProtocolSpecification")
        public Integer getIntCode() {
            return m_stpprotocolspecification;
        }

        public void setIntCode(Integer stpProtocolSpecification) {
            m_stpprotocolspecification = stpProtocolSpecification;
        }

                @Override
        public int compareTo(StpProtocolSpecification o) {
            return getIndex(m_stpprotocolspecification) - getIndex(o.m_stpprotocolspecification);
        }

        private static int getIndex(Integer code) {
            for (int i = 0; i < s_order.length; i++) {
                if (s_order[i] == code) {
                    return i;
                }
            }
            throw new IllegalArgumentException("illegal StpProtocolSpecification code '"+code+"'");
        }

                @Override
        public boolean equals(Object o) {
            if (o instanceof StpProtocolSpecification) {
                return m_stpprotocolspecification.intValue() == ((StpProtocolSpecification)o).m_stpprotocolspecification.intValue();
            }
            return false;
        }

                @Override
        public int hashCode() {
            return toString().hashCode();
        }

                @Override
        public String toString() {
            return String.valueOf(m_stpprotocolspecification);
        }

        public static StpProtocolSpecification get(Integer code) {
            if (code == null)
                return StpProtocolSpecification.UNKNOWN;
            switch (code) {
            case STP_PROTOCOL_SPECIFICATION_UNKNOWN: return UNKNOWN;
            case STP_PROTOCOL_SPECIFICATION_DECLB100: return DECLB100;
            case STP_PROTOCOL_SPECIFICATION_IEEE8021D: return IEEE8021D;
            default:
                throw new IllegalArgumentException("Cannot create StpProtocolSpecification from code "+code);
            }
        }

        /**
         * <p>getStpProtocolSpecificationString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        /**
         */
        public static String getStpProtocolSpecificationString(Integer code) {
            if (stpProtocolSpecificationMap.containsKey(code))
                    return stpProtocolSpecificationMap.get( code);
            return null;
        }
        
        public static StpProtocolSpecification UNKNOWN = new StpProtocolSpecification(STP_PROTOCOL_SPECIFICATION_UNKNOWN);
        public static StpProtocolSpecification DECLB100 = new StpProtocolSpecification(STP_PROTOCOL_SPECIFICATION_DECLB100);
        public static StpProtocolSpecification IEEE8021D = new StpProtocolSpecification(STP_PROTOCOL_SPECIFICATION_IEEE8021D);


    }

    private Integer m_id;
	private OnmsNode m_node;
	private String m_baseBridgeAddress;
	private Integer m_baseNumPorts;
	private BridgeBaseType m_baseType;
	private StpProtocolSpecification m_stpProtocolSpecification;
	private Integer m_stpPriority;
	private String m_stpDesignatedRoot;
	private Integer m_stpRootCost;
	private Integer m_stpRootPort;
	private StatusType m_status = StatusType.UNKNOWN;
	private Date m_lastPollTime;
	private Integer m_baseVlan;
	private String m_baseVlanName;

	public OnmsStpNode() {}

    public OnmsStpNode(final OnmsNode node, final Integer vlanIndex) {
    	m_node = node;
    	m_baseVlan = vlanIndex;
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

    @XmlElement
    @Column(length=12, nullable=false)
	public String getBaseBridgeAddress() {
		return m_baseBridgeAddress;
	}

	public void setBaseBridgeAddress(final String baseBridgeAddress) {
		m_baseBridgeAddress = baseBridgeAddress;
	}

    @XmlElement
    @Column
	public Integer getBaseNumPorts() {
		return m_baseNumPorts;
	}

	public void setBaseNumPorts(final Integer baseNumPorts) {
		m_baseNumPorts = baseNumPorts;
	}

    @XmlElement
    @Column
	public BridgeBaseType getBaseType() {
		return m_baseType;
	}

	public void setBaseType(final BridgeBaseType baseType) {
		m_baseType = baseType;
	}

    @XmlElement
    @Column
	public StpProtocolSpecification getStpProtocolSpecification() {
		return m_stpProtocolSpecification;
	}

	public void setStpProtocolSpecification(final StpProtocolSpecification stpProtocolSpecification) {
		m_stpProtocolSpecification = stpProtocolSpecification;
	}

    @XmlElement
    @Column
	public Integer getStpPriority() {
		return m_stpPriority;
	}

	public void setStpPriority(final Integer stpPriority) {
		m_stpPriority = stpPriority;
	}

    @XmlElement
    @Column(length=16)
	public String getStpDesignatedRoot() {
		return m_stpDesignatedRoot;
	}

	public void setStpDesignatedRoot(final String stpDesignatedRoot) {
		m_stpDesignatedRoot = stpDesignatedRoot;
	}

    @XmlElement
    @Column
	public Integer getStpRootCost() {
		return m_stpRootCost;
	}

	public void setStpRootCost(final Integer stpRootCost) {
		m_stpRootCost = stpRootCost;
	}

    @XmlElement
    @Column
	public Integer getStpRootPort() {
		return m_stpRootPort;
	}

	public void setStpRootPort(final Integer stpRootPort) {
		m_stpRootPort = stpRootPort;
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
	public Integer getBaseVlan() {
		return m_baseVlan;
	}

	public void setBaseVlan(final Integer baseVlan) {
		m_baseVlan = baseVlan;
	}

    @XmlElement
    @Column(length=32)
	public String getBaseVlanName() {
		return m_baseVlanName;
	}

	public void setBaseVlanName(final String baseVlanName) {
		m_baseVlanName = baseVlanName;
	}
	
    @Override
	public String toString() {
	    return new ToStringBuilder(this)
	        .append("id", m_id)
	        .append("node", m_node)
	        .append("baseBridgeAddress", m_baseBridgeAddress)
	        .append("baseNumPorts", m_baseNumPorts)
	        .append("baseType", m_baseType)
	        .append("stpProtocolSpecification", m_stpProtocolSpecification)
	        .append("stpPriority", m_stpPriority)
	        .append("stpDesignatedRoot", m_stpDesignatedRoot)
	        .append("stpRootCost", m_stpRootCost)
	        .append("stpRootPort", m_stpRootPort)
	        .append("status", m_status)
	        .append("lastPollTime", m_lastPollTime)
	        .append("baseVlan", m_baseVlan)
	        .append("baseVlanName", m_baseVlanName)
	        .toString();
	}
}
