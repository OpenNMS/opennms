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
import org.opennms.netmgt.model.topology.Topology;

@Entity
@Table(name="bridgeMacLink")
public class BridgeMacLink implements Topology {

    
    public enum BridgeMacLinkType {
        BRIDGE_LINK(1), BRIDGE_FORWARDER(2);

        private int m_type;

        BridgeMacLinkType(int type) {
            m_type = type;
        }

        protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

        static {
            s_typeMap.put(1, "bridge-link");
            s_typeMap.put(2, "bridge-forwarder");
        }

        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                return s_typeMap.get(code);
            return null;
        }

        public Integer getValue() {
            return m_type;
        }

        public static BridgeMacLinkType get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException(
                                                   "Cannot create BridgeMacLinkType from null code");
            switch (code) {
            case 1:
                return BRIDGE_LINK;
            case 2:
                return BRIDGE_FORWARDER;
            default:
                throw new IllegalArgumentException(
                                                   "Cannot create BridgeMacLinkType from code "
                                                           + code);
            }
        }

    }

    private Integer m_id;
    private OnmsNode m_node;
    private Integer m_bridgePort;
    private Integer m_bridgePortIfIndex;
    private String  m_bridgePortIfName;
    private String m_macAddress;
    private Integer m_vlan;
    private BridgeMacLinkType m_linkType;
    private Date m_bridgeMacLinkCreateTime = new Date();
    private Date m_bridgeMacLinkLastPollTime;
	
	public BridgeMacLink() {}


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

    @Column(name="bridgePort", nullable = false)
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

    @Column(name="macAddress",length=12, nullable=false)
	public String getMacAddress() {
		return m_macAddress;
	}


	public void setMacAddress(String macAddress) {
		m_macAddress = macAddress;
	}

    @Column(name = "linkType", nullable = false)
    @Type(type = "org.opennms.netmgt.model.BridgeMacLinkTypeUserType")
    public BridgeMacLinkType getLinkType() {
        return m_linkType;
    }

    public void setLinkType(BridgeMacLinkType linkType) {
        m_linkType = linkType;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="bridgeMacLinkCreateTime", nullable=false)
	public Date getBridgeMacLinkCreateTime() {
		return m_bridgeMacLinkCreateTime;
	}


	public void setBridgeMacLinkCreateTime(Date bridgeMacLinkCreateTime) {
		m_bridgeMacLinkCreateTime = bridgeMacLinkCreateTime;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="bridgeMacLinkLastPollTime", nullable=false)
	public Date getBridgeMacLinkLastPollTime() {
		return m_bridgeMacLinkLastPollTime;
	}


	public void setBridgeMacLinkLastPollTime(Date bridgeMacLinkLastPollTime) {
		m_bridgeMacLinkLastPollTime = bridgeMacLinkLastPollTime;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("Nodeid", m_node.getId())
				.append("bridgePort", m_bridgePort)
				.append("bridgePortIfIndex", m_bridgePortIfIndex)
				.append("bridgePortIfName", m_bridgePortIfName)
				.append("vlan", m_vlan)
                                .append("macAddress", m_macAddress)
                                .append("linktype", BridgeMacLinkType.
                                        getTypeString(
                                                      getLinkType().
                                                      getValue()))
				.append("m_bridgeMacLinkCreateTime", m_bridgeMacLinkCreateTime)
				.append("m_bridgeMacLinkLastPollTime", m_bridgeMacLinkLastPollTime)
				.toString();
	}
	
	@Transient
        public String printTopology() {
        StringBuffer strbfr = new StringBuffer();

        strbfr.append("maclink: nodeid:["); 
        strbfr.append(getNode().getId());
        strbfr.append("], bridgeport:[");
        strbfr.append(getBridgePort());
        strbfr.append("], ifindex:[");
        strbfr.append(getBridgePortIfIndex());
        strbfr.append("], vlan:[");
        strbfr.append(getVlan());
        strbfr.append("],");
        strbfr.append(getMacAddress());
        strbfr.append(",");
        strbfr.append(BridgeMacLinkType.
                      getTypeString(
                                    getLinkType().
                                    getValue()));
        strbfr.append("]");

	        return strbfr.toString();
	        }

	public void merge(BridgeMacLink element) {
		if (element == null)
			return;
		setBridgePortIfIndex(element.getBridgePortIfIndex());
		setBridgePortIfName(element.getBridgePortIfName());
		setVlan(element.getVlan());
		setLinkType(element.getLinkType());
		if (element.getBridgeMacLinkLastPollTime() == null)
		    setBridgeMacLinkLastPollTime(element.getBridgeMacLinkCreateTime());
		else 
		    setBridgeMacLinkLastPollTime(element.getBridgeMacLinkLastPollTime());
	}
	
}
