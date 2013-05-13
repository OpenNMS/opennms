/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.annotations.Type;

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.xml.bind.StatusTypeXmlAdapter;

@XmlRootElement(name = "link")
@Entity
@Table(name = "datalinkinterface")
@XmlAccessorType(XmlAccessType.NONE)
public class DataLinkInterface  implements Serializable, Comparable<DataLinkInterface> {
    private static final long serialVersionUID = -3336726327359373609L;

    private Integer m_id;
    private OnmsNode m_node;

    @Column(name="ifindex", nullable=false)
    private Integer m_ifIndex;
    @Column(name="nodeparentid", nullable=false)
    private Integer m_nodeParentId;
    @Column(name="parentifindex", nullable=false)
    private Integer m_parentIfIndex;
    @Column(name="status", length=1, nullable=false)
    private StatusType m_status;
    @Column(name="linktypeid", nullable=true)
    private Integer m_linkTypeId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastpolltime", nullable=false)
    private Date m_lastPollTime;
    @Column(name="source", nullable=false)
    private String m_source = "linkd";

    /** work around a marshalling issue by storing the OnmsNode nodeId **/
    @Transient
    private Integer m_nodeId = null;

    public DataLinkInterface() {
    }

    public DataLinkInterface(final OnmsNode node, final int ifIndex, final int nodeParentId, final int parentIfIndex, final StatusType status, final Date lastPollTime) {
        m_node = node;
        m_ifIndex = ifIndex;
        m_nodeParentId = nodeParentId;
        m_parentIfIndex = parentIfIndex;
        m_status = status;
        m_lastPollTime = lastPollTime;
        m_linkTypeId = -1;
    }

    public DataLinkInterface(final OnmsNode node, final int ifIndex, final int nodeParentId, final int parentIfIndex, final Date lastPollTime) {
        m_node = node;
        m_ifIndex = ifIndex;
        m_nodeParentId = nodeParentId;
        m_parentIfIndex = parentIfIndex;
        m_status = StatusType.UNKNOWN;
        m_lastPollTime = lastPollTime;
        m_linkTypeId = -1;
    }

    @XmlTransient
    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    public void setId(final int id) {
        m_id = id;
    }

    /**
     * Get the ID as a string.  This exists only for XML serialization.
     */
    @XmlID
    @XmlAttribute(name="id")
    @Transient
    public String getDataLinkInterfaceId() {
        return getId().toString();
    }

    public void setDataLinkInterfaceId(final String id) {
        m_id = Integer.valueOf(id);
    }

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    @XmlTransient
    public OnmsNode getNode() {
        return m_node;
    }

    public void setNode(final OnmsNode node) {
        m_node = node;
    }

    @Transient
    @XmlElement(name="nodeId")
    public Integer getNodeId() {
        return m_node == null? m_nodeId : m_node.getId();
    }

    public void setNodeId(final Integer nodeId) {
        m_nodeId = nodeId;
    }

    @XmlElement(name="ifIndex")
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    public void setIfIndex(final Integer ifIndex) {
        m_ifIndex = ifIndex;
    }

    @XmlElement(name="nodeParentId")
    public Integer getNodeParentId() {
        return m_nodeParentId;
    }

    public void setNodeParentId(final Integer nodeParentId) {
        m_nodeParentId = nodeParentId;
    }

    @XmlElement(name="parentIfIndex")
    public Integer getParentIfIndex() {
        return m_parentIfIndex;
    }

    public void setParentIfIndex(final Integer parentIfIndex) {
        m_parentIfIndex = parentIfIndex;
    }

    @XmlAttribute(name="status")
    @Type(type="org.opennms.netmgt.model.StatusTypeUserType")
    @XmlJavaTypeAdapter(StatusTypeXmlAdapter.class)
    public StatusType getStatus() {
        return m_status;
    }

    public void setStatus(final StatusType status) {
        m_status = status;
    }

    @XmlElement(name="linkTypeId")
    public Integer getLinkTypeId() {
        return m_linkTypeId;
    }

    public void setLinkTypeId(final Integer linkTypeId) {
        m_linkTypeId = linkTypeId;
    }

    @XmlElement(name="lastPollTime")
    public Date getLastPollTime() {
        return m_lastPollTime;
    }

    public void setLastPollTime(final Date lastPollTime) {
        m_lastPollTime = lastPollTime;
    }
    
    @XmlAttribute(name="source")
    public String getSource() {
        return m_source;
    }
    
    public void setSource(final String source) {
        m_source = source;
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.DataLinkInterface} object.
     * @return a int.
     */
    @Override
    public int compareTo(final DataLinkInterface o) {
        return new CompareToBuilder()
            .append(getId(), o.getId())
            .append(getNode(), o.getNode())
            .append(getIfIndex(), o.getIfIndex())
            .append(getSource(), o.getSource())
            .append(getNodeParentId(), o.getNodeParentId())
            .append(getParentIfIndex(), o.getParentIfIndex())
            .append(getStatus(), o.getStatus())
            .append(getLastPollTime(), o.getLastPollTime())
            .append(getLinkTypeId(), o.getLinkTypeId())
            .toComparison();
    }

}
