/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.core.style.ToStringCreator;


/**
 * Represents an OpenNMS Distributed Poller
 */
@Entity
@Table(name="distPoller")
public class OnmsDistPoller implements Serializable {

    private static final long serialVersionUID = -1094353783612066524L;

    /** identifier field */
    private String m_name;

    /** persistent field */
    private String m_ipAddress;

    /** nullable persistent field */
    private String m_comment;

    /** nullable persistent field */
    private BigDecimal m_discoveryLimit;

    /** nullable persistent field */
    private Date m_lastNodePull;

    /** nullable persistent field */
    private Date m_lastEventPull;

    /** nullable persistent field */
    private Date m_lastPackagePush;

    /** nullable persistent field */
    private Integer m_adminState;

    /** nullable persistent field */
    private Integer m_runState;

    /**
     * default constructor
     */
    public OnmsDistPoller() {}
    
    /**
     * minimal constructor
     *
     * @param name a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     */
    public OnmsDistPoller(String name, String ipAddress) {
        m_name = name;
        m_ipAddress = ipAddress;
    }

    /**
     * A human-readable name for each system.
     * Typically, the system's hostname (not fully qualified).
     *
     * @return a {@link java.lang.String} object.
     */
    @Id 
    @Column(name="dpName", nullable=false)
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param dpname a {@link java.lang.String} object.
     */
    public void setName(String dpname) {
        m_name = dpname;
    }

    /**
     * IP address of the distributed poller.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="dpIP", nullable=false, length=16)
    public String getIpAddress() {
        return m_ipAddress;
    }

    /**
     * <p>setIpAddress</p>
     *
     * @param dpip a {@link java.lang.String} object.
     */
    public void setIpAddress(String dpip) {
        m_ipAddress = dpip;
    }

    /**
     * A free form text field providing a desciption of the distrubted poller
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="dpComment", length=256)
    public String getComment() {
        return m_comment;
    }

    /**
     * <p>setComment</p>
     *
     * @param dpcomment a {@link java.lang.String} object.
     */
    public void setComment(String dpcomment) {
        m_comment = dpcomment;
    }

    /**
     * Numeric representation of percentage of interface speed available to discovery
     * process.  See documentation for "bandwidth troll"
     *
     * @return a {@link java.math.BigDecimal} object.
     */
    @Column(name="dpDiscLimit", length=5, scale=2)
    public BigDecimal getDiscoveryLimit() {
        return m_discoveryLimit;
    }

    /**
     * <p>setDiscoveryLimit</p>
     *
     * @param dpdisclimit a {@link java.math.BigDecimal} object.
     */
    public void setDiscoveryLimit(BigDecimal dpdisclimit) {
        m_discoveryLimit = dpdisclimit;
    }

    /**
     * Time of last pull of new nodes from the DP
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP) @Column(name="dpLastNodePull")
    public Date getLastNodePull() {
        return m_lastNodePull;
    }

    /**
     * <p>setLastNodePull</p>
     *
     * @param dplastnodepull a {@link java.util.Date} object.
     */
    public void setLastNodePull(Date dplastnodepull) {
        m_lastNodePull = dplastnodepull;
    }

    /**
     * Time of last pull of events from the DP
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP) @Column(name="dpLastEventPull")
    public Date getLastEventPull() {
        return m_lastEventPull;
    }

    /**
     * <p>setLastEventPull</p>
     *
     * @param dplasteventpull a {@link java.util.Date} object.
     */
    public void setLastEventPull(Date dplasteventpull) {
        m_lastEventPull = dplasteventpull;
    }

    /**
     * Time of last push of Package (config) to the DP
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP) @Column(name="dpLastPackagePush")
    public Date getLastPackagePush() {
        return m_lastPackagePush;
    }

    /**
     * <p>setLastPackagePush</p>
     *
     * @param dplastpackagepush a {@link java.util.Date} object.
     */
    public void setLastPackagePush(Date dplastpackagepush) {
        m_lastPackagePush = dplastpackagepush;
    }

    /**
     * Reflects desired state for this distributed poller. 1 = Up, 0 = Down
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name="dpAdminState")
    public Integer getAdminState() {
        return m_adminState;
    }

    /**
     * <p>setAdminState</p>
     *
     * @param dpadminstate a {@link java.lang.Integer} object.
     */
    public void setAdminState(Integer dpadminstate) {
        m_adminState = dpadminstate;
    }

    /**
     * Reflects the current perceived state of the distributed
     * poller.  1 = Up, 0 = Down
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name="dpRunState")
    public Integer getRunState() {
        return m_runState;
    }

    /**
     * <p>setRunState</p>
     *
     * @param dprunstate a {@link java.lang.Integer} object.
     */
    public void setRunState(Integer dprunstate) {
        m_runState = dprunstate;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("name", getName())
            .toString();
    }

}
