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
// Modifications:
//
// 2007 Jul 24: Java 5 generics and some code formatting. - dj@opennms.org
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

package org.opennms.web.notification;

import java.util.Date;
import java.util.List;

/**
 * Notify Bean, containing data from the notifications table.
 */
public class Notification {
    // Fields from the notifications table.
    // User id of the person being paged.
    public List<NoticeSentTo> m_sentTo;

    // Text Message being sent in the page
    public String m_txtMsg;

    // Numeric Message being sent in the page
    public String m_numMsg;

    // Notify ID
    public int m_notifyID;

    // Time the page was sent in milliseconds.
    public long m_timeSent;

    // Time the page was responded in milliseconds.
    public long m_timeReply;

    // Responder.
    public String m_responder;

    // Contact info.
    // public String m_contactInfo;

    // Problem Node
    public int m_nodeID;

    // ID of the interface on problem node.
    public String m_interfaceID;

    // Name of the problem service on the interface.
    public String m_serviceName;

    // ID of the problem service on the interface.
    public int m_serviceId;

    // ID of the event that triggered this notification
    public int m_eventId;

    /**
     * Default Constructor
     */
    public Notification() {
    }

    public Notification(List<NoticeSentTo> sentToList, int notifyId, String txtMsg, String numMsg, long timeSent, long timeReply, String responder, int nodeid, String interfaceID, int svcId, String svcName, int eventid) {
        m_sentTo = sentToList;
        m_notifyID = notifyId;
        m_txtMsg = txtMsg;
        m_numMsg = numMsg;
        m_timeSent = timeSent;
        m_timeReply = timeReply;
        m_responder = responder;
        m_nodeID = nodeid;
        m_interfaceID = interfaceID;
        m_serviceId = svcId;
        m_serviceName = svcName;
        m_eventId = eventid;
    }

    public List<NoticeSentTo> getSentTo() {
        return (this.m_sentTo);
    }

    public String getTextMessage() {
        return (this.m_txtMsg);
    }

    public String getNumericMessage() {
        return (this.m_numMsg);
    }

    public int getId() {
        return (this.m_notifyID);
    }

    public Date getTimeSent() {
        return (new Date(this.m_timeSent));
    }

    public Date getTimeReplied() {

        if (this.m_timeReply == 0) {
            return null;
        }

        return (new Date(this.m_timeReply));
    }

    public String getResponder() {
        return (this.m_responder);
    }

    public int getNodeId() {
        return (this.m_nodeID);
    }

    public String getInterfaceId() {
        return (this.m_interfaceID);
    }

    public String getIpAddress() {
        return (this.m_interfaceID);
    }

    public String getServiceName() {
        return (this.m_serviceName);
    }

    public int getServiceId() {
        return (this.m_serviceId);
    }

    public int getEventId() {
        return (this.m_eventId);
    }

    /*
     * public String toString() { StringBuffer str = new StringBuffer();
     * str.append("Txt Msg " + m_txtMsg + " \n"); str.append("Num Msg " +
     * m_numMsg + " \n"); str.append("time Sent " + this.getTimeSent() + " \n");
     * str.append("Reply time " + getTimeReplied() + " \n");
     * str.append("Responder " + this.getResponder() + " \n"); str.append("Node
     * ID " + m_nodeID+ "\n"); str.append("Interface ID" + m_interfaceID +
     * "\n"); str.append("Service ID : "+ m_serviceId + "\n" );
     * str.append("Service Name : "+m_serviceName + "\n"); return
     * str.toString(); }
     */
}
