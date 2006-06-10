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
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.springframework.core.style.ToStringCreator;


/** 
 * Represents an OpenNMS Distributed Poller
 * 
 * @hibernate.class table="distpoller"
 *     
*/
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

    /** default constructor */
    public OnmsDistPoller() {}
    
    /** minimal constructor */
    public OnmsDistPoller(String name, String ipAddress) {
        m_name = name;
        m_ipAddress = ipAddress;
    }

    /** 
     * A human-readable name for each system.
     * Typically, the system's hostname (not fully qualified).
     * 
     * @hibernate.id generator-class="assigned" column="dpname" length="12"
     *         
     */
    public String getName() {
        return m_name;
    }

    public void setName(String dpname) {
        m_name = dpname;
    }

    /**
     * IP address of the distributed poller.
     *  
     * @hibernate.property column="dpip" length="16" not-null="true"
     *         
     */
    public String getIpAddress() {
        return m_ipAddress;
    }

    public void setIpAddress(String dpip) {
        m_ipAddress = dpip;
    }

    /** 
     * A free form text field providing a desciption of the distrubted poller
     * 
     * @hibernate.property column="dpcomment" length="256"
     *         
     */
    public String getComment() {
        return m_comment;
    }

    public void setComment(String dpcomment) {
        m_comment = dpcomment;
    }

    /** 
     * Numeric representation of percentage of interface speed available to discovery
     * process.  See documentation for "bandwidth troll"
     * 
     * @hibernate.property column="dpdisclimit" length="5"
     *         
     */
    public BigDecimal getDiscoveryLimit() {
        return m_discoveryLimit;
    }

    public void setDiscoveryLimit(BigDecimal dpdisclimit) {
        m_discoveryLimit = dpdisclimit;
    }

    /**
     * Time of last pull of new nodes from the DP
     * 
     * @hibernate.property column="dplastnodepull" length="8"
     *         
     */
    public Date getLastNodePull() {
        return m_lastNodePull;
    }

    public void setLastNodePull(Date dplastnodepull) {
        m_lastNodePull = dplastnodepull;
    }

    /**
     * Time of last pull of events from the DP
     * 
     * @hibernate.property column="dplasteventpull" length="8"
     *         
     */
    public Date getLastEventPull() {
        return m_lastEventPull;
    }

    public void setLastEventPull(Date dplasteventpull) {
        m_lastEventPull = dplasteventpull;
    }

    /** 
     * Time of last push of Package (config) to the DP
     *
     * @hibernate.property column="dplastpackagepush" length="8"
     *         
     */
    public Date getLastPackagePush() {
        return m_lastPackagePush;
    }

    public void setLastPackagePush(Date dplastpackagepush) {
        m_lastPackagePush = dplastpackagepush;
    }

    /** 
     * Reflects desired state for this distributed poller. 1 = Up, 0 = Down
     * 
     * @hibernate.property column="dpadminstate" length="4"
     *         
     */
    public Integer getAdminState() {
        return m_adminState;
    }

    public void setAdminState(Integer dpadminstate) {
        m_adminState = dpadminstate;
    }

    /**
     * Reflects the current perceived state of the distributed 
     * poller.  1 = Up, 0 = Down
     * 
     * @hibernate.property column="dprunstate" length="4"
     *         
     */
    public Integer getRunState() {
        return m_runState;
    }

    public void setRunState(Integer dprunstate) {
        m_runState = dprunstate;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("name", getName())
            .toString();
    }

}
