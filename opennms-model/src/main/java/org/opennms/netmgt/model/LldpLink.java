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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.opennms.netmgt.model.LldpElement.LldpChassisIdSubType;

@Entity
@Table(name="lldpLink")
public class LldpLink implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3813247749765614567L;


	public enum LldpPortIdSubType{
	    LLDP_PORTID_SUBTYPE_INTERFACEALIAS(1),
	    LLDP_PORTID_SUBTYPE_PORTCOMPONENT(2),
	    LLDP_PORTID_SUBTYPE_MACADDRESS(3),
	    LLDP_PORTID_SUBTYPE_NETWORKADDRESS(4),
	    LLDP_PORTID_SUBTYPE_INTERFACENAME(5),
	    LLDP_PORTID_SUBTYPE_AGENTCIRCUITID(6),
	    LLDP_PORTID_SUBTYPE_LOCAL(7);
		/*
	     * LldpPortIdSubtype ::= TEXTUAL-CONVENTION
        STATUS current
        DESCRIPTION
                "This TC describes the source of a particular type of port
                identifier used in the LLDP MIB.

                The enumeration 'interfaceAlias(1)' represents a port
                identifier based on the ifAlias MIB object, defined in IETF
                RFC 2863.

                The enumeration 'portComponent(2)' represents a port
                identifier based on the value of entPhysicalAlias (defined in
                IETF RFC 2737) for a port component (i.e., entPhysicalClass
                value of 'port(10)'), within the containing chassis.

                The enumeration 'macAddress(3)' represents a port identifier
                based on a unicast source address (encoded in network
                byte order and IEEE 802.3 canonical bit order), which has
                been detected by the agent and associated with a particular
                port (IEEE Std 802-2001).

                The enumeration 'networkAddress(4)' represents a port
                identifier based on a network address, detected by the agent
                and associated with a particular port.

                The enumeration 'interfaceName(5)' represents a port
                identifier based on the ifName MIB object, defined in IETF
                RFC 2863.

                The enumeration 'agentCircuitId(6)' represents a port
                identifier based on the agent-local identifier of the circuit
                (defined in RFC 3046), detected by the agent and associated
                with a particular port.

                The enumeration 'local(7)' represents a port identifier
                based on a value locally assigned."

        SYNTAX INTEGER {
                interfaceAlias(1),
                portComponent(2),
                macAddress(3),
                networkAddress(4),
                interfaceName(5),
                agentCircuitId(6),
                local(7)
        }
	     */
		private int m_type;
  
	    LldpPortIdSubType(Integer chassisIdsubtype) {
	    	m_type = chassisIdsubtype;
	    }

	    protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

	    static {
        	s_typeMap.put(1, "interfaceAlias" );
        	s_typeMap.put(2, "portComponent" );
        	s_typeMap.put(3, "macAddress" );
        	s_typeMap.put(4, "networkAddress" );
        	s_typeMap.put(5, "interfaceName" );
        	s_typeMap.put(6, "agentCircuitId" );
        	s_typeMap.put(7, "local" );
        }

	    /**
	     * <p>ElementIdentifierTypeString</p>
	     *
	     * @return a {@link java.lang.String} object.
	     */
	    /**
	     */
	    public static String getTypeString(Integer code) {
	        if (s_typeMap.containsKey(code))
	                return s_typeMap.get( code);
	        return null;
	    }

        public static LldpPortIdSubType get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException("Cannot create LldpPortIdSubType from null code");
            switch (code) {
            case 1:  	return LLDP_PORTID_SUBTYPE_INTERFACEALIAS;
            case 2:  	return LLDP_PORTID_SUBTYPE_PORTCOMPONENT;
            case 3:  	return LLDP_PORTID_SUBTYPE_MACADDRESS;
            case 4:  	return LLDP_PORTID_SUBTYPE_NETWORKADDRESS;
            case 5:  	return LLDP_PORTID_SUBTYPE_INTERFACENAME;
            case 6:  	return LLDP_PORTID_SUBTYPE_AGENTCIRCUITID;
            case 7:  	return LLDP_PORTID_SUBTYPE_LOCAL;
            default:
                throw new IllegalArgumentException("Cannot create LldpPortIdSubType from code "+code);
            }
        }
        
        public Integer getValue() {
        	return m_type;
        }
        
	}

    private Integer m_id;	
	private OnmsNode m_node;
	
	private LldpPortIdSubType m_lldpPortIdSubType;
	private Integer m_lldpLocalPortNum;
	private Integer m_lldpPortIfindex;
	private String m_lldpPortId;
	private String m_lldpPortDescr;
    
	private String m_lldpRemChassisId;
	private String m_lldpRemSysname;
    private LldpChassisIdSubType m_lldpRemChassisIdSubType;
	private LldpPortIdSubType m_lldpRemPortIdSubType;
	private String m_lldpRemPortId;
	private String m_lldpRemPortDescr;
    private Date m_lldpLinkCreateTime = new Date();
    private Date m_lldpLinkLastPollTime;
	
	public LldpLink() {
	}

    public LldpLink(OnmsNode node, Integer localPortNum, Integer portIfIndex, String portId,
                    String portDescr, LldpPortIdSubType portIdSubType, String remChassisId, String remSysname, LldpChassisIdSubType remChassisIdSubType,
                    String remPortId, LldpPortIdSubType remPortIdSubType, String remPortDescr) {
        setNode(node);
        setLldpLocalPortNum(localPortNum);
        setLldpPortIfindex(portIfIndex);
        setLldpPortId(portId);
        setLldpPortDescr(portDescr);
        setLldpPortIdSubType(portIdSubType);
        setLldpRemChassisId(remChassisId);
        setLldpRemSysname(remSysname);
        setLldpRemChassisIdSubType(remChassisIdSubType);
        setLldpRemPortId(remPortId);
        setLldpRemPortIdSubType(remPortIdSubType);
        setLldpRemPortDescr(remPortDescr);
    }


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

    @Column(name="lldpLocalPortNum", nullable = false)
	public Integer getLldpLocalPortNum() {
		return m_lldpLocalPortNum;
	}

	public void setLldpLocalPortNum(Integer lldpLocalPortNum) {
		m_lldpLocalPortNum = lldpLocalPortNum;
	}



    @Column(name="lldpPortIdSubType", nullable = false)
    @Type(type="org.opennms.netmgt.model.LldpPortIdSubTypeUserType")
	public LldpPortIdSubType getLldpPortIdSubType() {
		return m_lldpPortIdSubType;
	}


	public void setLldpPortIdSubType(LldpPortIdSubType lldpPortIdSubType) {
		m_lldpPortIdSubType = lldpPortIdSubType;
	}

	
    @Column(name="lldpPortId" , length=256, nullable = false)
	public String getLldpPortId() {
		return m_lldpPortId;
	}


	public void setLldpPortId(String lldpPortId) {
		m_lldpPortId = lldpPortId;
	}

    @Column(name="lldpPortDescr" , length=256, nullable = false)
	public String getLldpPortDescr() {
		return m_lldpPortDescr;
	}

	public void setLldpPortDescr(String lldpPortDescr) {
		m_lldpPortDescr = lldpPortDescr;
	}

    @Column(name = "lldpPortIfindex", nullable = true)
	public Integer getLldpPortIfindex() {
		return m_lldpPortIfindex;
	}


	public void setLldpPortIfindex(Integer lldpPortIfindex) {
		m_lldpPortIfindex = lldpPortIfindex;
	}


    @Column(name="lldpRemChassisId" , length=256, nullable = false)
	public String getLldpRemChassisId() {
		return m_lldpRemChassisId;
	}


	public void setLldpRemChassisId(String lldpRemChassisId) {
		m_lldpRemChassisId = lldpRemChassisId;
	}


    @Column(name="lldpRemSysname" , length=256, nullable = false)
	public String getLldpRemSysname() {
		return m_lldpRemSysname;
	}


	public void setLldpRemSysname(String lldpRemSysname) {
		m_lldpRemSysname = lldpRemSysname;
	}


    @Column(name="lldpRemChassisIdSubType", nullable = false)
    @Type(type="org.opennms.netmgt.model.LldpChassisIdSubTypeUserType")
	public LldpChassisIdSubType getLldpRemChassisIdSubType() {
		return m_lldpRemChassisIdSubType;
	}


	public void setLldpRemChassisIdSubType(
			LldpChassisIdSubType lldpRemChassisIdSubType) {
		m_lldpRemChassisIdSubType = lldpRemChassisIdSubType;
	}


    @Column(name="lldpRemPortIdSubType", nullable = false)
    @Type(type="org.opennms.netmgt.model.LldpPortIdSubTypeUserType")
	public LldpPortIdSubType getLldpRemPortIdSubType() {
		return m_lldpRemPortIdSubType;
	}


	public void setLldpRemPortIdSubType(LldpPortIdSubType lldpRemPortIdSubType) {
		m_lldpRemPortIdSubType = lldpRemPortIdSubType;
	}

    @Column(name="lldpRemPortId" , length=256, nullable = false)
	public String getLldpRemPortId() {
		return m_lldpRemPortId;
	}


	public void setLldpRemPortId(String lldpRemPortId) {
		m_lldpRemPortId = lldpRemPortId;
	}


    @Column(name="lldpRemPortDescr" , length=256, nullable = false)
	public String getLldpRemPortDescr() {
		return m_lldpRemPortDescr;
	}


	public void setLldpRemPortDescr(String lldpRemPortDescr) {
		m_lldpRemPortDescr = lldpRemPortDescr;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lldpLinkCreateTime", nullable=false)
	public Date getLldpLinkCreateTime() {
		return m_lldpLinkCreateTime;
	}


	public void setLldpLinkCreateTime(Date lldpLinkCreateTime) {
		m_lldpLinkCreateTime = lldpLinkCreateTime;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lldpLinkLastPollTime", nullable=false)
	public Date getLldpLinkLastPollTime() {
		return m_lldpLinkLastPollTime;
	}


	public void setLldpLinkLastPollTime(Date lldpLinkLastPollTime) {
		m_lldpLinkLastPollTime = lldpLinkLastPollTime;
	}


	public void merge(LldpLink link) {

		setLldpPortId(link.getLldpPortId());
		setLldpPortIdSubType(link.getLldpPortIdSubType());
		setLldpPortDescr(link.getLldpPortDescr());
		setLldpPortIfindex(link.getLldpPortIfindex());
		
		setLldpRemChassisId(link.getLldpRemChassisId());
		setLldpRemChassisIdSubType(link.getLldpRemChassisIdSubType());
		setLldpRemSysname(link.getLldpRemSysname());
		
		setLldpRemPortId(link.getLldpRemPortId());
		setLldpRemPortIdSubType(link.getLldpRemPortIdSubType());
		setLldpRemPortDescr(link.getLldpRemPortDescr());

		setLldpLinkLastPollTime(link.getLldpLinkCreateTime());
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("NodeId", m_node.getId())
			.append("lldpLocalPortNum", m_lldpLocalPortNum)
			.append("lldpPortIdSubType", LldpPortIdSubType.getTypeString(m_lldpPortIdSubType.getValue()))
			.append("lldpPortId", m_lldpPortId)
			.append("lldpPortDescr", m_lldpPortDescr)
			.append("lldpPortIfindex", m_lldpPortIfindex)
			.append("lldpRemChassisId", m_lldpRemChassisId)
			.append("lldpRemChassisSubType",LldpChassisIdSubType.getTypeString(m_lldpRemChassisIdSubType.getValue()))
			.append("lldpRemSysname", m_lldpRemSysname)
			.append("lldpRemPortIdSubType", LldpPortIdSubType.getTypeString(m_lldpRemPortIdSubType.getValue()))
			.append("lldpRemPortId", m_lldpRemPortId)
			.append("lldpRemPortDescr", m_lldpRemPortDescr)
			.append("createTime", m_lldpLinkCreateTime)
			.append("lastPollTime", m_lldpLinkLastPollTime)
.toString();
	}
}
