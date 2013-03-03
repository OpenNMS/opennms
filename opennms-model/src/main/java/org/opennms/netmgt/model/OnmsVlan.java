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
    public static class VlanStatus implements Comparable<VlanStatus>, Serializable {
        
		private static final long serialVersionUID = -5676188320482765289L;
		/**
         * <p>String identifiers for the enumeration of values:</p>
         */ 
        public static final int VLAN_STATUS_UNKNOWN = 0;
    	/** Constant <code>CISCOVTP_VLAN_STATUS_OPERATIONAL=1</code> */
    	public final static int CISCOVTP_VLAN_STATUS_OPERATIONAL = 1;
    	/** Constant <code>CISCOVTP_VLAN_STATUS_SUSPENDED=2</code> */
    	public final static int CISCOVTP_VLAN_STATUS_SUSPENDED = 2;
    	/** Constant <code>CISCOVTP_VLAN_STATUS_mtuTooBigForDevice=3</code> */
    	public final static int CISCOVTP_VLAN_STATUS_mtuTooBigForDevice = 3;
    	/** Constant <code>CISCOVTP_VLAN_STATUS_mtuTooBigForTrunk=4</code> */
    	public final static int CISCOVTP_VLAN_STATUS_mtuTooBigForTrunk = 4;

    	// RowStatus Definition and mapping
    	public final static int ROWSTATUS_STARTING_INDEX = 4;
        
    	public final static int SNMPV2C_ROWSTATUS_ACTIVE = 5;
        public final static int SNMPV2C_ROWSTATUS_NOTINSERVICE = 6;
        public final static int SNMPV2C_ROWSTATUS_NOTREADY = 7;
        public final static int SNMPV2C_ROWSTATUS_CREATEANDGO = 8;
        public final static int SNMPV2C_ROWSTATUS_CREATEANDWAIT = 9;
        public final static int SNMPV2C_ROWSTATUS_DESTROY = 10;

        private static final Integer[] s_order = {0,1,2,3,4,5,6,7,8,9,10};

        private Integer m_vlanStatus;

        private static final Map<Integer, String> vlanStatusMap = new HashMap<Integer, String>();
        
        static {
            vlanStatusMap.put(0, "unknown" );
            vlanStatusMap.put(1, "operational" );
            vlanStatusMap.put(2, "ciscovtp/suspended" );
            vlanStatusMap.put(3, "ciscovtp/mtuTooBigForDevice" );
            vlanStatusMap.put(4, "ciscovtp/mtuTooBigForTrunk" );
            vlanStatusMap.put(5, "rowStatus/active" );
            vlanStatusMap.put(6, "rowStatus/notInService" );
            vlanStatusMap.put(7, "rowStatus/notReady" );
            vlanStatusMap.put(8, "rowStatus/createAndGo" );
            vlanStatusMap.put(9, "rowStatus/createAndWait" );
            vlanStatusMap.put(10, "rowStatus/destroy" );
                   }

        @SuppressWarnings("unused")
        private VlanStatus() {
        }

        public VlanStatus(Integer vlanType) {
            m_vlanStatus = vlanType;
        }

        @Column(name="vlanStatus")
        public Integer getIntCode() {
            return m_vlanStatus;
        }

        public void setIntCode(Integer vlanType) {
            m_vlanStatus = vlanType;
        }

        public int compareTo(VlanStatus o) {
            return getIndex(m_vlanStatus) - getIndex(o.m_vlanStatus);
        }

        private static int getIndex(Integer code) {
            for (int i = 0; i < s_order.length; i++) {
                if (s_order[i] == code) {
                    return i;
                }
            }
            throw new IllegalArgumentException("illegal vlanStatus code '"+code+"'");
        }

        public boolean equals(Object o) {
            if (o instanceof VlanStatus) {
                return m_vlanStatus.intValue() == ((VlanStatus)o).m_vlanStatus.intValue();
            }
            return false;
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public String toString() {
            return String.valueOf(m_vlanStatus);
        }

        public static VlanStatus get(Integer code) {
            if (code == null)
                return VlanStatus.UNKNOWN;
            switch (code) {
            case VLAN_STATUS_UNKNOWN: return UNKNOWN;
            case CISCOVTP_VLAN_STATUS_OPERATIONAL: return CISCOVTP_OPERATIONAL;
            case CISCOVTP_VLAN_STATUS_SUSPENDED: return CISCOVTP_SUSPENDED;
            case CISCOVTP_VLAN_STATUS_mtuTooBigForDevice: return CISCOVTP_mtuTooBigForDevice;
            case CISCOVTP_VLAN_STATUS_mtuTooBigForTrunk: return CISCOVTP_mtuTooBigForTrunk;
            case SNMPV2C_ROWSTATUS_ACTIVE : return ROWSTATUS_ACTIVE;
            case SNMPV2C_ROWSTATUS_NOTINSERVICE : return ROWSTATUS_NOTINSERVICE;
            case SNMPV2C_ROWSTATUS_NOTREADY : return ROWSTATUS_NOTREADY;
            case SNMPV2C_ROWSTATUS_CREATEANDGO : return ROWSTATUS_CREATEANDGO;
            case SNMPV2C_ROWSTATUS_CREATEANDWAIT : return ROWSTATUS_CREATEANDWAIT;
            case SNMPV2C_ROWSTATUS_DESTROY : return ROWSTATUS_DESTROY;
            default:
                throw new IllegalArgumentException("Cannot create vlanStatus from code "+code);
            }
        }

        /**
         * <p>getVlanStatusString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        /**
         */
        public static String getVlanStatusString(Integer code) {
            if (vlanStatusMap.containsKey(code))
                    return vlanStatusMap.get( code);
            return null;
        }
        
        public static VlanStatus UNKNOWN = new VlanStatus(VLAN_STATUS_UNKNOWN);
        public static VlanStatus CISCOVTP_OPERATIONAL = new VlanStatus(CISCOVTP_VLAN_STATUS_OPERATIONAL);
        public static VlanStatus CISCOVTP_SUSPENDED = new VlanStatus(CISCOVTP_VLAN_STATUS_SUSPENDED);
        public static VlanStatus CISCOVTP_mtuTooBigForDevice = new VlanStatus(CISCOVTP_VLAN_STATUS_mtuTooBigForDevice);
        public static VlanStatus CISCOVTP_mtuTooBigForTrunk = new VlanStatus(CISCOVTP_VLAN_STATUS_mtuTooBigForTrunk);
        public static VlanStatus ROWSTATUS_ACTIVE = new VlanStatus(SNMPV2C_ROWSTATUS_ACTIVE);
        public static VlanStatus ROWSTATUS_NOTINSERVICE = new VlanStatus(SNMPV2C_ROWSTATUS_NOTINSERVICE);
        public static VlanStatus ROWSTATUS_NOTREADY = new VlanStatus(SNMPV2C_ROWSTATUS_NOTREADY);
        public static VlanStatus ROWSTATUS_CREATEANDGO = new VlanStatus(SNMPV2C_ROWSTATUS_CREATEANDGO);
        public static VlanStatus ROWSTATUS_CREATEANDWAIT = new VlanStatus(SNMPV2C_ROWSTATUS_CREATEANDWAIT);
        public static VlanStatus ROWSTATUS_DESTROY = new VlanStatus(SNMPV2C_ROWSTATUS_DESTROY);


    }
    
    @Embeddable
    public static class VlanType implements Comparable<VlanType>, Serializable {
		
    	private static final long serialVersionUID = -7012640218990540145L;
        
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
        public static final int VLAN_TYPE_EXTREME_LAYERTWO = 7;
        

        public static final int THREECOM_STARTING_INDEX = 7;

        public static final int VLAN_TYPE_THREECOM_vlanLayer2 = 8;
        public static final int VLAN_TYPE_THREECOM_vlanUnspecifiedProtocols = 9;	 
        public static final int VLAN_TYPE_THREECOM_vlanIPProtocol = 10;	 
        public static final int VLAN_TYPE_THREECOM_vlanIPXProtocol = 11;	 
        public static final int VLAN_TYPE_THREECOM_vlanAppleTalkProtocol = 12;	 
        public static final int VLAN_TYPE_THREECOM_vlanXNSProtocol = 13;	 
        public static final int VLAN_TYPE_THREECOM_vlanISOProtocol = 14;	 
        public static final int VLAN_TYPE_THREECOM_vlanDECNetProtocol =	15;	 
        public static final int VLAN_TYPE_THREECOM_vlanNetBIOSProtocol = 16;	 
        public static final int VLAN_TYPE_THREECOM_vlanSNAProtocol = 17; 
        public static final int VLAN_TYPE_THREECOM_vlanVINESProtocol = 18;	 
        public static final int VLAN_TYPE_THREECOM_vlanX25Protocol = 19;	 
        public static final int VLAN_TYPE_THREECOM_vlanIGMPProtocol = 20;	 
        public static final int VLAN_TYPE_THREECOM_vlanSessionLayer = 21;	 
        public static final int VLAN_TYPE_THREECOM_vlanNetBeui = 22; 
        public static final int VLAN_TYPE_THREECOM_vlanLayeredProtocols = 23;	 
        public static final int VLAN_TYPE_THREECOM_vlanIPXIIProtocol = 24;	 
        public static final int VLAN_TYPE_THREECOM_vlanIPX8022Protocol = 25;	 
        public static final int VLAN_TYPE_THREECOM_vlanIPX8023Protocol = 26;	 
        public static final int VLAN_TYPE_THREECOM_vlanIPX8022SNAPProtocol = 27;	 

        /** 
        vlanLayer2 	 (1),	 
 		vlanUnspecifiedProtocols 	 (2),	 
 		vlanIPProtocol 	 (3),	 
 		vlanIPXProtocol 	 (4),	 
 		vlanAppleTalkProtocol 	 (5),	 
 		vlanXNSProtocol 	 (6),	 
 		vlanISOProtocol 	 (7),	 
 		vlanDECNetProtocol 	 (8),	 
 		vlanNetBIOSProtocol 	 (9),	 
 		vlanSNAProtocol 	 (10),	 
 		vlanVINESProtocol 	 (11),	 
 		vlanX25Protocol 	 (12),	 
 		vlanIGMPProtocol 	 (13),	 
 		vlanSessionLayer 	 (14),	 
 		vlanNetBeui 	 (15),	 
 		vlanLayeredProtocols 	 (16),	 
 		vlanIPXIIProtocol 	 (17),	 
 		vlanIPX8022Protocol 	 (18),	 
 		vlanIPX8023Protocol 	 (19),	 
 		vlanIPX8022SNAPProtocol 	 (20)	 
 */
        
        private static final Integer[] s_order = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27};

        private Integer m_vlanType;

        private static final Map<Integer, String> vlanTypeMap = new HashMap<Integer, String>();
        
        static {
            vlanTypeMap.put(0, "Unknown" );
            vlanTypeMap.put(1, "Ethernet" );
            vlanTypeMap.put(2, "CiscoVtp/FDDI" );
            vlanTypeMap.put(3, "CiscoVtp/TokenRing" );
            vlanTypeMap.put(4, "CiscoVtp/FDDINet" );
            vlanTypeMap.put(5, "CiscoVtp/TRNet" );
            vlanTypeMap.put(6, "CiscoVtp/Deprecated" );
            vlanTypeMap.put(7, "Extreme/LayerTwo" );
            vlanTypeMap.put(8, "3com/vlanLayer2" );	 
            vlanTypeMap.put(9, "3com/vlanUnspecifiedProtocols" );	 
            vlanTypeMap.put(10, "3com/vlanIPProtocol" ); 
            vlanTypeMap.put(11, "3com/vlanIPXProtocol" );	 
            vlanTypeMap.put(12, "3com/vlanAppleTalkProtocol" ); 
            vlanTypeMap.put(13, "3com/vlanXNSProtocol" );	 
            vlanTypeMap.put(14, "3com/vlanISOProtocol" );
            vlanTypeMap.put(15, "3com/vlanDECNetProtocol" );
            vlanTypeMap.put(16, "3com/vlanNetBIOSProtocol" );
            vlanTypeMap.put(17, "3com/vlanSNAProtocol" );
            vlanTypeMap.put(18, "3com/vlanVINESProtocol" );
            vlanTypeMap.put(19, "3com/vlanX25Protocol" );
            vlanTypeMap.put(20, "3com/vlanIGMPProtocol" );
            vlanTypeMap.put(21, "3com/vlanSessionLayer" );
            vlanTypeMap.put(22, "3com/vlanNetBeui" );
            vlanTypeMap.put(23, "3com/vlanLayeredProtocols" );
            vlanTypeMap.put(24, "3com/vlanIPXIIProtocol" );
            vlanTypeMap.put(25, "3com/vlanIPX8022Protocol" );
            vlanTypeMap.put(26, "3com/vlanIPX8023Protocol" );
            vlanTypeMap.put(27, "3com/vlanIPX8022SNAPProtocol" );	 
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
                return m_vlanType.intValue() == ((VlanType)o).m_vlanType.intValue();
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
                return VlanType.UNKNOWN;
            switch (code) {
            case VLAN_TYPE_UNKNOWN: return UNKNOWN;
            case VLAN_TYPE_VTP_ETHERNET: return CISCO_VTP_ETHERNET;
            case VLAN_TYPE_VTP_FDDI: return CISCO_VTP_FDDI;
            case VLAN_TYPE_VTP_TOKENRING: return CISCO_VTP_TOKENRING;
            case VLAN_TYPE_VTP_FDDINET: return CISCO_VTP_FDDINET;
            case VLAN_TYPE_VTP_TRNET: return CISCO_VTP_TRNET;
            case VLAN_TYPE_VTP_DEPRECATED: return CISCO_VTP_DEPRECATED;
            case VLAN_TYPE_EXTREME_LAYERTWO: return EXTREME_LAYER2;
            case VLAN_TYPE_THREECOM_vlanLayer2: return THREECOM_vlanLayer2;
            case VLAN_TYPE_THREECOM_vlanUnspecifiedProtocols: return THREECOM_vlanUnspecifiedProtocols;	 
            case VLAN_TYPE_THREECOM_vlanIPProtocol: return THREECOM_vlanIPProtocol;
            case VLAN_TYPE_THREECOM_vlanIPXProtocol: return THREECOM_vlanIPXProtocol;
            case VLAN_TYPE_THREECOM_vlanAppleTalkProtocol: return THREECOM_vlanAppleTalkProtocol; 
            case VLAN_TYPE_THREECOM_vlanXNSProtocol: return THREECOM_vlanXNSProtocol;
            case VLAN_TYPE_THREECOM_vlanISOProtocol: return THREECOM_vlanISOProtocol;
            case VLAN_TYPE_THREECOM_vlanDECNetProtocol: return THREECOM_vlanDECNetProtocol;
            case VLAN_TYPE_THREECOM_vlanNetBIOSProtocol: return THREECOM_vlanNetBIOSProtocol;
            case VLAN_TYPE_THREECOM_vlanSNAProtocol: return THREECOM_vlanSNAProtocol;
            case VLAN_TYPE_THREECOM_vlanVINESProtocol: return THREECOM_vlanVINESProtocol;
            case VLAN_TYPE_THREECOM_vlanX25Protocol: return THREECOM_vlanX25Protocol;
            case VLAN_TYPE_THREECOM_vlanIGMPProtocol: return THREECOM_vlanIGMPProtocol;
            case VLAN_TYPE_THREECOM_vlanSessionLayer: return THREECOM_vlanSessionLayer;
            case VLAN_TYPE_THREECOM_vlanNetBeui: return THREECOM_vlanNetBeui;
            case VLAN_TYPE_THREECOM_vlanLayeredProtocols: return THREECOM_vlanLayeredProtocols;	 
            case VLAN_TYPE_THREECOM_vlanIPXIIProtocol: return THREECOM_vlanIPXIIProtocol;
            case VLAN_TYPE_THREECOM_vlanIPX8022Protocol: return THREECOM_vlanIPX8022Protocol;
            case VLAN_TYPE_THREECOM_vlanIPX8023Protocol: return THREECOM_vlanIPX8023Protocol;
            case VLAN_TYPE_THREECOM_vlanIPX8022SNAPProtocol: return THREECOM_vlanIPX8022SNAPProtocol;
            default:
                throw new IllegalArgumentException("Cannot create vlanType from code "+code);
            }
        }

        /**
         * <p>getVlanTypeString</p>
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
        public static VlanType EXTREME_LAYER2 = new VlanType(VLAN_TYPE_EXTREME_LAYERTWO);
        public static VlanType THREECOM_vlanLayer2 = new VlanType(VLAN_TYPE_THREECOM_vlanLayer2);	 
        public static VlanType THREECOM_vlanUnspecifiedProtocols= new VlanType(VLAN_TYPE_THREECOM_vlanUnspecifiedProtocols);	 
        public static VlanType THREECOM_vlanIPProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPProtocol); 
        public static VlanType THREECOM_vlanIPXProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPXProtocol);	 
        public static VlanType THREECOM_vlanAppleTalkProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanAppleTalkProtocol);
        public static VlanType THREECOM_vlanXNSProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanXNSProtocol);	 
        public static VlanType THREECOM_vlanISOProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanISOProtocol);	 
        public static VlanType THREECOM_vlanDECNetProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanDECNetProtocol);	 
        public static VlanType THREECOM_vlanNetBIOSProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanNetBIOSProtocol);	 
        public static VlanType THREECOM_vlanSNAProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanSNAProtocol);	 
        public static VlanType THREECOM_vlanVINESProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanVINESProtocol);	 
        public static VlanType THREECOM_vlanX25Protocol = new VlanType(VLAN_TYPE_THREECOM_vlanX25Protocol);	 
        public static VlanType THREECOM_vlanIGMPProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanIGMPProtocol);	 
        public static VlanType THREECOM_vlanSessionLayer = new VlanType(VLAN_TYPE_THREECOM_vlanSessionLayer);	 
        public static VlanType THREECOM_vlanNetBeui = new VlanType(VLAN_TYPE_THREECOM_vlanNetBeui);	 
        public static VlanType THREECOM_vlanLayeredProtocols = new VlanType(VLAN_TYPE_THREECOM_vlanLayeredProtocols);	 
        public static VlanType THREECOM_vlanIPXIIProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPXIIProtocol);	 
        public static VlanType THREECOM_vlanIPX8022Protocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPX8022Protocol);	 
        public static VlanType THREECOM_vlanIPX8023Protocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPX8023Protocol);	 
        public static VlanType THREECOM_vlanIPX8022SNAPProtocol = new VlanType(VLAN_TYPE_THREECOM_vlanIPX8022SNAPProtocol);
    }

    private Integer m_id;
    private OnmsNode m_node;
    private Integer m_vlanId;
    private String m_vlanName;
    private VlanType m_vlanType = VlanType.UNKNOWN;
    private VlanStatus m_vlanStatus = VlanStatus.UNKNOWN;
    private StatusType m_status = StatusType.UNKNOWN;
    private Date m_lastPollTime;	

    public OnmsVlan() {
    }
	
    public OnmsVlan(final int index, final String name, final VlanStatus status, final VlanType type) {
	m_vlanId = index;
	m_vlanName = name;
	m_vlanStatus = status;
	m_vlanType = type;
    }

    public OnmsVlan(final int index, final String name, final VlanStatus status) {
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
	public VlanStatus getVlanStatus() {
		return m_vlanStatus;
	}

	public void setVlanStatus(final VlanStatus vlanStatus) {
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
