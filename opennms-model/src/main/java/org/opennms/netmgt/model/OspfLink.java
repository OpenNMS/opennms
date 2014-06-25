package org.opennms.netmgt.model;

import java.io.Serializable;
import java.net.InetAddress;
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
import org.hibernate.annotations.Type;

import static org.opennms.core.utils.InetAddressUtils.str;
@Entity
@Table(name="ospfLink")
public class OspfLink implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3798160983917807494L;
	private Integer m_id;	
	private OnmsNode m_node;

	private InetAddress m_ospfIpAddr;
	private InetAddress m_ospfIpMask; 
	private Integer m_ospfIfIndex;
	private Integer m_ospfAddressLessIndex;
	
	private InetAddress m_ospfRemRouterId;
	private InetAddress m_ospfRemIpAddr;
	private Integer m_ospfRemAddressLessIndex;

	private Date m_ospfLinkCreateTime = new Date();
    private Date m_ospfLinkLastPollTime;

    public OspfLink(){}

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

    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @Column(name="ospfIpAddr",nullable=true)
	public InetAddress getOspfIpAddr() {
		return m_ospfIpAddr;
	}
    
    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @Column(name="ospfIpMask",nullable=true)
	public InetAddress getOspfIpMask() {
		return m_ospfIpMask;
	}

    @Column(name="ospfAddressLessIndex",nullable=true)
	public Integer getOspfAddressLessIndex() {
		return m_ospfAddressLessIndex;
	}

    @Column(name="ospfIfIndex",nullable=true)
	public Integer getOspfIfIndex() {
		return m_ospfIfIndex;
	}

    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @Column(name="ospfRemRouterId",nullable=false)
    public InetAddress getOspfRemRouterId() {
		return m_ospfRemRouterId;
	}

    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @Column(name="ospfRemIpAddr",nullable=false)
	public InetAddress getOspfRemIpAddr() {
		return m_ospfRemIpAddr;
	}

    @Column(name="ospfRemAddressLessIndex",nullable=false)
	public Integer getOspfRemAddressLessIndex() {
		return m_ospfRemAddressLessIndex;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ospfLinkCreateTime", nullable=false)
	public Date getOspfLinkCreateTime() {
		return m_ospfLinkCreateTime;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ospfLinkLastPollTime", nullable=false)
	public Date getOspfLinkLastPollTime() {
		return m_ospfLinkLastPollTime;
	}


	public void setOspfIpAddr(InetAddress ospfIpAddr) {
		m_ospfIpAddr = ospfIpAddr;
	}

	public void setOspfIpMask(InetAddress ospfIpMask) {
		m_ospfIpMask = ospfIpMask;
	}
	
	public void setOspfIfIndex(Integer ospfIfIndex) {
		m_ospfIfIndex = ospfIfIndex;
	}

	public void setOspfAddressLessIndex(Integer ospfAddressLessIndex) {
		m_ospfAddressLessIndex = ospfAddressLessIndex;
	}
	
	public void setOspfRemRouterId(InetAddress ospfRemRouterId) {
		m_ospfRemRouterId = ospfRemRouterId;
	}

	public void setOspfRemIpAddr(InetAddress ospfRemIpAddr) {
		m_ospfRemIpAddr = ospfRemIpAddr;
	}

	public void setOspfRemAddressLessIndex(Integer ospfRemAddressLessIndex) {
		m_ospfRemAddressLessIndex = ospfRemAddressLessIndex;
	}

	public void setOspfLinkCreateTime(Date ospfLinkCreateTime) {
		m_ospfLinkCreateTime = ospfLinkCreateTime;
	}

	public void setOspfLinkLastPollTime(Date ospfLinkLastPollTime) {
		m_ospfLinkLastPollTime = ospfLinkLastPollTime;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("NodeId", m_node.getId())
			.append("ospfIpAddr", str(m_ospfIpAddr))
			.append("ospfAddressLessIndex", m_ospfAddressLessIndex)
			.append("ospfIpMask", str(m_ospfIpMask))
			.append("ospfIfIndex", m_ospfIfIndex)
			.append("ospfRemRouterId", str(m_ospfRemRouterId))
			.append("ospfRemIpAddr", str(m_ospfRemIpAddr))
			.append("ospfRemAddressLessIndex", m_ospfRemAddressLessIndex)
			.append("createTime", m_ospfLinkCreateTime)
			.append("lastPollTime", m_ospfLinkLastPollTime)
			.toString();
	}
	
	public void merge(OspfLink link) {
		if (link == null)
			return;
		setOspfIpAddr(link.getOspfIpAddr());
		setOspfIpMask(link.getOspfIpMask());
		setOspfIfIndex(link.getOspfIfIndex());
		setOspfAddressLessIndex(link.getOspfAddressLessIndex());
		
		setOspfRemRouterId(link.getOspfRemRouterId());
		setOspfRemIpAddr(link.getOspfRemIpAddr());
		setOspfRemAddressLessIndex(link.getOspfRemAddressLessIndex());
		
		setOspfLinkLastPollTime(link.getOspfLinkCreateTime());
	}

}
