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
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.opennms.netmgt.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.model.topology.Topology;

@Entity
@Table(name="isisLink")
public class IsIsLink implements Serializable,Topology {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3813247749765614567L;

    public enum IsisISAdjState {
        down(1),
        initializing(2),
        up(3),
        failed(4);
        private int m_value;

        IsisISAdjState(int value) {
        	m_value=value;
        }
 	    protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

        static {
        	s_typeMap.put(1, "down" );
        	s_typeMap.put(2, "initializing" );
        	s_typeMap.put(3, "up" );
        	s_typeMap.put(4, "failed" );
        }

        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                    return s_typeMap.get( code);
            return null;
        }
        
        public static IsisISAdjState get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException("Cannot create IsisISAdjState from null code");
            switch (code) {
            case 1: 	return down;
            case 2: 	return initializing;
            case 3: 	return up;
            case 4: 	return failed;
            default:
                throw new IllegalArgumentException("Cannot create IsisISAdjState from code "+code);
            }
        }
        
        public Integer getValue() {
            return m_value;
        }
        
    }
    public enum IsisISAdjNeighSysType {
        l1_IntermediateSystem(1),
        l2IntermediateSystem(2),
        l1L2IntermediateSystem(3),
        unknown(4);
        private int m_value;

        IsisISAdjNeighSysType(int value) {
        	m_value=value;
        }
 	    protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

        static {
        	s_typeMap.put(1, "l1_IntermediateSystem" );
        	s_typeMap.put(2, "l2IntermediateSystem" );
        	s_typeMap.put(3, "l1L2IntermediateSystem" );
        	s_typeMap.put(4, "unknown" );
        }

        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                    return s_typeMap.get( code);
            return null;
        }
        
        public static IsisISAdjNeighSysType get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException("Cannot create IsisISAdjNeighSysType from null code");
            switch (code) {
            case 1: 	return l1_IntermediateSystem;
            case 2: 	return l2IntermediateSystem;
            case 3: 	return l1L2IntermediateSystem;
            case 4: 	return unknown;
            default:
                throw new IllegalArgumentException("Cannot create IsisISAdjNeighSysType from code "+code);
            }
        }
        
        public Integer getValue() {
            return m_value;
        }
        
    }


    private Integer m_id;	
	private OnmsNode m_node;
	
	private Integer m_isisCircIndex;
	private Integer m_isisISAdjIndex;
	private Integer m_isisCircIfIndex;
	private IsisAdminState m_isisCircAdminState;
    
	private IsisISAdjState m_isisISAdjState;
	private String m_isisISAdjNeighSNPAAddress;
	private IsisISAdjNeighSysType m_isisISAdjNeighSysType;
	private String m_isisISAdjNeighSysID;
	private Integer m_isisISAdjNbrExtendedCircID;
    
	private Date m_isisLinkCreateTime = new Date();
    private Date m_isisLinkLastPollTime;
	
	public IsIsLink() {
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

    @Column(name="isisCircIndex", nullable = false)
	public Integer getIsisCircIndex() {
		return m_isisCircIndex;
	}

	public void setIsisCircIndex(Integer isisCircIndex) {
		m_isisCircIndex = isisCircIndex;
	}

    @Column(name="isisISAdjIndex", nullable = false)
	public Integer getIsisISAdjIndex() {
		return m_isisISAdjIndex;
	}

	public void setIsisISAdjIndex(Integer isisISAdjIndex) {
		m_isisISAdjIndex = isisISAdjIndex;
	}

    @Column(name="isisCircIfIndex", nullable = true)
	public Integer getIsisCircIfIndex() {
		return m_isisCircIfIndex;
	}

	public void setIsisCircIfIndex(Integer isisIfCircIndex) {
		m_isisCircIfIndex = isisIfCircIndex;
	}

    @Column(name="isisCircAdminState", nullable = true)
    @Type(type="org.opennms.netmgt.model.IsIsAdminStateUserType")
	public IsisAdminState getIsisCircAdminState() {
		return m_isisCircAdminState;
	}


	public void setIsisCircAdminState(IsisAdminState isisCircAdminState) {
		m_isisCircAdminState = isisCircAdminState;
	}

    @Column(name="isisISAdjState", nullable = false)
    @Type(type="org.opennms.netmgt.model.IsIsISAdjStateUserType")
	public IsisISAdjState getIsisISAdjState() {
		return m_isisISAdjState;
	}


	public void setIsisISAdjState(IsisISAdjState isisISAdjState) {
		m_isisISAdjState = isisISAdjState;
	}

    @Column(name="isisISAdjNeighSNPAAddress" , length=80, nullable = false)
	public String getIsisISAdjNeighSNPAAddress() {
		return m_isisISAdjNeighSNPAAddress;
	}


	public void setIsisISAdjNeighSNPAAddress(String isisISAdjNeighSNPAAddress) {
		m_isisISAdjNeighSNPAAddress = isisISAdjNeighSNPAAddress;
	}


    @Column(name="isisISAdjNeighSysType", nullable = false)
    @Type(type="org.opennms.netmgt.model.IsIsISAdjNeighSysTypeUserType")
	public IsisISAdjNeighSysType getIsisISAdjNeighSysType() {
		return m_isisISAdjNeighSysType;
	}


	public void setIsisISAdjNeighSysType(IsisISAdjNeighSysType isisISAdjNeighSysType) {
		m_isisISAdjNeighSysType = isisISAdjNeighSysType;
	}


    @Column(name="isisISAdjNeighSysID" , length=32, nullable = false)
	public String getIsisISAdjNeighSysID() {
		return m_isisISAdjNeighSysID;
	}


	public void setIsisISAdjNeighSysID(String isisISAdjNeighSysID) {
		m_isisISAdjNeighSysID = isisISAdjNeighSysID;
	}

    @Column(name="isisISAdjNbrExtendedCircID", nullable = false)
	public Integer getIsisISAdjNbrExtendedCircID() {
		return m_isisISAdjNbrExtendedCircID;
	}

	public void setIsisISAdjNbrExtendedCircID(Integer isisISAdjNbrExtendedCircID) {
		m_isisISAdjNbrExtendedCircID = isisISAdjNbrExtendedCircID;
	}
	
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="isisLinkCreateTime", nullable=false)
	public Date getIsisLinkCreateTime() {
		return m_isisLinkCreateTime;
	}


	public void setIsisLinkCreateTime(Date isisLinkCreateTime) {
		m_isisLinkCreateTime = isisLinkCreateTime;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="isisLinkLastPollTime", nullable=false)
	public Date getIsisLinkLastPollTime() {
		return m_isisLinkLastPollTime;
	}


	public void setIsisLinkLastPollTime(Date isisLinkLastPollTime) {
		m_isisLinkLastPollTime = isisLinkLastPollTime;
	}


	public void merge(IsIsLink link) {
		
		setIsisCircIfIndex(link.getIsisCircIfIndex());
		setIsisCircAdminState(link.getIsisCircAdminState());
		
		setIsisISAdjState(link.getIsisISAdjState());
		setIsisISAdjNeighSNPAAddress(link.getIsisISAdjNeighSNPAAddress());
		setIsisISAdjNeighSysType(link.getIsisISAdjNeighSysType());
		setIsisISAdjNeighSysID(link.getIsisISAdjNeighSysID());
		setIsisISAdjNbrExtendedCircID(link.getIsisISAdjNbrExtendedCircID());
	
		setIsisLinkLastPollTime(link.getIsisLinkCreateTime());
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("NodeId", m_node.getId())
			.append("isisCircIndex", m_isisCircIndex)
			.append("isisISAdjIndex", m_isisISAdjIndex)
			.append("isisCircIfIndex", m_isisCircIfIndex)
			.append("isisCircAdminState", IsisAdminState.getTypeString(m_isisCircAdminState.getValue()))
			.append("isisISAdjState", IsisISAdjState.getTypeString(m_isisISAdjState.getValue()))
			.append("isisISAdjNeighSNPAAddress", m_isisISAdjNeighSNPAAddress)
			.append("isisISAdjNeighSysType", IsisISAdjNeighSysType.getTypeString(m_isisISAdjNeighSysType.getValue()))
			.append("isisISAdjNeighSysID", m_isisISAdjNeighSysID)
			.append("isisISAdjNbrExtendedCircID", m_isisISAdjNbrExtendedCircID)
			.append("createTime", m_isisLinkCreateTime)
			.append("lastPollTime", m_isisLinkLastPollTime)
			.toString();
	}

        @Transient
        public String printTopology() {
            StringBuffer strb = new StringBuffer();
                strb.append("isislink: nodeid:["); 
                strb.append(getNode().getId());
                strb.append("]. circIndex:[ ");
                strb.append(getIsisCircIndex());
                strb.append("], ifindex:[");
                strb.append(getIsisCircIfIndex());
                strb.append("], AdminState:[");
                strb.append(IsisAdminState.getTypeString(getIsisCircAdminState().getValue()));
                strb.append("], ISAdjNeighSysID:[");
                strb.append(getIsisISAdjNeighSysID());
                strb.append("], ISAdjNeighSNPAAddress:[");
                strb.append(getIsisISAdjNeighSNPAAddress());
                strb.append("], ISAdjState:[");
                strb.append(IsisISAdjState.getTypeString(getIsisISAdjState().getValue()));
                strb.append("]");

            return strb.toString();
        }


}
