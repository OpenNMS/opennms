/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.tip.ram;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>OnmsAlarm class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="alarm")
public class Alarm  {
    /*
     * <alarm severity="CLEARED" id="1712" count="2" type="1">
     *     <ackTime>2011-03-04T06:47:41.164-05:00</ackTime>
     *     <ackUser>demo</ackUser>
     *     <description> &lt;p&gt;A FTP outage was identified on interface 65.41.39.146.&lt;/p&gt; &lt;p&gt;A new Outage record has been created and service level availability calculations will be impacted until this outage is resolved.&lt;/p&gt;</description>
     *     <parms>eventReason=did not connect to host with timeout: 3000ms retry: 1 of 1(string,text)</parms>
     *     <firstAutomationTime>2011-03-10T11:34:36.474-05:00</firstAutomationTime>
     *     <firstEventTime>2011-03-03T04:48:35-05:00</firstEventTime>
     *     <ipAddress>65.41.39.146</ipAddress>
     *     <lastAutomationTime>2011-03-10T11:34:36.474-05:00</lastAutomationTime>
     *     <lastEvent severity="MINOR" id="56909" log="Y" display="Y">
     *         <createTime>2011-03-10T11:20:44.860-05:00</createTime>
     *         <description> &lt;p&gt;A FTP outage was identified on interface 65.41.39.146.&lt;/p&gt; &lt;p&gt;A new Outage record has been created and service level availability calculations will be impacted until this outage is resolved.&lt;/p&gt;</description>
     *         <host>mephesto.internal.opennms.com</host>
     *         <logMessage>FTP outage identified on interface 65.41.39.146 with reason code: did not connect to host with timeout: 3000ms retry: 1 of 1.</logMessage><parms>eventReason=did not connect to host with timeout: 3000ms retry: 1 of 1(string,text)</parms>
     *         <source>OpenNMS.Poller.DefaultPollContext</source>
     *         <time>2011-03-10T11:20:44-05:00</time>
     *         <uei>uei.opennms.org/nodes/nodeLostService</uei>
     *         <ipAddress>65.41.39.146</ipAddress>
     *         <nodeId>2</nodeId>
     *     </lastEvent>
     *     <lastEventTime>2011-03-10T11:20:44-05:00</lastEventTime>
     *     <logMessage>FTP outage identified on interface 65.41.39.146 with reason code: did not connect to host with timeout: 3000ms retry: 1 of 1.</logMessage>
     *     <reductionKey>uei.opennms.org/nodes/nodeLostService::2:65.41.39.146:FTP</reductionKey>
     *     <suppressedTime>2011-03-03T04:48:35-05:00</suppressedTime>
     *     <suppressedUntil>2011-03-03T04:48:35-05:00</suppressedUntil>
     *     <uei>uei.opennms.org/nodes/nodeLostService</uei>
     *     <x733ProbableCause>0</x733ProbableCause>
     * </alarm>
    */
    
    private Integer m_id;
    private Integer m_alarmType;
    private Integer m_counter;
    private String m_severity;
    private Integer m_ifIndex;
    private String m_uei;
    private String m_ipAddr;
    private String m_reductionKey;
    private String m_logMsg;
    private String m_operInstruct;
    private String m_tTicketId;
    private String m_tTicketState;
    private String m_mouseOverText;
    private Date m_suppressedUntil;
    private String m_suppressedUser;
    private Date m_suppressedTime;
    private String m_alarmAckUser;
    private Date m_alarmAckTime;
    private String m_clearKey;
    private Event m_lastEvent;
    private String m_eventParms;
    private Date m_lastEventTime;
    private String m_applicationDN;
    private Date m_firstAutomationTime;
    private Date m_lastAutomationTime;

    @XmlAttribute(name="id")
    public Integer getId() {
        return m_id;
    }
    
    @XmlAttribute(name="type")
    public Integer getAlarmType() {
        return m_alarmType;
    }

    @XmlAttribute(name="count")
    public Integer getCounter() {
        return m_counter;
    }
    
    @XmlAttribute(name="severity")
    public String getSeverity() {
        return m_severity;
    }
    
    @XmlAttribute(name="ifIndex")
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    @XmlElement(name="uei")
    public String getUei() {
        return m_uei;
    }

    @XmlElement(name="ipAddress")
    public String getIpAddr() {
        return m_ipAddr;
    }

    @XmlElement(name="reductionKey")
    public String getReductionKey() {
        return m_reductionKey;
    }

    @XmlElement(name="logMessage")
    public String getLogMsg() {
        return m_logMsg;
    }

    @XmlElement(name="operatorInstructions")
    public String getOperInstruct() {
        return m_operInstruct;
    }

    @XmlElement(name="troubleTicket")
    public String getTTicketId() {
        return m_tTicketId;
    }

    @XmlElement(name="troubleTicketState")
    public String getTTicketState() {
        return m_tTicketState;
    }

    @XmlElement(name="mouseOverText")
    public String getMouseOverText() {
        return m_mouseOverText;
    }

    @XmlElement(name="suppressedUntil")
    public Date getSuppressedUntil() {
        return m_suppressedUntil;
    }

    @XmlElement(name="suppressedBy")
    public String getSuppressedUser() {
        return m_suppressedUser;
    }

    @XmlElement(name="suppressedTime")
    public Date getSuppressedTime() {
        return m_suppressedTime;
    }

    @XmlElement(name="ackUser")
    public String getAlarmAckUser() {
        return m_alarmAckUser;
    }

    @XmlElement(name="ackTime")
    public Date getAlarmAckTime() {
        return m_alarmAckTime;
    }

    @XmlElement(name="clearKey")
    public String getClearKey() {
        return m_clearKey;
    }

    @XmlElement(name="lastEvent")
    public Event getLastEvent() {
        return m_lastEvent;
    }
    
    @XmlElement(name="parms")
    public String getEventParms() {
        return m_eventParms;
    }
    
    @XmlElement(name="lastEventTime")
    public Date getLastEventTime() {
        return m_lastEventTime;
    }

    @XmlElement(name="applicationDN")
    public String getApplicationDN() {
        return m_applicationDN;
    }
    
    @XmlElement(name="firstAutomationTime")
    public Date getFirstAutomationTime() {
        return m_firstAutomationTime;
    }
    
    @XmlElement(name="lastAutomationTime")
    public Date getLastAutomationTime() {
        return m_lastAutomationTime;
    }

    public String gettTicketId() {
        return m_tTicketId;
    }

    public void settTicketId(String tTicketId) {
        m_tTicketId = tTicketId;
    }

    public String gettTicketState() {
        return m_tTicketState;
    }

    public void settTicketState(String tTicketState) {
        m_tTicketState = tTicketState;
    }

    public void setId(Integer id) {
        m_id = id;
    }

    public void setAlarmType(Integer alarmType) {
        m_alarmType = alarmType;
    }

    public void setCounter(Integer counter) {
        m_counter = counter;
    }

    public void setSeverity(String severity) {
        m_severity = severity;
    }

    public void setIfIndex(Integer ifIndex) {
        m_ifIndex = ifIndex;
    }

    public void setUei(String uei) {
        m_uei = uei;
    }

    public void setIpAddr(String ipAddr) {
        m_ipAddr = ipAddr;
    }

    public void setReductionKey(String reductionKey) {
        m_reductionKey = reductionKey;
    }

    public void setLogMsg(String logMsg) {
        m_logMsg = logMsg;
    }

    public void setOperInstruct(String operInstruct) {
        m_operInstruct = operInstruct;
    }

    public void setMouseOverText(String mouseOverText) {
        m_mouseOverText = mouseOverText;
    }

    public void setSuppressedUntil(Date suppressedUntil) {
        m_suppressedUntil = suppressedUntil;
    }

    public void setSuppressedUser(String suppressedUser) {
        m_suppressedUser = suppressedUser;
    }

    public void setSuppressedTime(Date suppressedTime) {
        m_suppressedTime = suppressedTime;
    }

    public void setAlarmAckUser(String alarmAckUser) {
        m_alarmAckUser = alarmAckUser;
    }

    public void setAlarmAckTime(Date alarmAckTime) {
        m_alarmAckTime = alarmAckTime;
    }

    public void setClearKey(String clearKey) {
        m_clearKey = clearKey;
    }

    public void setLastEvent(Event lastEvent) {
        m_lastEvent = lastEvent;
    }

    public void setEventParms(String eventParms) {
        m_eventParms = eventParms;
    }

    public void setLastEventTime(Date lastEventTime) {
        m_lastEventTime = lastEventTime;
    }

    public void setApplicationDN(String applicationDN) {
        m_applicationDN = applicationDN;
    }

    public void setFirstAutomationTime(Date firstAutomationTime) {
        m_firstAutomationTime = firstAutomationTime;
    }

    public void setLastAutomationTime(Date lastAutomationTime) {
        m_lastAutomationTime = lastAutomationTime;
    }

    @Override
    public String toString() {
        return "Alarm [m_id=" + m_id + ", m_alarmType=" + m_alarmType
                + ", m_counter=" + m_counter + ", m_severity=" + m_severity
                + ", m_ifIndex=" + m_ifIndex + ", m_uei=" + m_uei
                + ", m_ipAddr=" + m_ipAddr + ", m_reductionKey="
                + m_reductionKey + ", m_logMsg=" + m_logMsg
                + ", m_operInstruct=" + m_operInstruct + ", m_tTicketId="
                + m_tTicketId + ", m_tTicketState=" + m_tTicketState
                + ", m_mouseOverText=" + m_mouseOverText
                + ", m_suppressedUntil=" + m_suppressedUntil
                + ", m_suppressedUser=" + m_suppressedUser
                + ", m_suppressedTime=" + m_suppressedTime
                + ", m_alarmAckUser=" + m_alarmAckUser + ", m_alarmAckTime="
                + m_alarmAckTime + ", m_clearKey=" + m_clearKey
                + ", m_lastEvent=" + m_lastEvent + ", m_eventParms="
                + m_eventParms + ", m_lastEventTime=" + m_lastEventTime
                + ", m_applicationDN=" + m_applicationDN
                + ", m_firstAutomationTime=" + m_firstAutomationTime
                + ", m_lastAutomationTime=" + m_lastAutomationTime + "]";
    }

}
