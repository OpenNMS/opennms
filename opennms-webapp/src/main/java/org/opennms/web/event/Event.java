/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.event;

import java.util.Date;
import java.util.Map;

import org.opennms.netmgt.model.OnmsSeverity;

/**
 * A JavaBean implementation to hold information about a network event as
 * defined by OpenNMS.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class Event {
    /** Unique identifier for the event, cannot be null */
    protected int id;

    /** Universal Event Identifer (UEI) for this event, cannot be null */
    protected String uei;

    /**
     * Contains the eid, eidtext (optionally), specific, and generic identifier
     * for the SNMP Trap. This maps directly to the &lt;snmp&gt;element in the
     * Event Data Stream DTD.
     */
    protected String snmp;

    /**
     * The &lt;time&gt; element from the Event Data Stream DTD, which is the
     * time the event was received by the source process. Cannot be null.
     */
    protected Date time;

    /** The &lt;host&gt; element from the Event Data Stream DTD */
    protected String host;

    /** The &lt;snmphost&gt; element from the Event Data Stream DTD */
    protected String snmphost;

    /** The dpName of the Dist Poller which received the event, cannot be null. */
    protected String dpName;

    /** The &lt;parms&gt; element from the Event Data Stream DTD */
    protected Map<String, String> parms;

    /** Unique integer identifier for node, can be null */
    protected Integer nodeID;

    /** Unique integer identifier of service/poller package, can be null */
    protected Integer serviceID;

    /** Human-readable name of the service */
    protected String serviceName;

    /** IP Address of node's interface */
    protected String ipAddr;

    /** Creation time of event in database, cannot be null */
    protected Date createTime;

    /** Free-form textual description of the event */
    protected String description;

    /**
     * Formatted display text to control how the event will appear in the
     * browser. This field may contain variables that are populated by field
     * values of the message.
     */
    protected String logMessage;

    /**
     * Logical group with which to associate event. This field provides a means
     * of logically grouping related events.
     */
    protected String logGroup;

    /**
     * Severity the of event.
     * 
     * <pre>
     * 
     *   1  = Indeterminate
     *   2 = Cleared (unimplemented at this time)
     *   3 = Warning
     *   4 = Minor
     *   5 = Major
     *   6 = Critical
     *  
     * </pre>
     */
    protected OnmsSeverity severity;

    /** Operator instruction for event. */
    protected String operatorInstruction;

    /**
     * Automated Action for event. Should consist of fully-qualfied pathname to
     * executable command, with possible variables used to reference
     * event-specific data
     */
    protected String autoAction;

    /**
     * Operator Action for event. Should consist of fully-qualfied pathname to
     * executable command, with possible variables used to reference
     * event-specific data
     */
    protected String operatorAction;

    /**
     * Text of the eventOperAction. Menu text displayed to Operator, which if
     * selected, will invoke action described in eventOperAction.
     */
    protected String operatorActionMenuText;

    /**
     * Notification string. Should consist of a fully-qualfied pathname to an
     * executable which invokes the notification software, and will likely
     * contain event-specific variables
     */
    protected String notification;

    /**
     * Trouble ticket integration string. Should consist of fully-qualfied
     * pathname to executable command, with possible variables used to reference
     * event-specific data
     */
    protected String troubleTicket;

    /**
     * State of the trouble ticket. Trouble ticket on/off boolean 1=on, 0=off.
     * Can be null.
     */
    protected Integer troubleTicketState;

    /**
     * The forwarding information. Contains a list of triplets:
     * <code>Destination,State,Mechanism;Destination,State,Mechanism;</code>
     * which reflect the following:
     * <ul>
     * <li>State is a boolean flag as to whether the entry is active or not.
     * 1=on, 0=off.
     * <li>Destination is hostname or IP of system to forward the event to
     * <li>Method is the means by which it will be forwarded. A keyword, e.g.,
     * SNMP
     * </ul>
     */
    protected String forward;

    /**
     * Mouse over text. Text to be displayed on MouseOver event, if the event is
     * displayed in the browser and the operator needs additional info.
     */
    protected String mouseOverText;

    /** The name of the user who acknowledged this event. */
    protected String acknowledgeUser;

    /** The time this event was acknowledged. */
    protected Date acknowledgeTime;

    /** The human-readable name of the node of this event. Can be null. */
    protected String nodeLabel;

    /**
     * The alarmId if reduced.
     * Can be null.
     */
    protected Integer alarmId;
    
    /**
     * Whether the event is displayable.
     */
    protected Boolean eventDisplay;

    protected String systemId;

    protected String location;

    protected String nodeLocation;

    /**
     * Empty constructor to create an empty <code>Event</code> instance. All
     * fields will hold the default values.
     */
    public Event() {
    }

    /**
     * Create an event that represents a real network event with only the
     * required parameters.
     *
     * @param id a int.
     * @param uei a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @param dpName a {@link java.lang.String} object.
     * @param createTime a {@link java.util.Date} object.
     * @param severityId a int.
     */
    public Event(int id, String uei, Date time, String dpName, Date createTime, int severityId) {
        if (uei == null || time == null || dpName == null || createTime == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.id = id;
        this.uei = uei;
        this.time = time;
        this.dpName = dpName;
        this.createTime = createTime;
        severity = OnmsSeverity.get(severityId);
    }

    /**
     * Create an event that represents a real network event with all the
     * parameters.
     *
     * @param id a int.
     * @param uei a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @param dpName a {@link java.lang.String} object.
     * @param createTime a {@link java.util.Date} object.
     * @param severityId a int.
     * @param snmp a {@link java.lang.String} object.
     * @param host a {@link java.lang.String} object.
     * @param snmphost a {@link java.lang.String} object.
     * @param parms a {@link java.lang.String} object.
     * @param nodeID a {@link java.lang.Integer} object.
     * @param serviceID a {@link java.lang.Integer} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param description a {@link java.lang.String} object.
     * @param logMessage a {@link java.lang.String} object.
     * @param logGroup a {@link java.lang.String} object.
     * @param operatorInstruction a {@link java.lang.String} object.
     * @param autoAction a {@link java.lang.String} object.
     * @param operatorAction a {@link java.lang.String} object.
     * @param operatorActionMenuText a {@link java.lang.String} object.
     * @param notification a {@link java.lang.String} object.
     * @param troubleTicket a {@link java.lang.String} object.
     * @param troubleTicketState a {@link java.lang.Integer} object.
     * @param forward a {@link java.lang.String} object.
     * @param mouseOverText a {@link java.lang.String} object.
     * @param acknowledgeUser a {@link java.lang.String} object.
     * @param acknowledgeTime a {@link java.util.Date} object.
     */
    public Event(int id, String uei, Date time, String dpName, Date createTime, int severityId, String snmp, String host, String snmphost, Map<String, String> parms, Integer nodeID, Integer serviceID, String ipAddr, String description, String logMessage, String logGroup, String operatorInstruction, String autoAction, String operatorAction, String operatorActionMenuText, String notification, String troubleTicket, Integer troubleTicketState, String forward, String mouseOverText, String acknowledgeUser, Date acknowledgeTime) {
        this(id, uei, time, dpName, createTime, severityId, snmp, host, snmphost, parms, nodeID, serviceID, ipAddr, description, logMessage, logGroup, operatorInstruction, autoAction, operatorAction, operatorActionMenuText, notification, troubleTicket, troubleTicketState, forward, mouseOverText, acknowledgeUser, acknowledgeTime, null, null, null, null);
    }

    /**
     * <p>Constructor for Event.</p>
     *
     * @param id a int.
     * @param uei a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @param dpName a {@link java.lang.String} object.
     * @param createTime a {@link java.util.Date} object.
     * @param severityId a int.
     * @param snmp a {@link java.lang.String} object.
     * @param host a {@link java.lang.String} object.
     * @param snmphost a {@link java.lang.String} object.
     * @param parms a {@link java.lang.String} object.
     * @param nodeID a {@link java.lang.Integer} object.
     * @param serviceID a {@link java.lang.Integer} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param description a {@link java.lang.String} object.
     * @param logMessage a {@link java.lang.String} object.
     * @param logGroup a {@link java.lang.String} object.
     * @param operatorInstruction a {@link java.lang.String} object.
     * @param autoAction a {@link java.lang.String} object.
     * @param operatorAction a {@link java.lang.String} object.
     * @param operatorActionMenuText a {@link java.lang.String} object.
     * @param notification a {@link java.lang.String} object.
     * @param troubleTicket a {@link java.lang.String} object.
     * @param troubleTicketState a {@link java.lang.Integer} object.
     * @param forward a {@link java.lang.String} object.
     * @param mouseOverText a {@link java.lang.String} object.
     * @param acknowledgeUser a {@link java.lang.String} object.
     * @param acknowledgeTime a {@link java.util.Date} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param alarmId a {@link java.lang.Integer} object.
     */
    public Event(int id, String uei, Date time, String dpName, Date createTime, int severityId, String snmp, String host, String snmphost, Map<String, String> parms, Integer nodeID, Integer serviceID, String ipAddr, String description, String logMessage, String logGroup, String operatorInstruction, String autoAction, String operatorAction, String operatorActionMenuText, String notification, String troubleTicket, Integer troubleTicketState, String forward, String mouseOverText, String acknowledgeUser, Date acknowledgeTime, String nodeLabel, String serviceName, Integer alarmId) {
        this(id, uei, time, dpName, createTime, severityId, snmp, host, snmphost, parms, nodeID, serviceID, ipAddr, description, logMessage, logGroup, operatorInstruction, autoAction, operatorAction, operatorActionMenuText, notification, troubleTicket, troubleTicketState, forward, mouseOverText, acknowledgeUser, acknowledgeTime, nodeLabel, serviceName, alarmId, null);
    }

    /**
     * Create an event that represents a real network event with all the
     * parameters.
     */
    public Event(int id, String uei, Date time, String dpName, Date createTime, int severityId, String snmp, String host, String snmphost, Map<String, String> parms, Integer nodeID, Integer serviceID, String ipAddr, String description, String logMessage, String logGroup, String operatorInstruction, String autoAction, String operatorAction, String operatorActionMenuText, String notification, String troubleTicket, Integer troubleTicketState, String forward, String mouseOverText, String acknowledgeUser, Date acknowledgeTime, String nodeLabel, String serviceName, Integer alarmId, Boolean eventDisplay) {

        if (uei == null || time == null || dpName == null || createTime == null) {
            throw new IllegalArgumentException("Cannot take null values for the following parameters: uei, time, dpName, createTime.");
        }

        // required fields
        this.id = id;
        this.uei = uei;
        this.time = time;
        this.dpName = dpName;
        this.createTime = createTime;
        severity = OnmsSeverity.get(severityId);

        // optional fields
        this.snmp = snmp;
        this.host = host;
        this.snmphost = snmphost;
        this.parms = parms;
        this.nodeID = nodeID;
        this.serviceID = serviceID;
        this.ipAddr = ipAddr;
        this.description = description;
        this.logMessage = logMessage;
        this.logGroup = logGroup;
        this.operatorInstruction = operatorInstruction;
        this.autoAction = autoAction;
        this.operatorAction = operatorAction;
        this.operatorActionMenuText = operatorActionMenuText;
        this.notification = notification;
        this.troubleTicket = troubleTicket;
        this.troubleTicketState = troubleTicketState;
        this.forward = forward;
        this.mouseOverText = mouseOverText;
        this.acknowledgeUser = acknowledgeUser;
        this.acknowledgeTime = acknowledgeTime;
        this.nodeLabel = nodeLabel;
        this.serviceName = serviceName;
        this.alarmId = alarmId;
        this.eventDisplay = eventDisplay;
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a int.
     */
    public int getId() {
        return (id);
    }

    /**
     * <p>Getter for the field <code>uei</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUei() {
        return (uei);
    }

    /**
     * <p>Getter for the field <code>time</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getTime() {
        return (time);
    }

    /**
     * <p>Getter for the field <code>dpName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDpName() {
        return (dpName);
    }

    /**
     * <p>Getter for the field <code>createTime</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getCreateTime() {
        return (createTime);
    }

    /**
     * <p>Getter for the field <code>severity</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    public OnmsSeverity getSeverity() {
        return (severity);
    }

    /**
     * <p>Getter for the field <code>snmp</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSnmp() {
        return (snmp);
    }

    /**
     * <p>Getter for the field <code>host</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHost() {
        return (host);
    }

    /**
     * <p>getSnmpHost</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpHost() {
        return (snmphost);
    }

    /**
     * <p>Getter for the field <code>parms</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public Map<String, String> getParms() {
        return (parms);
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return (nodeID.intValue());
    }

    /**
     * <p>Getter for the field <code>nodeLabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return (nodeLabel);
    }

    /**
     * <p>getServiceId</p>
     *
     * @return a int.
     */
    public int getServiceId() {
        return (serviceID.intValue());
    }

    /**
     * <p>Getter for the field <code>serviceName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return (serviceName);
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return (ipAddr);
    }

    /**
     * <p>Getter for the field <code>description</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return (description);
    }

    /**
     * <p>Getter for the field <code>logMessage</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLogMessage() {
        return (logMessage);
    }

    /**
     * <p>Getter for the field <code>logGroup</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLogGroup() {
        return (logGroup);
    }

    /**
     * <p>Getter for the field <code>operatorInstruction</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOperatorInstruction() {
        return (operatorInstruction);
    }

    /**
     * <p>Getter for the field <code>autoAction</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAutoAction() {
        return (autoAction);
    }

    /**
     * <p>Getter for the field <code>operatorAction</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOperatorAction() {
        return (operatorAction);
    }

    /**
     * <p>Getter for the field <code>operatorActionMenuText</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOperatorActionMenuText() {
        return (operatorActionMenuText);
    }

    /**
     * <p>Getter for the field <code>notification</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNotification() {
        return (notification);
    }

    /**
     * <p>Getter for the field <code>troubleTicket</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTroubleTicket() {
        return (troubleTicket);
    }

    /**
     * <p>Getter for the field <code>troubleTicketState</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getTroubleTicketState() {
        return (troubleTicketState);
    }

    /**
     * <p>Getter for the field <code>forward</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForward() {
        return (forward);
    }

    /**
     * <p>Getter for the field <code>mouseOverText</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMouseOverText() {
        return (mouseOverText);
    }

    /**
     * <p>Getter for the field <code>acknowledgeUser</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAcknowledgeUser() {
        return (acknowledgeUser);
    }

    /**
     * <p>Getter for the field <code>acknowledgeTime</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getAcknowledgeTime() {
        return (acknowledgeTime);
    }

    /**
     * <p>isAcknowledged</p>
     *
     * @return a boolean.
     */
    public boolean isAcknowledged() {
        return (acknowledgeUser != null);
    }

    /**
     * <p>Getter for the field <code>alarmId</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getAlarmId() {
        return (alarmId);
    }

    /**
     * <p>Getter for the field <code>eventDisplay</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getEventDisplay() {
        return (eventDisplay);
    }

    public String getSystemId() {
        return systemId;
    }

    public String getLocation() {
        return location;
    }
    
    public String getNodeLocation() {
        return nodeLocation;
    }
}
