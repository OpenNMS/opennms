package org.opennms.netmgt.model;

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

import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name="bridgeMacLink")
public class BridgeMacLink {

	private Integer m_id;
	private OnmsNode m_node;
	private Integer m_bridgePort;
	private Integer m_bridgePortIfIndex;
	private String  m_bridgePortIfName;
	private String m_macAddress;
	private Integer m_vlan;
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
				.append("m_bridgeMacLinkCreateTime", m_bridgeMacLinkCreateTime)
				.append("m_bridgeMacLinkLastPollTime", m_bridgeMacLinkLastPollTime)
				.toString();
	}
	
	public void merge(BridgeMacLink element) {
		if (element == null)
			return;
		setVlan(element.getVlan());
		
		setBridgeMacLinkLastPollTime(element.getBridgeMacLinkCreateTime());
	}


	
}
