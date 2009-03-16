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
// 2008 Dec 21: Added eventParm column - david@opennms.org
// 2008 Oct 04: Use new OnmsSeverity object and remove package name from OnmsEvent. - dj@opennms.org
// 2008 Sep 26: Move some of the alarm "constant" information into here
// 2007 Apr 05: Make annotation nullability and default values match what's in create.sql. - dj@opennms.org
//
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
import java.util.Calendar;
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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;
import org.springframework.core.style.ToStringCreator;

@XmlRootElement
@Entity
@Table(name="alarms")
public class OnmsAlarm implements Acknowledgeable, Serializable {
    private static final long serialVersionUID = 7275548439687562161L;
    
    public static final int PROBLEM_TYPE = 1;
    
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

    /** nullable persistent field */
    private Integer m_ifIndex;

    /** persistent field */
    private Integer m_counter;

    /** persistent field */
    private OnmsSeverity m_severity;

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
    private OnmsEvent m_lastEvent;
    
    /** persistent field */
    private String m_eventParms;

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

    /** full constructor */
    public OnmsAlarm(Integer alarmid, String eventuei, OnmsDistPoller distPoller, OnmsNode node, String ipaddr, OnmsServiceType serviceType, String reductionkey, Integer alarmtype, Integer counter, Integer severity, Date firsteventtime, String description, String logmsg, String operinstruct, String tticketid, TroubleTicketState tticketstate, String mouseovertext, Date suppresseduntil, String suppresseduser, Date suppressedtime, String alarmackuser, Date alarmacktime, String clearuei, String managedObjectInstance, String managedObjectType, OnmsEvent event) {
        this.m_id = alarmid;
        this.m_uei = eventuei;
        this.m_distPoller = distPoller;
        this.m_node = node;
        this.m_ipAddr = ipaddr;
        this.m_serviceType = serviceType;
        this.m_reductionKey = reductionkey;
        this.m_alarmType = alarmtype;
        this.m_counter = counter;
        this.m_severity = OnmsSeverity.get(severity);
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

    /** default constructor */
    public OnmsAlarm() {
    }

    /** minimal constructor */
    public OnmsAlarm(Integer alarmid, String eventuei, OnmsDistPoller distPoller, Integer counter, Integer severity, Date firsteventtime, OnmsEvent event) {
        this.m_id = alarmid;
        this.m_uei = eventuei;
        this.m_distPoller = distPoller;
        this.m_counter = counter;
        this.m_severity = OnmsSeverity.get(severity);
        this.m_firstEventTime = firsteventtime;
        this.m_lastEvent = event;
    }

    @Id
    @SequenceGenerator(name="alarmSequence", sequenceName="alarmsNxtId")
    @GeneratedValue(generator="alarmSequence")    
    @Column(name="alarmId")
    public Integer getId() {
        return this.m_id;
    }

    public void setId(Integer alarmid) {
        this.m_id = alarmid;
    }

    @Column(name="eventUEI", length=256, nullable=false)
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
	@XmlTransient
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="dpName", nullable=false)
    public OnmsDistPoller getDistPoller() {
        return this.m_distPoller;
    }

    public void setDistPoller(OnmsDistPoller distPoller) {
        this.m_distPoller = distPoller;
    }

    // TODO change this to an Entity anre remove nodeid, ipaddr, serviceid
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    public OnmsNode getNode() {
        return this.m_node;
    }

    public void setNode(OnmsNode node) {
        this.m_node = node;
    }

    @Column(name="ipaddr", length=16)
    public String getIpAddr() {
        return this.m_ipAddr;
    }

    public void setIpAddr(String ipaddr) {
        this.m_ipAddr = ipaddr;
    }

    @XmlTransient
    @ManyToOne
    @JoinColumn(name="serviceid")
    public OnmsServiceType getServiceType() {
        return this.m_serviceType;
    }

    public void setServiceType(OnmsServiceType service) {
        this.m_serviceType = service;
    }

    @Column(name="reductionKey", unique=true, length=256)
    public String getReductionKey() {
        return this.m_reductionKey;
    }

    public void setReductionKey(String reductionkey) {
        this.m_reductionKey = reductionkey;
    }

    @Column(name="alarmType")
    public Integer getAlarmType() {
        return this.m_alarmType;
    }

    public void setAlarmType(Integer alarmtype) {
        this.m_alarmType = alarmtype;
    }

    @Column(name="counter", nullable=false)
    public Integer getCounter() {
        return this.m_counter;
    }

    public void setCounter(Integer counter) {
        this.m_counter = counter;
    }

    @Transient
    public OnmsSeverity getSeverity() {
        return this.m_severity;
    }

    @Transient
    public void setSeverity(OnmsSeverity severity) {
        m_severity = severity;
    }
    
    @Column(name="severity", nullable=false)
    public Integer getSeverityId() {
        return this.m_severity.getId();
    }

    public void setSeverityId(Integer severity) {
        this.m_severity = OnmsSeverity.get(severity);
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="firstEventTime")
    public Date getFirstEventTime() {
        return this.m_firstEventTime;
    }

    public void setFirstEventTime(Date firsteventtime) {
        this.m_firstEventTime = firsteventtime;
    }

    @Column(name="description", length=4000)
    public String getDescription() {
        return this.m_description;
    }

    public void setDescription(String description) {
        this.m_description = description;
    }

    @Column(name="logmsg", length=256)
    public String getLogMsg() {
        return this.m_logMsg;
    }

    public void setLogMsg(String logmsg) {
        this.m_logMsg = logmsg;
    }

    @Column(name="operinstruct", length=1024)
    public String getOperInstruct() {
        return this.m_operInstruct;
    }

    public void setOperInstruct(String operinstruct) {
        this.m_operInstruct = operinstruct;
    }

    @Column(name="tticketId", length=128)
    public String getTTicketId() {
        return this.m_tTicketId;
    }

    public void setTTicketId(String tticketid) {
        this.m_tTicketId = tticketid;
    }

    @Column(name="tticketState")
    public TroubleTicketState getTTicketState() {
        return this.m_tTicketState;
    }

    public void setTTicketState(TroubleTicketState tticketstate) {
        this.m_tTicketState = tticketstate;
    }

    @Column(name="mouseOverText", length=64)
    public String getMouseOverText() {
        return this.m_mouseOverText;
    }

    public void setMouseOverText(String mouseovertext) {
        this.m_mouseOverText = mouseovertext;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="suppressedUntil")
    public Date getSuppressedUntil() {
        return this.m_suppressedUntil;
    }

    public void setSuppressedUntil(Date suppresseduntil) {
        this.m_suppressedUntil = suppresseduntil;
    }

    @Column(name="suppressedUser", length=256)
    public String getSuppressedUser() {
        return this.m_suppressedUser;
    }

    public void setSuppressedUser(String suppresseduser) {
        this.m_suppressedUser = suppresseduser;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="suppressedTime")
    public Date getSuppressedTime() {
        return this.m_suppressedTime;
    }

    public void setSuppressedTime(Date suppressedtime) {
        this.m_suppressedTime = suppressedtime;
    }

    @Column(name="alarmAckUser", length=256)
    public String getAlarmAckUser() {
        return this.m_alarmAckUser;
    }

    public void setAlarmAckUser(String alarmackuser) {
        this.m_alarmAckUser = alarmackuser;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="alarmAckTime")
    public Date getAlarmAckTime() {
        return this.m_alarmAckTime;
    }

    public void setAlarmAckTime(Date alarmacktime) {
        this.m_alarmAckTime = alarmacktime;
    }

    @Column(name="clearUEI", length=256)
    public String getClearUei() {
        return this.m_clearUei;
    }

    public void setClearUei(String clearuei) {
        this.m_clearUei = clearuei;
    }

    @Column(name="clearKey", length=256)
    public String getClearKey() {
        return this.m_clearKey;
    }

    public void setClearKey(String clearKey) {
        this.m_clearKey = clearKey;
    }

    @XmlTransient
    @ManyToOne(fetch=FetchType.LAZY, optional=true)
    @JoinColumn(name="lastEventId")
    public OnmsEvent getLastEvent() {
        return this.m_lastEvent;
    }

    public void setLastEvent(OnmsEvent event) {
        this.m_lastEvent = event;
        if (event!=null) this.m_lastEventTime = event.getEventTime(); // alarm can be saved with no associated event
    }
    
    @Column(name="eventParms", length=1024)
    public String getEventParms() {
        return this.m_eventParms;
    }

    public void setEventParms(String eventparms) {
        this.m_eventParms = eventparms;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("alarmid", getId())
            .toString();
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastEventTime")
    public Date getLastEventTime() {
        return m_lastEventTime;
    }

    public void setLastEventTime(Date lastEventTime) {
        m_lastEventTime = lastEventTime;
    }
    

    @Column(name="applicationDN", length=512)
    public String getApplicationDN() {
        return m_applicationDN;
    }

    public void setApplicationDN(String applicationDN) {
        m_applicationDN = applicationDN;
    }

    @Column(name="managedObjectInstance", length=512)
    public String getManagedObjectInstance() {
        return m_managedObjectInstance;
    }

    public void setManagedObjectInstance(String managedObjectInstance) {
        m_managedObjectInstance = managedObjectInstance;
    }

    @Column(name="managedObjectType", length=512)
    public String getManagedObjectType() {
        return m_managedObjectType;
    }

    public void setManagedObjectType(String managedObjectType) {
        m_managedObjectType = managedObjectType;
    }

    @Column(name="ossPrimaryKey", length=512)
    public String getOssPrimaryKey() {
        return m_ossPrimaryKey;
    }
    
    public void setOssPrimaryKey(String key) {
        m_ossPrimaryKey = key;
    }
    
    @Column(name="x733AlarmType", length=31)
    public String getX733AlarmType() {
        return m_x733AlarmType;
    }
    
    public void setX733AlarmType(String alarmType) {
        m_x733AlarmType = alarmType;
    }
    
    @Column(name="x733ProbableCause", nullable=false)
    public int getX733ProbableCause() {
        return m_x733ProbableCause;
    }
    
    public void setX733ProbableCause(int cause) {
        m_x733ProbableCause = cause;
    }
    
    @Column(name="qosAlarmState", length=31)
    public String getQosAlarmState() {
        return m_qosAlarmState;
        
    }
    public void setQosAlarmState(String alarmState) {
        m_qosAlarmState = alarmState;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="firstAutomationTime")
    public Date getFirstAutomationTime() {
        return m_firstAutomationTime;
    }

    public void setFirstAutomationTime(Date firstAutomationTime) {
        m_firstAutomationTime = firstAutomationTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastAutomationTime")
    public Date getLastAutomationTime() {
        return m_lastAutomationTime;
    }

    public void setLastAutomationTime(Date lastAutomationTime) {
        m_lastAutomationTime = lastAutomationTime;
    }
    
    @XmlTransient
    @CollectionOfElements
    @JoinTable(name="alarm_details", joinColumns = @JoinColumn(name="alarmId"))
    @MapKey(columns=@Column(name="attribute"))
    @Column(name="attributeValue", nullable=false)
    public Map<String, String> getDetails() {
        return m_details;
    }
    
    public void setDetails(Map<String, String> alarmDetails) {
        m_details = alarmDetails;
    }

    @Column(name="ifIndex")
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    public void setIfIndex(Integer ifIndex) {
        m_ifIndex = ifIndex;
    }

    public void acknowledge(String user) {
        if (m_alarmAckTime == null || m_alarmAckUser == null) {
            m_alarmAckTime = Calendar.getInstance().getTime();
            m_alarmAckUser = user;
        }
    }
    
    public void unacknowledge(String ackUser) {
        m_alarmAckTime = null;
        m_alarmAckUser = null;
    }
    
    public void clear(String ackUser) {
        m_severity = OnmsSeverity.CLEARED;
    }
    
    public void escalate(String ackUser) {
        m_severity = OnmsSeverity.escalate(m_severity);
        m_alarmAckUser = ackUser;
        m_alarmAckTime = Calendar.getInstance().getTime();
    }

    @Transient
    public AckType getType() {
        return AckType.ALARM;
    }
    
    @Transient
    public Integer getAckId() {
        return m_id;
    }
    
    @Transient
    public String getAckUser() {
        return m_alarmAckUser;
    }
    
    @Transient
    public Date getAckTime() {
        return m_alarmAckTime;
    }
    
}
