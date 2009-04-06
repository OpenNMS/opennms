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

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A JavaBean for holding information about a single outage.
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
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

    protected Outage() {
    }

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

    public int getId() {
        return outageId;
    }

    public int getNodeId() {
        return (nodeId);
    }

    public String getIpAddress() {
        return (ipAddress);
    }

    /** can be null */
    public String getHostname() {
        return (hostname);
    }

    /** can be null */
    public String getNodeLabel() {
        return (nodeLabel);
    }

    public int getServiceId() {
        return (serviceId);
    }

    /** can be null */
    public String getServiceName() {
        return (serviceName);
    }

    public Date getLostServiceTime() {
        return (lostServiceTime);
    }

    /** can be null */
    public Date getRegainedServiceTime() {
        return regainedServiceTime;
    }

    /** can be null */
    public Integer getLostServiceEventId() {
        return lostServiceEventId;
    }

    /** can be null */
    public Integer getRegainedServiceEventId() {
        return regainedServiceEventId;
    }

    /** can be null */
    public Integer getLostServiceNotificationId() {
        return lostServiceNotificationId;
    }

    /** can be null */
    public String getLostServiceNotificationAcknowledgedBy() {
        return lostServiceNotificationAcknowledgedBy;
    }

    public Date getSuppressTime() {
        return suppressTime;
    }
    
    public String getSuppressedBy() {
        return suppressedBy;
    }
    
    /**
     * @deprecated Please use
     *             {@link #getLostServiceTime getLostServiceTimeInstead}
     */
    public Date getTimeDown() {
        return (this.getLostServiceTime());
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("ID", getId())
            .append("node ID", getNodeId())
            .append("IP Address", getIpAddress())
            .append("Hostname", getHostname())
            .append("Node Label", getNodeLabel())
            .append("Service Name", getServiceName())
            .append("Lost Service Time", getLostServiceTime())
            .append("Regained Service Time", getRegainedServiceTime())
            .append("Acknowledged By", getLostServiceNotificationAcknowledgedBy())
            .append("Suppress Time", getSuppressTime())
            .append("Suppressed By", getSuppressedBy())
            .toString();
    }
}
