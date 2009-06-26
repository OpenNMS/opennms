//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// 2008 Oct 04: Move Severity inner class into sharable OnmsSeverity model class. - dj@opennms.org
// 2008 Sep 27: Move Severity-related code here in a Java 5 enum class
//              from AlarmUtil and use new class internally. - dj@opennms.org
// 2005 Apr 18: This file was created from Event.java
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.alarm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;

/**
 * A JavaBean implementation to hold information about a network alarm as
 * defined by OpenNMS.
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class Alarm extends Object {
    public static final int PROBLEM_TYPE = 1;
    
    public static final int RESOLUTION_TYPE = 2;

    /** Unique identifier for the alarm, cannot be null */
    protected int id;

    /** Universal Event Identifer (UEI) for this alarm, cannot be null */
    protected String uei;

    /** The dpName of the Dist Poller which received the alarm, cannot be null. */
    protected String dpName;

    /** Unique integer identifier for node, can be null */
    protected Integer nodeID;

    /** IP Address of node's interface */
    protected String ipAddr;

    /** Unique integer identifier of service/poller package, can be null */
    protected Integer serviceID;

    /** Reduction key for this alarm, cannot be null */
    protected String reductionKey;

    /** Reduction count for the alarm, cannot be null */
    protected int count;

    /**
     * Severity the of alarm.
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

    /** The last event to be reduced by this alarm */
    protected int lastEventID;

    /**
     * The first time an event was reduced by this alarm
     */
    protected Date firsteventtime;

    /**
     * The last time an event was reduced by this alarm
     */
    protected Date lasteventtime;

    /** Free-form textual description of the alarm */
    protected String description;

    /**
     * Formatted display text to control how the alarm will appear in the
     * browser. This field may contain variables that are populated by field
     * values of the message.
     */
    protected String logMessage;

    /** Operator instruction for event. */
    protected String operatorInstruction;

    /**
     * Trouble ticket id.  This represents the id as returned from a trouble ticketing system
     * or null if not trouble ticket exists.
     */
    protected String troubleTicket;

    /**
     * State of the trouble ticket. Trouble ticket on/off boolean 1=on, 0=off.
     * Can be null.
     */
    protected TroubleTicketState troubleTicketState;

    /**
     * Mouse over text. Text to be displayed on MouseOver event, if the event is
     * displayed in the browser and the operator needs additional info.
     */
    protected String mouseOverText;

    /** The time that suppression will end for this alarm. */
    protected Date suppressedUntil;

    /** The name of the user who suppressed this alarm. */
    protected String suppressedUser;

    /** The time this alarm was suppressed. */
    protected Date suppressedTime;

    /** The name of the user who acknowledged this alarm. */
    protected String acknowledgeUser;

    /** The time this alarm was acknowledged. */
    protected Date acknowledgeTime;

    /** The &lt;parms&gt; element for this alarm.*/
    protected String parms;

    /** Human-readable name of the service */
    protected String serviceName;

    /** The human-readable name of the node of this alarm. Can be null. */
    protected String nodeLabel;

    /**
     * Empty constructor to create an empty <code>Alarm</code> instance. All
     * fields will hold the default values.
     */
    public Alarm() {
    }

    /**
     * Create an alarm that represents a real network alarm with only the
     * required parameters.
     */
    public Alarm(int id, String uei, String dpName, Date lasteventtime, Date firsteventtime, int count, int severityId) {
        if (uei == null || dpName == null || lasteventtime == null || firsteventtime == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.id = id;
        this.uei = uei;
        this.dpName = dpName;
        this.lasteventtime = lasteventtime;
        this.firsteventtime = firsteventtime;
        this.count = count;
        this.severity = OnmsSeverity.get(severityId);
    }

    /**
     * Create an alarm that represents a real network alarm with all the
     * parameters.
     */
    public Alarm(int id, String uei, String dpName, Integer nodeID, String ipAddr, Integer serviceID, String reductionKey, int count, int severity, int lastEventID, Date firsteventtime, Date lasteventtime, String description, String logMessage, String operatorInstruction, String troubleTicket, TroubleTicketState troubleTicketState, String mouseOverText, Date suppressedUntil, String suppressedUser, Date suppressedTime, String acknowledgeUser, Date acknowledgeTime, String parms) {
    	this(id, uei, dpName, nodeID, ipAddr, serviceID, reductionKey, count, severity, lastEventID, firsteventtime, lasteventtime, description, logMessage, operatorInstruction, troubleTicket, troubleTicketState, mouseOverText, suppressedUntil, suppressedUser, suppressedTime, acknowledgeUser, acknowledgeTime, parms, null, null);
    }

    /**
     * Create an alarm that represents a real network alarm with all the
     * parameters.
     */
    public Alarm(int id, String uei, String dpName, Integer nodeID, String ipAddr, Integer serviceID, String reductionKey, int count, int severityId, int lastEventID, Date firsteventtime, Date lasteventtime, String description, String logMessage, String operatorInstruction, String troubleTicket, TroubleTicketState troubleTicketState, String mouseOverText, Date suppressedUntil, String suppressedUser, Date suppressedTime, String acknowledgeUser, Date acknowledgeTime, String parms, String nodeLabel, String serviceName) {

        if (uei == null || dpName == null || lasteventtime == null || firsteventtime == null ) {
            throw new IllegalArgumentException("Cannot take null values for the following parameters: uei, dpName, firsteventtime, lasteventtime.");
        }

        // required fields
        this.id = id;
        this.uei = uei;
        this.dpName = dpName;
        this.lasteventtime = lasteventtime;
        this.firsteventtime = firsteventtime;
	this.count = count;
        this.severity = OnmsSeverity.get(severityId);

        // optional fields
    	this.nodeID = nodeID;
	this.ipAddr = ipAddr;
	this.serviceID = serviceID;
	this.reductionKey = reductionKey;
	this.lastEventID = lastEventID;
	this.description = description;
	this.logMessage = logMessage;
	this.operatorInstruction = operatorInstruction;
	this.troubleTicket = troubleTicket;
	this.troubleTicketState = troubleTicketState;
	this.mouseOverText = mouseOverText;
	this.suppressedUntil = suppressedUntil;
	this.suppressedUser = suppressedUser;
	this.suppressedTime = suppressedTime;
	this.acknowledgeUser = acknowledgeUser;
	this.acknowledgeTime = acknowledgeTime;
        this.parms = parms;
        this.nodeLabel = nodeLabel;
        this.serviceName = serviceName;

    }

    public int getId() {
        return (this.id);
    }

    public String getUei() {
        return (this.uei);
    }

    public String getDpName() {
        return (this.dpName);
    }

    public Date getLastEventTime() {
        return (lasteventtime);
    }

    public Date getFirstEventTime() {
        return (firsteventtime);
    }

    public int getCount() {
        return (this.count);
    }

    public OnmsSeverity getSeverity() {
        return severity;
    }
    
    public int getNodeId() {
        return (this.nodeID.intValue());
    }

    public String getIpAddress() {
        return (this.ipAddr);
    }

    public int getServiceId() {
        return (this.serviceID.intValue());
    }

    public String getReductionKey() {
        return (this.reductionKey);
    }

    public int getLastEventID() {
        return (this.lastEventID);
    }

    public String getDescription() {
        return (this.description);
    }

    public String getLogMessage() {
        return (this.logMessage);
    }

    public String getOperatorInstruction() {
        return (this.operatorInstruction);
    }

    public String getTroubleTicket() {
        return (this.troubleTicket);
    }

    public TroubleTicketState getTroubleTicketState() {
        return (this.troubleTicketState);
    }

    public String getMouseOverText() {
        return (this.mouseOverText);
    }

    public Date getSuppressedUntil() {
        return (this.suppressedUntil);
    }

    public String getSuppressedUser() {
        return (this.suppressedUser);
    }

    public Date getSuppressedTime() {
        return (this.suppressedTime);
    }

    public String getAcknowledgeUser() {
        return (this.acknowledgeUser);
    }

    public Date getAcknowledgeTime() {
        return (this.acknowledgeTime);
    }

    public boolean isAcknowledged() {
        return (this.acknowledgeUser != null);
    }

    public String getParms() {
        return (this.parms);
    }

    public String getNodeLabel() {
        return (this.nodeLabel);
    }

    public String getServiceName() {
        return (this.serviceName);
    }


}
