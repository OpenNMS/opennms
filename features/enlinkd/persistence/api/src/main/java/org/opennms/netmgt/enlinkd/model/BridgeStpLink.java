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
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsNode;

@Entity
@Table(name="bridgeStpLink")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public class BridgeStpLink implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5550390569444249173L;

    public enum BridgeDot1dStpPortState {
		DOT1D_STP_PORT_STATUS_DISABLED(1),
    	DOT1D_STP_PORT_STATUS_BLOCKING(2),
    	DOT1D_STP_PORT_STATUS_LISTENING(3),
    	DOT1D_STP_PORT_STATUS_LEARNING(4),
    	DOT1D_STP_PORT_STATUS_FORWARDING(5),
    	DOT1D_STP_PORT_STATUS_BROKEN(6);

		private final int m_type;

		BridgeDot1dStpPortState(int type) {
			m_type = type;
		}
		
        private static final Map<Integer, String> s_stpPortStatusMap = new HashMap<>();
        
        static {
            s_stpPortStatusMap.put(1, "disabled" );
            s_stpPortStatusMap.put(2, "blocking" );
            s_stpPortStatusMap.put(3, "listening" );
            s_stpPortStatusMap.put(4, "learning" );
            s_stpPortStatusMap.put(5, "forwarding" );
            s_stpPortStatusMap.put(6, "broken" );
        }
        
        public static String getTypeString(Integer code) {
            if (s_stpPortStatusMap.containsKey(code))
                    return s_stpPortStatusMap.get( code);
            return null;
        }

        public Integer getValue() {
        	return m_type;
        }

        public static BridgeDot1dStpPortState get(Integer code) {
            if (code == null )
                throw new IllegalArgumentException("Cannot create Dot1dStpPortState from null code");
            switch (code) {
            case 1: 	return DOT1D_STP_PORT_STATUS_DISABLED;
            case 2: 	return DOT1D_STP_PORT_STATUS_BLOCKING;
            case 3: 	return DOT1D_STP_PORT_STATUS_LISTENING;
            case 4: 	return DOT1D_STP_PORT_STATUS_LEARNING;
            case 5: 	return DOT1D_STP_PORT_STATUS_FORWARDING;
            case 6: 	return DOT1D_STP_PORT_STATUS_BROKEN;
                       default:
            	throw new IllegalArgumentException("Cannot create Dot1dStpPortState from code "+code);
            }
        }
		
		
	}
	
	public enum BridgeDot1dStpPortEnable {
		DOT1D_STP_PORT_ENABLED(1),
		DOT1D_STP_PORT_DISABLED(2);
		
		private final int m_type;

		BridgeDot1dStpPortEnable(int type) {
			m_type = type;
		}
		
        private static final Map<Integer, String> s_stpPortenableMap = new HashMap<>();
        
        static {
            s_stpPortenableMap.put(1, "enabled" );
            s_stpPortenableMap.put(2, "disabled" );
        }
        
        public static String getTypeString(Integer code) {
            if (s_stpPortenableMap.containsKey(code))
                    return s_stpPortenableMap.get( code);
            return null;
        }

        public Integer getValue() {
        	return m_type;
        }

        public static BridgeDot1dStpPortEnable get(Integer code) {
            if (code == null )
                throw new IllegalArgumentException("Cannot create Dot1dStpPortEnable from null code");
            switch (code) {
            case 1: 	return DOT1D_STP_PORT_ENABLED;
            case 2: 	return DOT1D_STP_PORT_DISABLED;
                       default:
            	throw new IllegalArgumentException("Cannot create Dot1dStpPortEnable from code "+code);
            }
        }
		
		
		
	}
	private Integer m_id;
	private OnmsNode m_node;

	private Integer m_stpPort;
	private Integer m_stpPortPriority;
	private BridgeDot1dStpPortState m_stpPortState;
	private BridgeDot1dStpPortEnable m_stpPortEnable;
	private Integer m_stpPortPathCost;
	private Integer m_designatedCost;
	private String m_designatedRoot;
	private String m_designatedBridge;
	private String m_designatedPort;

	private Date m_bridgeStpLinkCreateTime = new Date();
    private Date m_bridgeStpLinkLastPollTime;

    private Integer m_stpPortIfIndex;
	private String  m_stpPortIfName;
	private Integer m_vlan;
	
	public BridgeStpLink() {}


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

    @Column(name="stpPort", nullable = false)
	public Integer getStpPort() {
		return m_stpPort;
	}

	public void setStpPort(Integer bridgePort) {
		m_stpPort = bridgePort;
	}

    @Column(name="stpPortPriority", nullable = false)
	public Integer getStpPortPriority() {
		return m_stpPortPriority;
	}


	public void setStpPortPriority(Integer stpPortPriority) {
		m_stpPortPriority = stpPortPriority;
	}


    @Column(name="stpPortState", nullable = false)
    @Type(type="org.opennms.netmgt.enlinkd.model.BridgeDot1dStpPortStateUserType")
	public BridgeDot1dStpPortState getStpPortState() {
		return m_stpPortState;
	}


	public void setStpPortState(BridgeDot1dStpPortState stpPortState) {
		m_stpPortState = stpPortState;
	}


    @Column(name="stpPortEnable", nullable = false)
    @Type(type="org.opennms.netmgt.enlinkd.model.BridgeDot1dStpPortEnableUserType")
	public BridgeDot1dStpPortEnable getStpPortEnable() {
		return m_stpPortEnable;
	}


	public void setStpPortEnable(BridgeDot1dStpPortEnable stpPortEnable) {
		m_stpPortEnable = stpPortEnable;
	}

    @Column(name="stpPortPathCost", nullable = false)
	public Integer getStpPortPathCost() {
		return m_stpPortPathCost;
	}


	public void setStpPortPathCost(Integer stpPortPathCost) {
		m_stpPortPathCost = stpPortPathCost;
	}

    @Column(name="stpPortIfIndex")
	public Integer getStpPortIfIndex() {
		return m_stpPortIfIndex;
	}


	public void setStpPortIfIndex(Integer bridgePortIfIndex) {
		m_stpPortIfIndex = bridgePortIfIndex;
	}


    @Column(name = "stpPortIfName", length = 32)
	public String getStpPortIfName() {
		return m_stpPortIfName;
	}


	public void setStpPortIfName(String bridgePortIfName) {
		m_stpPortIfName = bridgePortIfName;
	}

	@Column(name="vlan")
	public Integer getVlan() {
		return m_vlan;
	}


	public void setVlan(Integer vlan) {
		m_vlan = vlan;
	}


    @Column(name="designatedRoot",length=16, nullable=false)
	public String getDesignatedRoot() {
		return m_designatedRoot;
	}
   
    @Transient
    public String getDesignatedRootAddress() {
    	return InetAddressUtils.getBridgeAddressFromStpBridgeId(m_designatedRoot);
    }


	public void setDesignatedRoot(String designatedRoot) {
		m_designatedRoot = designatedRoot;
	}


	@Column(name="designatedCost", nullable = false)
	public Integer getDesignatedCost() {
		return m_designatedCost;
	}


	public void setDesignatedCost(Integer designatedCost) {
		m_designatedCost = designatedCost;
	}	

    @Column(name="designatedBridge",length=16)
	public String getDesignatedBridge() {
		return m_designatedBridge;
	}

    @Transient
    public String getDesignatedBridgeAddress() {
    	return InetAddressUtils.getBridgeAddressFromStpBridgeId(m_designatedBridge);
    }
    
	public void setDesignatedBridge(String macAddress) {
		m_designatedBridge = macAddress;
	}


    @Column(name="designatedPort", length=4, nullable = false)
	public String getDesignatedPort() {
		return m_designatedPort;
	}

    @Transient
    public int getDesignatedBridgePortNum() {
    	return InetAddressUtils.getBridgeDesignatedPortNumber(m_designatedPort);
    }

    public void setDesignatedPort(String bridgePort) {
		m_designatedPort = bridgePort;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="bridgeStpLinkCreateTime", nullable=false)
	public Date getBridgeStpLinkCreateTime() {
		return m_bridgeStpLinkCreateTime;
	}


	public void setBridgeStpLinkCreateTime(Date bridgeLinkCreateTime) {
		m_bridgeStpLinkCreateTime = bridgeLinkCreateTime;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="bridgeStpLinkLastPollTime", nullable=false)
	public Date getBridgeStpLinkLastPollTime() {
		return m_bridgeStpLinkLastPollTime;
	}


	public void setBridgeStpLinkLastPollTime(Date bridgeLinkLastPollTime) {
		m_bridgeStpLinkLastPollTime = bridgeLinkLastPollTime;
	}
	
	@Override
	public String toString() {
		Integer nodeid = null;
		if (m_node != null)
			nodeid = m_node.getId();
		return new ToStringBuilder(this)
				.append("Nodeid", nodeid)
				.append("stpPortState", m_stpPortState)
				.append("stpPortEnable", m_stpPortEnable)
				.append("stpPort", m_stpPort)
				.append("stpPortIfIndex", m_stpPortIfIndex)
				.append("stpPortIfName", m_stpPortIfName)
				.append("vlan", m_vlan)
				.append("designatedRoot", m_designatedRoot)
				.append("designatedCost", m_designatedCost)
				.append("designatedBridge", m_designatedBridge)
				.append("designatedPort", m_designatedPort)
				.append("m_bridgeStpLinkCreateTime", m_bridgeStpLinkCreateTime)
				.append("m_bridgeStpLinkLastPollTime", m_bridgeStpLinkLastPollTime)
				.toString();
	}
	
	public void merge(BridgeStpLink element) {
		if (element == null)
			return;
		
		setStpPortState(element.getStpPortState());
		setStpPortEnable(element.getStpPortEnable());
		setStpPortIfIndex(element.getStpPortIfIndex());
		setStpPortIfName(element.getStpPortIfName());
		setVlan(element.getVlan());

		setDesignatedRoot(element.getDesignatedRoot());
		setDesignatedCost(element.getDesignatedCost());
		setDesignatedBridge(element.getDesignatedBridge());
		setDesignatedPort(element.getDesignatedPort());
		setBridgeStpLinkLastPollTime(element.getBridgeStpLinkCreateTime());
	}

}
