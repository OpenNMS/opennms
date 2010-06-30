//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.outage;

import java.util.Date;

/**
 * A JavaBean for holding information about a single outage.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class Outage {
    protected int outageId;

    protected int nodeId;

    protected String ipAddress;

    protected String hostname;

    protected String nodeLabel;

    protected int serviceId;

    protected String serviceName;

    protected Date lostServiceTime;

    protected Date regainedServiceTime;

    protected Integer lostServiceEventId;

    protected Integer regainedServiceEventId;

    protected Integer lostServiceNotificationId;

    protected String lostServiceNotificationAcknowledgedBy;
    
    //JOED
    
    protected Date suppressTime;
        
    protected String suppressedBy;

    /**
     * <p>Constructor for Outage.</p>
     */
    protected Outage() {
    }

    /**
     * <p>Constructor for Outage.</p>
     *
     * @param outageId a int.
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param hostname a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param serviceName a {@link java.lang.String} object.
     * @param lostServiceTime a {@link java.util.Date} object.
     * @param regainedServiceTime a {@link java.util.Date} object.
     * @param lostServiceEventId a {@link java.lang.Integer} object.
     * @param regainedServiceEventId a {@link java.lang.Integer} object.
     * @param lostServiceNotificationId a {@link java.lang.Integer} object.
     * @param lostServiceNotificationAcknowledgedBy a {@link java.lang.String} object.
     * @param suppressTime a {@link java.util.Date} object.
     * @param suppressedBy a {@link java.lang.String} object.
     */
    protected Outage(int outageId, int nodeId, String nodeLabel, String ipAddress, String hostname, int serviceId, String serviceName, Date lostServiceTime, Date regainedServiceTime, Integer lostServiceEventId, Integer regainedServiceEventId, Integer lostServiceNotificationId, String lostServiceNotificationAcknowledgedBy, Date suppressTime, String suppressedBy) {
        this.outageId = outageId;
        this.nodeId = nodeId;
        this.nodeLabel = nodeLabel;
        this.ipAddress = ipAddress;
        this.hostname = hostname;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.lostServiceTime = lostServiceTime;
        this.regainedServiceTime = regainedServiceTime;
        this.lostServiceEventId = lostServiceEventId;
        this.regainedServiceEventId = regainedServiceEventId;
        this.lostServiceNotificationId = lostServiceNotificationId;
        this.lostServiceNotificationAcknowledgedBy = lostServiceNotificationAcknowledgedBy;
        this.suppressTime = suppressTime;
        this.suppressedBy = suppressedBy;  
    }

    /**
     * <p>getId</p>
     *
     * @return a int.
     */
    public int getId() {
        return this.outageId;
    }

    /**
     * <p>Getter for the field <code>nodeId</code>.</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return (this.nodeId);
    }

    /**
     * <p>Getter for the field <code>ipAddress</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return (this.ipAddress);
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostname() {
        return (this.hostname);
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return (this.nodeLabel);
    }

    /**
     * <p>Getter for the field <code>serviceId</code>.</p>
     *
     * @return a int.
     */
    public int getServiceId() {
        return (this.serviceId);
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return (this.serviceName);
    }

    /**
     * <p>Getter for the field <code>lostServiceTime</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getLostServiceTime() {
        return (this.lostServiceTime);
    }

    /**
     * can be null
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getRegainedServiceTime() {
        return this.regainedServiceTime;
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLostServiceEventId() {
        return this.lostServiceEventId;
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getRegainedServiceEventId() {
        return this.regainedServiceEventId;
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLostServiceNotificationId() {
        return this.lostServiceNotificationId;
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLostServiceNotificationAcknowledgedBy() {
        return this.lostServiceNotificationAcknowledgedBy;
    }

    /**
     * <p>Getter for the field <code>suppressTime</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getSuppressTime() {
        return this.suppressTime;
    }
    
    /**
     * <p>Getter for the field <code>suppressedBy</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSuppressedBy() {
        return this.suppressedBy;
    }
    
    /**
     * <p>getTimeDown</p>
     *
     * @deprecated Please use
     *             {@link #getLostServiceTime getLostServiceTimeInstead}
     * @return a {@link java.util.Date} object.
     */
    public Date getTimeDown() {
        return (this.getLostServiceTime());
    }

}
