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
package org.opennms.web.notification;

import java.util.Date;
import java.util.List;

/**
 * Notify Bean, containing data from the notifications table.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
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

    /**
     * <p>Constructor for Notification.</p>
     *
     * @param sentToList a {@link java.util.List} object.
     * @param notifyId a int.
     * @param txtMsg a {@link java.lang.String} object.
     * @param numMsg a {@link java.lang.String} object.
     * @param timeSent a long.
     * @param timeReply a long.
     * @param responder a {@link java.lang.String} object.
     * @param nodeid a int.
     * @param interfaceID a {@link java.lang.String} object.
     * @param svcId a int.
     * @param svcName a {@link java.lang.String} object.
     * @param eventid a int.
     */
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

    /**
     * <p>getSentTo</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<NoticeSentTo> getSentTo() {
        return (this.m_sentTo);
    }

    /**
     * <p>getTextMessage</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextMessage() {
        return (this.m_txtMsg);
    }

    /**
     * <p>getNumericMessage</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNumericMessage() {
        return (this.m_numMsg);
    }

    /**
     * <p>getId</p>
     *
     * @return a int.
     */
    public int getId() {
        return (this.m_notifyID);
    }

    /**
     * <p>getTimeSent</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getTimeSent() {
        return (new Date(this.m_timeSent));
    }

    /**
     * <p>getTimeReplied</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getTimeReplied() {

        if (this.m_timeReply == 0) {
            return null;
        }

        return (new Date(this.m_timeReply));
    }

    /**
     * <p>getResponder</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResponder() {
        return (this.m_responder);
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return (this.m_nodeID);
    }

    /**
     * <p>getInterfaceId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInterfaceId() {
        return (this.m_interfaceID);
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return (this.m_interfaceID);
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return (this.m_serviceName);
    }

    /**
     * <p>getServiceId</p>
     *
     * @return a int.
     */
    public int getServiceId() {
        return (this.m_serviceId);
    }

    /**
     * <p>getEventId</p>
     *
     * @return a int.
     */
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
