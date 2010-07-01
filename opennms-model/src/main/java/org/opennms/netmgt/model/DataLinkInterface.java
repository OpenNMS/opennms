//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
/**
 * <p>DataLinkInterface class.</p>
 *
 * @author: joed
 * Date  : Jul 31, 2008
 * @author ranger
 * @version $Id: $
 */

package org.opennms.netmgt.model;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.GeneratedValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "datalinkinterface")
public class DataLinkInterface  implements Serializable, Comparable<DataLinkInterface> {
    private static final long serialVersionUID = 5241963830563150843L;

    private Integer id;

    @Column(name="nodeid", nullable=false)
    private Integer nodeId;
    @Column(name="ifindex", nullable=false)
    private Integer ifIndex;
    @Column(name="nodeparentid", nullable=false)
    private Integer nodeParentId;
    @Column(name="parentifindex", nullable=false)
    private Integer parentIfIndex;
    @Column(name="status", length=1, nullable=false)
    private String  status;
    @Column(name="linktypeid", nullable=true)
    private Integer linkTypeId;
    @Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastpolltime", nullable=false)
    private Date lastPollTime;

    /**
     * <p>Constructor for DataLinkInterface.</p>
     */
    public DataLinkInterface() {
        // not sure what to do here, but Hibernate wants it.
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
    public DataLinkInterface(int nodeId, int ifIndex, int nodeParentId, int parentIfIndex, String status, Date lastPollTime) {
        this.nodeId = nodeId;
        this.ifIndex = ifIndex;
        this.nodeParentId = nodeParentId;
        this.parentIfIndex = parentIfIndex;
        this.status = status;
        this.lastPollTime = lastPollTime;
        this.linkTypeId = -1;
    }

    /**
     * Method getNodeId returns the nodeId of this DataLinkInterface object.
     *
     * @return the nodeId (type Integer) of this DataLinkInterface object.
     */
    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")
    public Integer getId() {
        return id;
    }

    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id a int.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * <p>Getter for the field <code>nodeId</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getNodeId() {
        return nodeId;
    }

    /**
     * <p>Setter for the field <code>nodeId</code>.</p>
     *
     * @param nodeId a int.
     */
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Method getIfIndex returns the ifIndex of this DataLinkInterface object.
     *
     * @return the ifIndex (type Integer) of this DataLinkInterface object.
     */
    public Integer getIfIndex() {
        return ifIndex;
    }

    /**
     * <p>Setter for the field <code>ifIndex</code>.</p>
     *
     * @param ifIndex a int.
     */
    public void setIfIndex(int ifIndex) {
        this.ifIndex = ifIndex;
    }

    /**
     * Method getNodeParentId returns the nodeParentId of this DataLinkInterface object.
     *
     * @return the nodeParentId (type Integer) of this DataLinkInterface object.
     */
    public Integer getNodeParentId() {
        return nodeParentId;
    }

    /**
     * <p>Setter for the field <code>nodeParentId</code>.</p>
     *
     * @param nodeParentId a int.
     */
    public void setNodeParentId(int nodeParentId) {
        this.nodeParentId = nodeParentId;
    }

    /**
     * Method getParentIfIndex returns the parentIfIndex of this DataLinkInterface object.
     *
     * @return the parentIfIndex (type Integer) of this DataLinkInterface object.
     */
    public Integer getParentIfIndex() {
        return parentIfIndex;
    }

    /**
     * <p>Setter for the field <code>parentIfIndex</code>.</p>
     *
     * @param parentIfIndex a int.
     */
    public void setParentIfIndex(int parentIfIndex) {
        this.parentIfIndex = parentIfIndex;
    }

    /**
     * Method getStatus returns the status of this DataLinkInterface object.
     *
     * @return the status (type String) of this DataLinkInterface object.
     */
    public String getStatus() {
        return status;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link java.lang.String} object.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * <p>Getter for the field <code>linkTypeId</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLinkTypeId() {
        return linkTypeId;
    }

    /**
     * <p>Setter for the field <code>linkTypeId</code>.</p>
     *
     * @param linkTypeId a {@link java.lang.Integer} object.
     */
    public void setLinkTypeId(Integer linkTypeId) {
        this.linkTypeId = linkTypeId;
    }

    /**
     * Method getLastPollTime returns the lastPollTime of this DataLinkInterface object.
     *
     * @return the lastPollTime (type Date) of this DataLinkInterface object.
     */
    public Date getLastPollTime() {
        return lastPollTime;
    }

    /**
     * <p>Setter for the field <code>lastPollTime</code>.</p>
     *
     * @param lastPollTime a {@link java.util.Date} object.
     */
    public void setLastPollTime(Date lastPollTime) {
        this.lastPollTime = lastPollTime;
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.DataLinkInterface} object.
     * @return a int.
     */
    public int compareTo(DataLinkInterface o) {
        return new CompareToBuilder()
            .append(getId(), o.getId())
            .append(getNodeId(), o.getNodeId())
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
            .append(getId())
            .append(getNodeId())
            .append(getIfIndex())
            .append(getNodeParentId())
            .append(getParentIfIndex())
            .append(getStatus())
            .append(getLastPollTime())
            .append(getLinkTypeId())
            .toHashCode();
    }
}
