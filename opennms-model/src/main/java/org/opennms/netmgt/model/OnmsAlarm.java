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
// Modifications:
//
// 2008 Sep 26: Move some of the alarm "constant" information into here
// 2007 Apr 05: Make annotation nullability and default values match what's in create.sql. - dj@opennms.org
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
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;
import org.springframework.core.style.ToStringCreator;

@Entity
/**
 * <p>OnmsAlarm class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Table(name="alarms")
public class OnmsAlarm implements Serializable {
    private static final long serialVersionUID = 7275548439687562161L;

    /** Constant <code>INDETERMINATE_SEVERITY=1</code> */
    public static final int INDETERMINATE_SEVERITY = 1;

    /** Constant <code>CLEARED_SEVERITY=2</code> */
    public static final int CLEARED_SEVERITY = 2;

    /** Constant <code>NORMAL_SEVERITY=3</code> */
    public static final int NORMAL_SEVERITY = 3;

    /** Constant <code>WARNING_SEVERITY=4</code> */
    public static final int WARNING_SEVERITY = 4;

    /** Constant <code>MINOR_SEVERITY=5</code> */
    public static final int MINOR_SEVERITY = 5;

    /** Constant <code>MAJOR_SEVERITY=6</code> */
    public static final int MAJOR_SEVERITY = 6;

    /** Constant <code>CRITICAL_SEVERITY=7</code> */
    public static final int CRITICAL_SEVERITY = 7;
    
    /** Constant <code>PROBLEM_TYPE=1</code> */
    public static final int PROBLEM_TYPE = 1;
    
    /** Constant <code>RESOLUTION_TYPE=2</code> */
    public static final int RESOLUTION_TYPE = 2;

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
    private OnmsServiceType m_serviceType;

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
    
    /** persistent field */
    private Date m_lastEventTime;

    /** persistent field */
    private Date m_firstAutomationTime;
    
    /** persistent field */
    private Date m_lastAutomationTime;

    /** nullable persistent field */
    private String m_description;

    /** nullable persistent field */
    private String m_logMsg;

    /** nullable persistent field */
    private String m_operInstruct;

    /** nullable persistent field */
    private String m_tTicketId;
    
      /** nullable persistent field */
    private TroubleTicketState m_tTicketState;

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
    
    /** nullable persistent field */
    private String m_clearKey;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsEvent m_lastEvent;

    /** persistent field */
    private String m_managedObjectInstance;
    
    /** persistent field */
    private String m_managedObjectType;
    
    /** persistent field */
    private String m_applicationDN;

    private String m_ossPrimaryKey;

    private String m_x733AlarmType;

    private String m_qosAlarmState;

    private int m_x733ProbableCause = 0;
    
	private Map<String, String> m_details;

    /**
     * full constructor
     *
     * @param alarmid a {@link java.lang.Integer} object.
     * @param eventuei a {@link java.lang.String} object.
     * @param distPoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @param ipaddr a {@link java.lang.String} object.
     * @param serviceType a {@link org.opennms.netmgt.model.OnmsServiceType} object.
     * @param reductionkey a {@link java.lang.String} object.
     * @param alarmtype a {@link java.lang.Integer} object.
     * @param counter a {@link java.lang.Integer} object.
     * @param severity a {@link java.lang.Integer} object.
     * @param firsteventtime a {@link java.util.Date} object.
     * @param description a {@link java.lang.String} object.
     * @param logmsg a {@link java.lang.String} object.
     * @param operinstruct a {@link java.lang.String} object.
     * @param tticketid a {@link java.lang.String} object.
     * @param tticketstate a {@link org.opennms.netmgt.model.TroubleTicketState} object.
     * @param mouseovertext a {@link java.lang.String} object.
     * @param suppresseduntil a {@link java.util.Date} object.
     * @param suppresseduser a {@link java.lang.String} object.
     * @param suppressedtime a {@link java.util.Date} object.
     * @param alarmackuser a {@link java.lang.String} object.
     * @param alarmacktime a {@link java.util.Date} object.
     * @param clearuei a {@link java.lang.String} object.
     * @param managedObjectInstance a {@link java.lang.String} object.
     * @param managedObjectType a {@link java.lang.String} object.
     * @param event a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    public OnmsAlarm(Integer alarmid, String eventuei, OnmsDistPoller distPoller, OnmsNode node, String ipaddr, OnmsServiceType serviceType, String reductionkey, Integer alarmtype, Integer counter, Integer severity, Date firsteventtime, String description, String logmsg, String operinstruct, String tticketid, TroubleTicketState tticketstate, String mouseovertext, Date suppresseduntil, String suppresseduser, Date suppressedtime, String alarmackuser, Date alarmacktime, String clearuei, String managedObjectInstance, String managedObjectType, org.opennms.netmgt.model.OnmsEvent event) {
        this.m_id = alarmid;
        this.m_uei = eventuei;
        this.m_distPoller = distPoller;
        this.m_node = node;
        this.m_ipAddr = ipaddr;
        this.m_serviceType = serviceType;
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
        this.m_managedObjectInstance = managedObjectInstance;
    }

    /**
     * default constructor
     */
    public OnmsAlarm() {
    }

    /**
     * minimal constructor
     *
     * @param alarmid a {@link java.lang.Integer} object.
     * @param eventuei a {@link java.lang.String} object.
     * @param distPoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
     * @param counter a {@link java.lang.Integer} object.
     * @param severity a {@link java.lang.Integer} object.
     * @param firsteventtime a {@link java.util.Date} object.
     * @param event a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
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
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @SequenceGenerator(name="alarmSequence", sequenceName="alarmsNxtId")
    @GeneratedValue(generator="alarmSequence")    
    @Column(name="alarmId")
    public Integer getId() {
        return this.m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param alarmid a {@link java.lang.Integer} object.
     */
    public void setId(Integer alarmid) {
        this.m_id = alarmid;
    }

    /**
     * <p>getUei</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="eventUEI", length=256, nullable=false)
    public String getUei() {
        return this.m_uei;
    }

    /**
     * <p>setUei</p>
     *
     * @param eventuei a {@link java.lang.String} object.
     */
    public void setUei(String eventuei) {
        this.m_uei = eventuei;
    }

    /**
     * <p>getDistPoller</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="dpName", nullable=false)
    public OnmsDistPoller getDistPoller() {
        return this.m_distPoller;
    }

    /**
     * <p>setDistPoller</p>
     *
     * @param distPoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
     */
    public void setDistPoller(OnmsDistPoller distPoller) {
        this.m_distPoller = distPoller;
    }

    // TODO change this to an Entity anre remove nodeid, ipaddr, serviceid
    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    public OnmsNode getNode() {
        return this.m_node;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        this.m_node = node;
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="ipaddr", length=16)
    public String getIpAddr() {
        return this.m_ipAddr;
    }

    /**
     * <p>setIpAddr</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     */
    public void setIpAddr(String ipaddr) {
        this.m_ipAddr = ipaddr;
    }

    /**
     * <p>getServiceType</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsServiceType} object.
     */
    @ManyToOne
    @JoinColumn(name="serviceid")
    public OnmsServiceType getServiceType() {
        return this.m_serviceType;
    }

    /**
     * <p>setServiceType</p>
     *
     * @param service a {@link org.opennms.netmgt.model.OnmsServiceType} object.
     */
    public void setServiceType(OnmsServiceType service) {
        this.m_serviceType = service;
    }

    /**
     * <p>getReductionKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="reductionKey", unique=true, length=256)
    public String getReductionKey() {
        return this.m_reductionKey;
    }

    /**
     * <p>setReductionKey</p>
     *
     * @param reductionkey a {@link java.lang.String} object.
     */
    public void setReductionKey(String reductionkey) {
        this.m_reductionKey = reductionkey;
    }

    /**
     * <p>getAlarmType</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name="alarmType")
    public Integer getAlarmType() {
        return this.m_alarmType;
    }

    /**
     * <p>setAlarmType</p>
     *
     * @param alarmtype a {@link java.lang.Integer} object.
     */
    public void setAlarmType(Integer alarmtype) {
        this.m_alarmType = alarmtype;
    }

    /**
     * <p>getCounter</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name="counter", nullable=false)
    public Integer getCounter() {
        return this.m_counter;
    }

    /**
     * <p>setCounter</p>
     *
     * @param counter a {@link java.lang.Integer} object.
     */
    public void setCounter(Integer counter) {
        this.m_counter = counter;
    }

    /**
     * <p>getSeverity</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name="severity", nullable=false)
    public Integer getSeverity() {
        return this.m_severity;
    }

    /**
     * <p>setSeverity</p>
     *
     * @param severity a {@link java.lang.Integer} object.
     */
    public void setSeverity(Integer severity) {
        this.m_severity = severity;
    }

    /**
     * <p>getFirstEventTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="firstEventTime")
    public Date getFirstEventTime() {
        return this.m_firstEventTime;
    }

    /**
     * <p>setFirstEventTime</p>
     *
     * @param firsteventtime a {@link java.util.Date} object.
     */
    public void setFirstEventTime(Date firsteventtime) {
        this.m_firstEventTime = firsteventtime;
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="description", length=4000)
    public String getDescription() {
        return this.m_description;
    }

    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        this.m_description = description;
    }

    /**
     * <p>getLogMsg</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="logmsg", length=256)
    public String getLogMsg() {
        return this.m_logMsg;
    }

    /**
     * <p>setLogMsg</p>
     *
     * @param logmsg a {@link java.lang.String} object.
     */
    public void setLogMsg(String logmsg) {
        this.m_logMsg = logmsg;
    }

    /**
     * <p>getOperInstruct</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="operinstruct", length=1024)
    public String getOperInstruct() {
        return this.m_operInstruct;
    }

    /**
     * <p>setOperInstruct</p>
     *
     * @param operinstruct a {@link java.lang.String} object.
     */
    public void setOperInstruct(String operinstruct) {
        this.m_operInstruct = operinstruct;
    }

    /**
     * <p>getTTicketId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="tticketId", length=128)
    public String getTTicketId() {
        return this.m_tTicketId;
    }

    /**
     * <p>setTTicketId</p>
     *
     * @param tticketid a {@link java.lang.String} object.
     */
    public void setTTicketId(String tticketid) {
        this.m_tTicketId = tticketid;
    }

    /**
     * <p>getTTicketState</p>
     *
     * @return a {@link org.opennms.netmgt.model.TroubleTicketState} object.
     */
    @Column(name="tticketState")
    public TroubleTicketState getTTicketState() {
        return this.m_tTicketState;
    }

    /**
     * <p>setTTicketState</p>
     *
     * @param tticketstate a {@link org.opennms.netmgt.model.TroubleTicketState} object.
     */
    public void setTTicketState(TroubleTicketState tticketstate) {
        this.m_tTicketState = tticketstate;
    }

    /**
     * <p>getMouseOverText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="mouseOverText", length=64)
    public String getMouseOverText() {
        return this.m_mouseOverText;
    }

    /**
     * <p>setMouseOverText</p>
     *
     * @param mouseovertext a {@link java.lang.String} object.
     */
    public void setMouseOverText(String mouseovertext) {
        this.m_mouseOverText = mouseovertext;
    }

    /**
     * <p>getSuppressedUntil</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="suppressedUntil")
    public Date getSuppressedUntil() {
        return this.m_suppressedUntil;
    }

    /**
     * <p>setSuppressedUntil</p>
     *
     * @param suppresseduntil a {@link java.util.Date} object.
     */
    public void setSuppressedUntil(Date suppresseduntil) {
        this.m_suppressedUntil = suppresseduntil;
    }

    /**
     * <p>getSuppressedUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="suppressedUser", length=256)
    public String getSuppressedUser() {
        return this.m_suppressedUser;
    }

    /**
     * <p>setSuppressedUser</p>
     *
     * @param suppresseduser a {@link java.lang.String} object.
     */
    public void setSuppressedUser(String suppresseduser) {
        this.m_suppressedUser = suppresseduser;
    }

    /**
     * <p>getSuppressedTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="suppressedTime")
    public Date getSuppressedTime() {
        return this.m_suppressedTime;
    }

    /**
     * <p>setSuppressedTime</p>
     *
     * @param suppressedtime a {@link java.util.Date} object.
     */
    public void setSuppressedTime(Date suppressedtime) {
        this.m_suppressedTime = suppressedtime;
    }

    /**
     * <p>getAlarmAckUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="alarmAckUser", length=256)
    public String getAlarmAckUser() {
        return this.m_alarmAckUser;
    }

    /**
     * <p>setAlarmAckUser</p>
     *
     * @param alarmackuser a {@link java.lang.String} object.
     */
    public void setAlarmAckUser(String alarmackuser) {
        this.m_alarmAckUser = alarmackuser;
    }

    /**
     * <p>getAlarmAckTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="alarmAckTime")
    public Date getAlarmAckTime() {
        return this.m_alarmAckTime;
    }

    /**
     * <p>setAlarmAckTime</p>
     *
     * @param alarmacktime a {@link java.util.Date} object.
     */
    public void setAlarmAckTime(Date alarmacktime) {
        this.m_alarmAckTime = alarmacktime;
    }

    /**
     * <p>getClearUei</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="clearUEI", length=256)
    public String getClearUei() {
        return this.m_clearUei;
    }

    /**
     * <p>setClearUei</p>
     *
     * @param clearuei a {@link java.lang.String} object.
     */
    public void setClearUei(String clearuei) {
        this.m_clearUei = clearuei;
    }

    /**
     * <p>getClearKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="clearKey", length=256)
    public String getClearKey() {
        return this.m_clearKey;
    }

    /**
     * <p>setClearKey</p>
     *
     * @param clearKey a {@link java.lang.String} object.
     */
    public void setClearKey(String clearKey) {
        this.m_clearKey = clearKey;
    }

    /**
     * <p>getLastEvent</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    @ManyToOne(fetch=FetchType.LAZY, optional=true)
    @JoinColumn(name="lastEventId")
    public OnmsEvent getLastEvent() {
        return this.m_lastEvent;
    }

    /**
     * <p>setLastEvent</p>
     *
     * @param event a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    public void setLastEvent(OnmsEvent event) {
        this.m_lastEvent = event;
        if (event!=null) this.m_lastEventTime = event.getEventTime(); // alarm can be saved with no associated event
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringCreator(this)
            .append("alarmid", getId())
            .toString();
    }

    /**
     * <p>getLastEventTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastEventTime")
    public Date getLastEventTime() {
        return m_lastEventTime;
    }

    /**
     * <p>setLastEventTime</p>
     *
     * @param lastEventTime a {@link java.util.Date} object.
     */
    public void setLastEventTime(Date lastEventTime) {
        m_lastEventTime = lastEventTime;
    }
    

    /**
     * <p>getApplicationDN</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="applicationDN", length=512)
    public String getApplicationDN() {
        return m_applicationDN;
    }

    /**
     * <p>setApplicationDN</p>
     *
     * @param applicationDN a {@link java.lang.String} object.
     */
    public void setApplicationDN(String applicationDN) {
        m_applicationDN = applicationDN;
    }

    /**
     * <p>getManagedObjectInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="managedObjectInstance", length=512)
    public String getManagedObjectInstance() {
        return m_managedObjectInstance;
    }

    /**
     * <p>setManagedObjectInstance</p>
     *
     * @param managedObjectInstance a {@link java.lang.String} object.
     */
    public void setManagedObjectInstance(String managedObjectInstance) {
        m_managedObjectInstance = managedObjectInstance;
    }

    /**
     * <p>getManagedObjectType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="managedObjectType", length=512)
    public String getManagedObjectType() {
        return m_managedObjectType;
    }

    /**
     * <p>setManagedObjectType</p>
     *
     * @param managedObjectType a {@link java.lang.String} object.
     */
    public void setManagedObjectType(String managedObjectType) {
        m_managedObjectType = managedObjectType;
    }

    /**
     * <p>getOssPrimaryKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="ossPrimaryKey", length=512)
    public String getOssPrimaryKey() {
        return m_ossPrimaryKey;
    }
    
    /**
     * <p>setOssPrimaryKey</p>
     *
     * @param key a {@link java.lang.String} object.
     */
    public void setOssPrimaryKey(String key) {
        m_ossPrimaryKey = key;
    }
    
    /**
     * <p>getX733AlarmType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="x733AlarmType", length=31)
    public String getX733AlarmType() {
        return m_x733AlarmType;
    }
    
    /**
     * <p>setX733AlarmType</p>
     *
     * @param alarmType a {@link java.lang.String} object.
     */
    public void setX733AlarmType(String alarmType) {
        m_x733AlarmType = alarmType;
    }
    
    /**
     * <p>getX733ProbableCause</p>
     *
     * @return a int.
     */
    @Column(name="x733ProbableCause", nullable=false)
    public int getX733ProbableCause() {
        return m_x733ProbableCause;
    }
    
    /**
     * <p>setX733ProbableCause</p>
     *
     * @param cause a int.
     */
    public void setX733ProbableCause(int cause) {
        m_x733ProbableCause = cause;
    }
    
    /**
     * <p>getQosAlarmState</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="qosAlarmState", length=31)
    public String getQosAlarmState() {
        return m_qosAlarmState;
        
    }
    /**
     * <p>setQosAlarmState</p>
     *
     * @param alarmState a {@link java.lang.String} object.
     */
    public void setQosAlarmState(String alarmState) {
        m_qosAlarmState = alarmState;
    }

    /**
     * <p>getFirstAutomationTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="firstAutomationTime")
    public Date getFirstAutomationTime() {
        return m_firstAutomationTime;
    }

    /**
     * <p>setFirstAutomationTime</p>
     *
     * @param firstAutomationTime a {@link java.util.Date} object.
     */
    public void setFirstAutomationTime(Date firstAutomationTime) {
        m_firstAutomationTime = firstAutomationTime;
    }

    /**
     * <p>getLastAutomationTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastAutomationTime")
    public Date getLastAutomationTime() {
        return m_lastAutomationTime;
    }

    /**
     * <p>setLastAutomationTime</p>
     *
     * @param lastAutomationTime a {@link java.util.Date} object.
     */
    public void setLastAutomationTime(Date lastAutomationTime) {
        m_lastAutomationTime = lastAutomationTime;
    }
    
    /**
     * <p>getDetails</p>
     *
     * @return a {@link java.util.Map} object.
     */
    @CollectionOfElements
    @JoinTable(name="alarm_details", joinColumns = @JoinColumn(name="alarmId"))
    @MapKey(columns=@Column(name="attribute"))
    @Column(name="attributeValue", nullable=false)
    public Map<String, String> getDetails() {
        return m_details;
    }
    
    /**
     * <p>setDetails</p>
     *
     * @param alarmDetails a {@link java.util.Map} object.
     */
    public void setDetails(Map<String, String> alarmDetails) {
        m_details = alarmDetails;
    }



}
