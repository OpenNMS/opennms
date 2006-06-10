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
import java.util.Date;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="alarms"
 *     
*/
public class OnmsAlarm implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1342362989494090682L;

    /** identifier field */
    private Integer m_id;

    /** persistent field */
    private String m_uei;

    /** persistent field */
    private OnmsDistPoller m_distPoller;

    /** nullable persistent field */
    private OnmsNode m_node;

    /** nullable persistent field */
    private String m_ipAddr;

    /** nullable persistent field */
    private OnmsMonitoredService m_service;

    /** nullable persistent field */
    private String m_reductionKey;

    /** nullable persistent field */
    private Integer m_alarmType;

    /** persistent field */
    private Integer m_counter;

    /** persistent field */
    private Integer m_severity;

    /** persistent field */
    private Date m_firstEventTime;

    /** nullable persistent field */
    private String m_description;

    /** nullable persistent field */
    private String m_logMsg;

    /** nullable persistent field */
    private String m_operInstruct;

    /** nullable persistent field */
    private String m_tTicketId;

    /** nullable persistent field */
    private Integer m_tTicketState;

    /** nullable persistent field */
    private String m_mouseOverText;

    /** nullable persistent field */
    private Date m_suppressedUntil;

    /** nullable persistent field */
    private String m_suppressedUser;

    /** nullable persistent field */
    private Date m_suppressedTime;

    /** nullable persistent field */
    private String m_alarmAckUser;

    /** nullable persistent field */
    private Date m_alarmAckTime;

    /** nullable persistent field */
    private String m_clearUei;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsEvent m_lastEvent;

    /** full constructor */
    public OnmsAlarm(Integer alarmid, String eventuei, OnmsDistPoller distPoller, OnmsNode node, String ipaddr, OnmsMonitoredService service, String reductionkey, Integer alarmtype, Integer counter, Integer severity, Date firsteventtime, String description, String logmsg, String operinstruct, String tticketid, Integer tticketstate, String mouseovertext, Date suppresseduntil, String suppresseduser, Date suppressedtime, String alarmackuser, Date alarmacktime, String clearuei, org.opennms.netmgt.model.OnmsEvent event) {
        this.m_id = alarmid;
        this.m_uei = eventuei;
        this.m_distPoller = distPoller;
        this.m_node = node;
        this.m_ipAddr = ipaddr;
        this.m_service = service;
        this.m_reductionKey = reductionkey;
        this.m_alarmType = alarmtype;
        this.m_counter = counter;
        this.m_severity = severity;
        this.m_firstEventTime = firsteventtime;
        this.m_description = description;
        this.m_logMsg = logmsg;
        this.m_operInstruct = operinstruct;
        this.m_tTicketId = tticketid;
        this.m_tTicketState = tticketstate;
        this.m_mouseOverText = mouseovertext;
        this.m_suppressedUntil = suppresseduntil;
        this.m_suppressedUser = suppresseduser;
        this.m_suppressedTime = suppressedtime;
        this.m_alarmAckUser = alarmackuser;
        this.m_alarmAckTime = alarmacktime;
        this.m_clearUei = clearuei;
        this.m_lastEvent = event;
    }

    /** default constructor */
    public OnmsAlarm() {
    }

    /** minimal constructor */
    public OnmsAlarm(Integer alarmid, String eventuei, OnmsDistPoller distPoller, Integer counter, Integer severity, Date firsteventtime, OnmsEvent event) {
        this.m_id = alarmid;
        this.m_uei = eventuei;
        this.m_distPoller = distPoller;
        this.m_counter = counter;
        this.m_severity = severity;
        this.m_firstEventTime = firsteventtime;
        this.m_lastEvent = event;
    }

    /** 
     *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="alarmid"
     *         
     */
    public Integer getId() {
        return this.m_id;
    }

    public void setId(Integer alarmid) {
        this.m_id = alarmid;
    }

    /** 
     *            @hibernate.property
     *             column="eventuei"
     *             length="256"
     *             not-null="true"
     *         
     */
    public String getUei() {
        return this.m_uei;
    }

    public void setUei(String eventuei) {
        this.m_uei = eventuei;
    }

    /** 
     *            @hibernate.property
     *             column="dpname"
     *             length="12"
     *             not-null="true"
     *         
     */
    public OnmsDistPoller getDistPoller() {
        return this.m_distPoller;
    }

    public void setDistPoller(OnmsDistPoller distPoller) {
        this.m_distPoller = distPoller;
    }

    /** 
     *            @hibernate.property
     *             column="nodeid"
     *             length="4"
     *         
     */
    public OnmsNode getNode() {
        return this.m_node;
    }

    public void setNode(OnmsNode node) {
        this.m_node = node;
    }

    /** 
     *            @hibernate.property
     *             column="ipaddr"
     *             length="16"
     *         
     */
    public String getIpAddr() {
        return this.m_ipAddr;
    }

    public void setIpAddr(String ipaddr) {
        this.m_ipAddr = ipaddr;
    }

    /** 
     *            @hibernate.property
     *             column="serviceid"
     *             length="4"
     *         
     */
    public OnmsMonitoredService getService() {
        return this.m_service;
    }

    public void setService(OnmsMonitoredService service) {
        this.m_service = service;
    }

    /** 
     *            @hibernate.property
     *             column="reductionkey"
     *             unique="true"
     *             length="256"
     *         
     */
    public String getReductionKey() {
        return this.m_reductionKey;
    }

    public void setReductionKey(String reductionkey) {
        this.m_reductionKey = reductionkey;
    }

    /** 
     *            @hibernate.property
     *             column="alarmtype"
     *             length="4"
     *         
     */
    public Integer getAlarmType() {
        return this.m_alarmType;
    }

    public void setAlarmType(Integer alarmtype) {
        this.m_alarmType = alarmtype;
    }

    /** 
     *            @hibernate.property
     *             column="counter"
     *             length="4"
     *             not-null="true"
     *         
     */
    public Integer getCounter() {
        return this.m_counter;
    }

    public void setCounter(Integer counter) {
        this.m_counter = counter;
    }

    /** 
     *            @hibernate.property
     *             column="severity"
     *             length="4"
     *             not-null="true"
     *         
     */
    public Integer getSeverity() {
        return this.m_severity;
    }

    public void setSeverity(Integer severity) {
        this.m_severity = severity;
    }

    /** 
     *            @hibernate.property
     *             column="firsteventtime"
     *             length="8"
     *             not-null="true"
     *         
     */
    public Date getFirstEventTime() {
        return this.m_firstEventTime;
    }

    public void setFirstEventTime(Date firsteventtime) {
        this.m_firstEventTime = firsteventtime;
    }

    /** 
     *            @hibernate.property
     *             column="description"
     *             length="4000"
     *         
     */
    public String getDescription() {
        return this.m_description;
    }

    public void setDescription(String description) {
        this.m_description = description;
    }

    /** 
     *            @hibernate.property
     *             column="logmsg"
     *             length="256"
     *         
     */
    public String getLogMsg() {
        return this.m_logMsg;
    }

    public void setLogMsg(String logmsg) {
        this.m_logMsg = logmsg;
    }

    /** 
     *            @hibernate.property
     *             column="operinstruct"
     *             length="1024"
     *         
     */
    public String getOperInstruct() {
        return this.m_operInstruct;
    }

    public void setOperInstruct(String operinstruct) {
        this.m_operInstruct = operinstruct;
    }

    /** 
     *            @hibernate.property
     *             column="tticketid"
     *             length="128"
     *         
     */
    public String getTTicketId() {
        return this.m_tTicketId;
    }

    public void setTTicketId(String tticketid) {
        this.m_tTicketId = tticketid;
    }

    /** 
     *            @hibernate.property
     *             column="tticketstate"
     *             length="4"
     *         
     */
    public Integer getTTicketState() {
        return this.m_tTicketState;
    }

    public void setTTicketState(Integer tticketstate) {
        this.m_tTicketState = tticketstate;
    }

    /** 
     *            @hibernate.property
     *             column="mouseovertext"
     *             length="64"
     *         
     */
    public String getMouseOverText() {
        return this.m_mouseOverText;
    }

    public void setMouseOverText(String mouseovertext) {
        this.m_mouseOverText = mouseovertext;
    }

    /** 
     *            @hibernate.property
     *             column="suppresseduntil"
     *             length="8"
     *         
     */
    public Date getSuppressedUntil() {
        return this.m_suppressedUntil;
    }

    public void setSuppressedUntil(Date suppresseduntil) {
        this.m_suppressedUntil = suppresseduntil;
    }

    /** 
     *            @hibernate.property
     *             column="suppresseduser"
     *             length="256"
     *         
     */
    public String getSuppressedUser() {
        return this.m_suppressedUser;
    }

    public void setSuppressedUser(String suppresseduser) {
        this.m_suppressedUser = suppresseduser;
    }

    /** 
     *            @hibernate.property
     *             column="suppressedtime"
     *             length="8"
     *         
     */
    public Date getSuppressedTime() {
        return this.m_suppressedTime;
    }

    public void setSuppressedTime(Date suppressedtime) {
        this.m_suppressedTime = suppressedtime;
    }

    /** 
     *            @hibernate.property
     *             column="alarmackuser"
     *             length="256"
     *         
     */
    public String getAlarmAckUser() {
        return this.m_alarmAckUser;
    }

    public void setAlarmAckUser(String alarmackuser) {
        this.m_alarmAckUser = alarmackuser;
    }

    /** 
     *            @hibernate.property
     *             column="alarmacktime"
     *             length="8"
     *         
     */
    public Date getAlarmAckTime() {
        return this.m_alarmAckTime;
    }

    public void setAlarmAckTime(Date alarmacktime) {
        this.m_alarmAckTime = alarmacktime;
    }

    /** 
     *            @hibernate.property
     *             column="clearuei"
     *             length="256"
     *         
     */
    public String getClearUei() {
        return this.m_clearUei;
    }

    public void setClearUei(String clearuei) {
        this.m_clearUei = clearuei;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="lasteventid"         
     *         
     */
    public org.opennms.netmgt.model.OnmsEvent getLastEvent() {
        return this.m_lastEvent;
    }

    public void setLastEvent(org.opennms.netmgt.model.OnmsEvent event) {
        this.m_lastEvent = event;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("alarmid", getId())
            .toString();
    }

}
