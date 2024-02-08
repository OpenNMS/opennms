/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd.model;

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
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsNode;

@Entity
@Table(name="bridgeElement")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public class BridgeElement implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3137257592300016141L;

    public enum BridgeDot1dStpProtocolSpecification {
		DOT1D_STP_PROTOCOL_SPECIFICATION_UNKNOWN(1),
		DOT1D_STP_PROTOCOL_SPECIFICATION_DECLB100(2),
		DOT1D_STP_PROTOCOL_SPECIFICATION_IEEE8021D(3),
		DOT1D_STP_PROTOCOL_SPECIFICATION_IEEE8021M(4),
		DOT1D_STP_PROTOCOL_SPECIFICATION_IEEE8021AQ(5);
		private final int m_type;

		BridgeDot1dStpProtocolSpecification(int type) {
			m_type = type;
		}
		
	    protected static final Map<Integer, String> TYPE_MAP = new HashMap<>();

        static {
        	TYPE_MAP.put(1, "unknown" );
        	TYPE_MAP.put(2, "decLb100" );
        	TYPE_MAP.put(3, "ieee802.1d" );
        	TYPE_MAP.put(4, "ieee802.1m" );
        	TYPE_MAP.put(5, "ieee802.1aq" );
        }
        
        public static String getTypeString(Integer code) {
            if (TYPE_MAP.containsKey(code))
                    return TYPE_MAP.get( code);
            return "other-vendor-specific";
        }

        public Integer getValue() {
        	return m_type;
        }

        public static BridgeDot1dStpProtocolSpecification get(Integer code) {
            if (code == null )
                throw new IllegalArgumentException("Cannot create Dot1dStpProtocolSpecification from null code");
            if (code <= 0 )
                throw new IllegalArgumentException("Cannot create Dot1dStpProtocolSpecification from" + code +" code");
            switch (code) {
            case 1: 	return DOT1D_STP_PROTOCOL_SPECIFICATION_UNKNOWN;
            case 2: 	return DOT1D_STP_PROTOCOL_SPECIFICATION_DECLB100;
            case 3: 	return DOT1D_STP_PROTOCOL_SPECIFICATION_IEEE8021D;
            case 4: 	return DOT1D_STP_PROTOCOL_SPECIFICATION_IEEE8021M;
            case 5: 	return DOT1D_STP_PROTOCOL_SPECIFICATION_IEEE8021AQ;
            default:
                throw new IllegalArgumentException("Cannot create Dot1dStpProtocolSpecification from" + code +" code");
            }
        }

		
	}
	
	public enum BridgeDot1dBaseType {
		DOT1DBASETYPE_UNKNOWN(1),
		DOT1DBASETYPE_TRANSPARENT_ONLY(2),
		DOT1DBASETYPE_SOURCEROUTE_ONLY(3),
		DOT1DBASETYPE_SRT(4);
		
		private final int m_type;

		BridgeDot1dBaseType(int type) {
			m_type=type;
		}
		
	    protected static final Map<Integer, String> s_typeMap = new HashMap<>();

        static {
        	s_typeMap.put(1, "unknown" );
        	s_typeMap.put(2, "transparent-only" );
        	s_typeMap.put(3, "sourceroute-only" );
        	s_typeMap.put(4, "srt" );
        }
        
        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                    return s_typeMap.get( code);
            return null;
        }

        public Integer getValue() {
        	return m_type;
        }

        public static BridgeDot1dBaseType get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException("Cannot create Dot1dBaseType from null code");
            switch (code) {
            case 1: 	return DOT1DBASETYPE_UNKNOWN;
            case 2: 	return DOT1DBASETYPE_TRANSPARENT_ONLY;
            case 3: 	return DOT1DBASETYPE_SOURCEROUTE_ONLY;
            case 4: 	return DOT1DBASETYPE_SRT; 		
            default:
                throw new IllegalArgumentException("Cannot create Dot1dBaseType from code "+code);
            }
        }



	}

	private Integer m_id;
	private OnmsNode m_node;
	private String m_baseBridgeAddress;
	private Integer m_baseNumPorts;
	private BridgeDot1dBaseType m_baseType;
	private BridgeDot1dStpProtocolSpecification m_stpProtocolSpecification;
	private Integer m_stpPriority;
	private String m_stpDesignatedRoot;
	private Integer m_stpRootCost;
	private Integer m_stpRootPort;
	private Integer m_vlan;
	private String m_vlanname;
    private Date m_bridgeNodeCreateTime = new Date();
    private Date m_bridgeNodeLastPollTime;
	
	public BridgeElement() {}


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


    @Column(name="baseBridgeAddress",length=12, nullable=false)
	public String getBaseBridgeAddress() {
		return m_baseBridgeAddress;
	}


	public void setBaseBridgeAddress(String baseBridgeAddress) {
		m_baseBridgeAddress = baseBridgeAddress;
	}


    @Column(name="baseNumPorts", nullable = false)
	public Integer getBaseNumPorts() {
		return m_baseNumPorts;
	}


	public void setBaseNumPorts(Integer baseNumPorts) {
		m_baseNumPorts = baseNumPorts;
	}


    @Column(name="baseType", nullable = false)
    @Type(type="org.opennms.netmgt.enlinkd.model.BridgeDot1dBaseTypeUserType")
	public BridgeDot1dBaseType getBaseType() {
		return m_baseType;
	}


	public void setBaseType(BridgeDot1dBaseType baseType) {
		m_baseType = baseType;
	}


    @Column(name="vlan")
	public Integer getVlan() {
		return m_vlan;
	}


	public void setVlan(Integer vlan) {
		m_vlan = vlan;
	}


    @Column(name="vlanname",length=64)
	public String getVlanname() {
		return m_vlanname;
	}


	public void setVlanname(String vlanname) {
		m_vlanname = vlanname;
	}


    @Column(name="stpProtocolSpecification")
    @Type(type="org.opennms.netmgt.enlinkd.model.BridgeDot1dStpProtocolSpecificationUserType")
	public BridgeDot1dStpProtocolSpecification getStpProtocolSpecification() {
		return m_stpProtocolSpecification;
	}

	public void setStpProtocolSpecification(BridgeDot1dStpProtocolSpecification stpProtocolSpecification) {
		m_stpProtocolSpecification = stpProtocolSpecification;
	}


    @Column(name="stpPriority")
	public Integer getStpPriority() {
		return m_stpPriority;
	}


	public void setStpPriority(Integer stpPriority) {
		m_stpPriority = stpPriority;
	}


    @Column(name="stpDesignatedRoot",length=16)
	public String getStpDesignatedRoot() {
		return m_stpDesignatedRoot;
	}


	public void setStpDesignatedRoot(String stpDesignatedRoot) {
		m_stpDesignatedRoot = stpDesignatedRoot;
	}


    @Column(name="stpRootCost")
	public Integer getStpRootCost() {
		return m_stpRootCost;
	}


	public void setStpRootCost(Integer stpRootCost) {
		m_stpRootCost = stpRootCost;
	}


    @Column(name="stpRootPort")
	public Integer getStpRootPort() {
		return m_stpRootPort;
	}


	public void setStpRootPort(Integer stpRootPort) {
		m_stpRootPort = stpRootPort;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="bridgeNodeCreateTime", nullable=false)
	public Date getBridgeNodeCreateTime() {
		return m_bridgeNodeCreateTime;
	}

	public void setBridgeNodeCreateTime(Date bridgeNodeCreateTime) {
		m_bridgeNodeCreateTime = bridgeNodeCreateTime;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="bridgeNodeLastPollTime", nullable=false)
	public Date getBridgeNodeLastPollTime() {
		return m_bridgeNodeLastPollTime;
	}

	public void setBridgeNodeLastPollTime(Date bridgeNodeLastPollTime) {
		m_bridgeNodeLastPollTime = bridgeNodeLastPollTime;
	}


	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("Nodeid", m_node.getId())
                .append("baseBridgeAddress", m_baseBridgeAddress)
				.append("baseNumPorts", m_baseNumPorts)
				.append("baseType",BridgeDot1dBaseType.getTypeString(m_baseType.getValue()))
				.append("stpProtocolSpecification", m_stpProtocolSpecification)
				.append("stpPriority",m_stpPriority)
				.append("stpDesignatedRoot",m_stpDesignatedRoot)
				.append("stpRootCost",m_stpRootCost)
				.append("m_stpRootPort",m_stpRootPort)
				.append("vlan", m_vlan)
				.append("vlanname", m_vlanname)
				.append("m_bridgeNodeCreateTime", m_bridgeNodeCreateTime)
				.append("m_bridgeNodeLastPollTime", m_bridgeNodeLastPollTime)
				.toString();
	}
	
	public void merge(BridgeElement element) {
		//nodeid and vlan are unique primary key
		if (element == null)
			return;
		
		setBaseBridgeAddress(element.getBaseBridgeAddress());
		setBaseNumPorts(element.getBaseNumPorts());
		setBaseType(element.getBaseType());
		
		setStpProtocolSpecification(element.getStpProtocolSpecification());
		setStpPriority(element.getStpPriority());
		setStpDesignatedRoot(element.getStpDesignatedRoot());
		setStpRootCost(element.getStpRootCost());
		setStpRootPort(element.getStpRootPort());
		
		setBridgeNodeLastPollTime(element.getBridgeNodeCreateTime());
	}
	
}
