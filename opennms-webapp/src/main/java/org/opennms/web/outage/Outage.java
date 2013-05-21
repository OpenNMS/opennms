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

package org.opennms.web.outage;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A JavaBean for holding information about a single outage.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @version $Id: $
 * @since 1.8.1
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
    
    protected Date suppressTime;
        
    protected String suppressedBy;

    protected String building;

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
        this(outageId, nodeId, nodeLabel, ipAddress, hostname, serviceId, serviceName, lostServiceTime, regainedServiceTime, lostServiceEventId, regainedServiceEventId, lostServiceNotificationId, lostServiceNotificationAcknowledgedBy, suppressTime, suppressedBy, null);
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
     * @param building a {@link java.lang.String} object.
     */
    protected Outage(int outageId, int nodeId, String nodeLabel, String ipAddress, String hostname, int serviceId, String serviceName, Date lostServiceTime, Date regainedServiceTime, Integer lostServiceEventId, Integer regainedServiceEventId, Integer lostServiceNotificationId, String lostServiceNotificationAcknowledgedBy, Date suppressTime, String suppressedBy, String building) {
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
        this.building = building;
    }

    /**
     * <p>getId</p>
     *
     * @return a int.
     */
    public int getId() {
        return outageId;
    }

    /**
     * <p>Getter for the field <code>nodeId</code>.</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return (nodeId);
    }

    /**
     * <p>Getter for the field <code>ipAddress</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return (ipAddress);
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostname() {
        return (hostname);
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return (nodeLabel);
    }

    /**
     * <p>Getter for the field <code>serviceId</code>.</p>
     *
     * @return a int.
     */
    public int getServiceId() {
        return (serviceId);
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return (serviceName);
    }

    /**
     * <p>Getter for the field <code>lostServiceTime</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getLostServiceTime() {
        return (lostServiceTime);
    }

    /**
     * can be null
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getRegainedServiceTime() {
        return regainedServiceTime;
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLostServiceEventId() {
        return lostServiceEventId;
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getRegainedServiceEventId() {
        return regainedServiceEventId;
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLostServiceNotificationId() {
        return lostServiceNotificationId;
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLostServiceNotificationAcknowledgedBy() {
        return lostServiceNotificationAcknowledgedBy;
    }

    /**
     * <p>Getter for the field <code>suppressTime</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getSuppressTime() {
        return suppressTime;
    }
    
    /**
     * <p>Getter for the field <code>suppressedBy</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSuppressedBy() {
        return suppressedBy;
    }

    /**
     * can be null
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBuilding() {
        return building;
    }

    /**
     * <p>getTimeDown</p>
     *
     * @deprecated Please use {@link #getLostServiceTime getLostServiceTime} instead.
     * @return a {@link java.util.Date} object.
     */
    public Date getTimeDown() {
        return (this.getLostServiceTime());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
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
            .append("Building", getBuilding())
            .toString();
    }
}
