/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

    protected String location;

    protected String eventLocation;

    protected String perspectiveLocation;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getPerspectiveLocation() {
        return perspectiveLocation;
    }

    public void setPerspectiveLocation(String perspectiveLocation) {
        this.perspectiveLocation = perspectiveLocation;
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
            .append("Location", getLocation())
            .append("Service Name", getServiceName())
            .append("Lost Service Time", getLostServiceTime())
            .append("Regained Service Time", getRegainedServiceTime())
            .append("Event Location", getEventLocation())
            .append("Acknowledged By", getLostServiceNotificationAcknowledgedBy())
            .append("Suppress Time", getSuppressTime())
            .append("Suppressed By", getSuppressedBy())
            .append("Building", getBuilding())
            .append("Perspective Location", getPerspectiveLocation())
            .toString();
    }
}
