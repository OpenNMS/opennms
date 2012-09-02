/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name = "link")
@Entity
@Table(name = "datalinkinterface")
public class DataLinkInterface  implements Serializable, Comparable<DataLinkInterface> {
    private static final long serialVersionUID = 5241963830563150843L;

    private Integer m_id;
    private OnmsNode m_node;

    @Column(name="ifindex", nullable=false)
    private Integer m_ifIndex;
    @Column(name="nodeparentid", nullable=false)
    private Integer m_nodeParentId;
    @Column(name="parentifindex", nullable=false)
    private Integer m_parentIfIndex;
    @Column(name="status", length=1, nullable=false)
    private String m_status;
    @Column(name="linktypeid", nullable=true)
    private Integer m_linkTypeId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastpolltime", nullable=false)
    private Date m_lastPollTime;

    public DataLinkInterface() {
    }

    /**
     * <p>Constructor for DataLinkInterface.</p>
     *
     * @param nodeId a int.
     * @param ifIndex a int.
     * @param nodeParentId a int.
     * @param parentIfIndex a int.
     * @param status a {@link java.lang.String} object.
     * @param lastPollTime a {@link java.util.Date} object.
     */
    public DataLinkInterface(final OnmsNode node, final int ifIndex, final int nodeParentId, final int parentIfIndex, final String status, final Date lastPollTime) {
        m_node = node;
        m_ifIndex = ifIndex;
        m_nodeParentId = nodeParentId;
        m_parentIfIndex = parentIfIndex;
        m_status = status;
        m_lastPollTime = lastPollTime;
        m_linkTypeId = -1;
    }

    /**
     * Method id returns the id of this DataLinkInterface object.
     *
     * @return the Id (type Integer) of this DataLinkInterface object.
     */
    @XmlTransient
    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    @XmlID
    @XmlAttribute(name="id")
    @Transient
    public String getDataLinkInterfaceId() {
        return getId().toString();
    }
    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id a int.
     */
    public void setId(final int id) {
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

    /**
     * Method getIfIndex returns the ifIndex of this DataLinkInterface object.
     *
     * @return the ifIndex (type Integer) of this DataLinkInterface object.
     */
    
    @XmlElement( name = "ifIndex" )
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>Setter for the field <code>ifIndex</code>.</p>
     *
     * @param ifIndex a int.
     */
    public void setIfIndex(final int ifIndex) {
        m_ifIndex = ifIndex;
    }

    /**
     * Method getNodeParentId returns the nodeParentId of this DataLinkInterface object.
     *
     * @return the nodeParentId (type Integer) of this DataLinkInterface object.
     */
    @XmlElement(name = "nodeParentId")
    public Integer getNodeParentId() {
        return m_nodeParentId;
    }

    /**
     * <p>Setter for the field <code>nodeParentId</code>.</p>
     *
     * @param nodeParentId a int.
     */
    public void setNodeParentId(final int nodeParentId) {
        m_nodeParentId = nodeParentId;
    }

    /**
     * Method getParentIfIndex returns the parentIfIndex of this DataLinkInterface object.
     *
     * @return the parentIfIndex (type Integer) of this DataLinkInterface object.
     */
    @XmlElement(name = "parentIfIndex")
    public Integer getParentIfIndex() {
        return m_parentIfIndex;
    }

    /**
     * <p>Setter for the field <code>parentIfIndex</code>.</p>
     *
     * @param parentIfIndex a int.
     */
    public void setParentIfIndex(final int parentIfIndex) {
        m_parentIfIndex = parentIfIndex;
    }

    /**
     * Method getStatus returns the status of this DataLinkInterface object.
     *
     * @return the status (type String) of this DataLinkInterface object.
     */
    public String getStatus() {
        return m_status;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link java.lang.String} object.
     */
    public void setStatus(final String status) {
        m_status = status;
    }

    /**
     * <p>Getter for the field <code>linkTypeId</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLinkTypeId() {
        return m_linkTypeId;
    }

    /**
     * <p>Setter for the field <code>linkTypeId</code>.</p>
     *
     * @param linkTypeId a {@link java.lang.Integer} object.
     */
    public void setLinkTypeId(final Integer linkTypeId) {
        m_linkTypeId = linkTypeId;
    }

    /**
     * Method getLastPollTime returns the lastPollTime of this DataLinkInterface object.
     *
     * @return the lastPollTime (type Date) of this DataLinkInterface object.
     */
    public Date getLastPollTime() {
        return m_lastPollTime;
    }

    /**
     * <p>Setter for the field <code>lastPollTime</code>.</p>
     *
     * @param lastPollTime a {@link java.util.Date} object.
     */
    public void setLastPollTime(final Date lastPollTime) {
        m_lastPollTime = lastPollTime;
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.DataLinkInterface} object.
     * @return a int.
     */
    public int compareTo(final DataLinkInterface o) {
        return new CompareToBuilder()
            .append(getId(), o.getId())
            .append(getNode(), o.getNode())
            .append(getIfIndex(), o.getIfIndex())
            .append(getNodeParentId(), o.getNodeParentId())
            .append(getParentIfIndex(), o.getParentIfIndex())
            .append(getStatus(), o.getStatus())
            .append(getLastPollTime(), o.getLastPollTime())
            .append(getLinkTypeId(), o.getLinkTypeId())
            .toComparison();
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return new HashCodeBuilder()
            .append(m_id)
            .append(m_node)
            .append(m_ifIndex)
            .append(m_nodeParentId)
            .append(m_parentIfIndex)
            .append(m_status)
            .append(m_lastPollTime)
            .append(m_linkTypeId)
            .toHashCode();
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", m_id)
            .append("node", m_node)
            .append("ifIndex", m_ifIndex)
            .append("nodeParentId", m_nodeParentId)
            .append("parentIfIndex", m_parentIfIndex)
            .append("status", m_status)
            .append("linkTypeId", m_linkTypeId)
            .append("lastPollTime", m_lastPollTime)
            .toString();
    }
}
