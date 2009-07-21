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
// 2007 Apr 05: Set default Set objects so that they won't be null before we commit this to the DB. - dj@opennms.org
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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Filter;
import org.springframework.core.style.ToStringCreator;

@XmlRootElement
@Entity
@Table(name="events")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public class OnmsEvent extends OnmsEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7412025003474162992L;

	/** identifier field */
	private Integer m_eventId;

	/** persistent field */
	private String m_eventUei;

	/** persistent field */
	private Date m_eventTime;

	/** nullable persistent field */
	private String m_eventHost;

	/** persistent field */
	private String m_eventSource;

	/** nullable persistent field */
	private String m_ipAddr;

	/** persistent field */
	private OnmsDistPoller m_distPoller;

	/** nullable persistent field */
	private String m_eventSnmpHost;

	/** nullable persistent field */
	private OnmsServiceType m_serviceType;

	/** nullable persistent field */
	private String m_eventSnmp;

	/** nullable persistent field */
	private String m_eventParms;

	/** persistent field */
	private Date m_eventCreateTime;

	/** nullable persistent field */
	private String m_eventDescr;

	/** nullable persistent field */
	private String m_eventLogGroup;

	/** nullable persistent field */
	private String m_eventLogMsg;

	/** persistent field */
	private Integer m_eventSeverity;
	
	/** nullable persistent field */
    private Integer m_ifIndex;

	/** nullable persistent field */
	private String m_eventPathOutage;

	/** nullable persistent field */
	private String m_eventCorrelation;

	/** nullable persistent field */
	private Integer m_eventSuppressedCount;

	/** nullable persistent field */
	private String m_eventOperInstruct;

	/** nullable persistent field */
	private String m_eventAutoAction;

	/** nullable persistent field */
	private String m_eventOperAction;

	/** nullable persistent field */
	private String m_eventOperActionMenuText;

	/** nullable persistent field */
	private String m_eventNotification;

	/** nullable persistent field */
	private String m_eventTTicket;

	/** nullable persistent field */
	private Integer m_eventTTicketState;

	/** nullable persistent field */
	private String m_eventForward;

	/** nullable persistent field */
	private String m_eventMouseOverText;

	/** persistent field */
	private String m_eventLog;

	/** persistent field */
	private String m_eventDisplay;

	/** nullable persistent field */
	private String m_eventAckUser;

	/** nullable persistent field */
	private Date m_eventAckTime;

	/** nullable persistent field */
	private OnmsAlarm m_alarm;

	/** persistent field */
	private org.opennms.netmgt.model.OnmsNode m_node;

	/** persistent field */
	private Set<OnmsNotification> m_notifications = new HashSet<OnmsNotification>();

	/** persistent field */
	private Set<OnmsOutage> m_associatedServiceRegainedOutages = new HashSet<OnmsOutage>();

	/** persistent field */
	private Set<OnmsOutage> m_associatedServiceLostOutages = new HashSet<OnmsOutage>();

	/** full constructor */
	public OnmsEvent(Integer eventid, String eventuei, Date eventtime,
			String eventhost, String eventsource, String ipaddr,
			OnmsDistPoller distPoller, String eventsnmphost, OnmsServiceType service,
			String eventsnmp, String eventparms, Date eventcreatetime,
			String eventdescr, String eventloggroup, String eventlogmsg,
			Integer eventseverity, String eventpathoutage, String eventcorrelation,
			Integer eventsuppressedcount, String eventoperinstruct,
			String eventautoaction, String eventoperaction,
			String eventoperactionmenutext, String eventnotification,
			String eventtticket, Integer eventtticketstate,
			String eventforward, String eventmouseovertext, String eventlog,
			String eventdisplay, String eventackuser, Date eventacktime,
			OnmsAlarm alarm, org.opennms.netmgt.model.OnmsNode node,
			Set<OnmsNotification> notifications, Set<OnmsOutage> outagesBySvcregainedeventid,
			Set<OnmsOutage> outagesBySvclosteventid) {
		m_eventId = eventid;
		m_eventUei = eventuei;
		m_eventTime = eventtime;
		m_eventHost = eventhost;
		m_eventSource = eventsource;
		m_ipAddr = ipaddr;
		m_distPoller = distPoller;
		m_eventSnmpHost = eventsnmphost;
		m_serviceType = service;
		m_eventSnmp = eventsnmp;
		m_eventParms = eventparms;
		m_eventCreateTime = eventcreatetime;
		m_eventDescr = eventdescr;
		m_eventLogGroup = eventloggroup;
		m_eventLogMsg = eventlogmsg;
		m_eventSeverity = eventseverity;
		m_eventPathOutage = eventpathoutage;
		m_eventCorrelation = eventcorrelation;
		m_eventSuppressedCount = eventsuppressedcount;
		m_eventOperInstruct = eventoperinstruct;
		m_eventAutoAction = eventautoaction;
		m_eventOperAction = eventoperaction;
		m_eventOperActionMenuText = eventoperactionmenutext;
		m_eventNotification = eventnotification;
		m_eventTTicket = eventtticket;
		m_eventTTicketState = eventtticketstate;
		m_eventForward = eventforward;
		m_eventMouseOverText = eventmouseovertext;
		m_eventLog = eventlog;
		m_eventDisplay = eventdisplay;
		m_eventAckUser = eventackuser;
		m_eventAckTime = eventacktime;
		m_alarm = alarm;
		m_node = node;
		m_notifications = notifications;
		m_associatedServiceRegainedOutages = outagesBySvcregainedeventid;
		m_associatedServiceLostOutages = outagesBySvclosteventid;
	}

	/** default constructor */
	public OnmsEvent() {
	}

	/** minimal constructor */
	public OnmsEvent(Integer eventid, String eventuei, Date eventtime,
			String eventsource, OnmsDistPoller distPoller, Date eventcreatetime,
			Integer eventseverity, String eventlog, String eventdisplay,
			org.opennms.netmgt.model.OnmsNode node, Set<OnmsNotification> notifications,
			Set<OnmsOutage> outagesBySvcregainedeventid, Set<OnmsOutage> outagesBySvclosteventid,
			Set<OnmsAlarm> alarms) {
		m_eventId = eventid;
		m_eventUei = eventuei;
		m_eventTime = eventtime;
		m_eventSource = eventsource;
		m_distPoller = distPoller;
		m_eventCreateTime = eventcreatetime;
		m_eventSeverity = eventseverity;
		m_eventLog = eventlog;
		m_eventDisplay = eventdisplay;
		m_node = node;
		m_notifications = notifications;
		m_associatedServiceRegainedOutages = outagesBySvcregainedeventid;
		m_associatedServiceLostOutages = outagesBySvclosteventid;
	}

    @Id
    @XmlAttribute(name="id")
    @Column(name="eventId")
    @SequenceGenerator(name="eventSequence", sequenceName="eventsNxtId")
    @GeneratedValue(generator="eventSequence")    
	public Integer getId() {
		return m_eventId;
	}

	public void setId(Integer eventid) {
		m_eventId = eventid;
	}

	@Column(name="eventUei", length=256, nullable=false)
	public String getEventUei() {
		return m_eventUei;
	}

	public void setEventUei(String eventuei) {
		m_eventUei = eventuei;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eventTime", nullable=false)
	public Date getEventTime() {
		return m_eventTime;
	}

	public void setEventTime(Date eventtime) {
		m_eventTime = eventtime;
	}

	@Column(name="eventHost", length=256)
	public String getEventHost() {
		return m_eventHost;
	}

	public void setEventHost(String eventhost) {
		m_eventHost = eventhost;
	}

	@Column(name="eventSource", length=128, nullable=false)
	public String getEventSource() {
		return m_eventSource;
	}

	public void setEventSource(String eventsource) {
		m_eventSource = eventsource;
	}

	@Column(name="ipAddr", length=16)
	public String getIpAddr() {
		return m_ipAddr;
	}

	public void setIpAddr(String ipaddr) {
		m_ipAddr = ipaddr;
	}

	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="eventDpName", nullable=false)
	public OnmsDistPoller getDistPoller() {
		return m_distPoller;
	}

	public void setDistPoller(OnmsDistPoller distPoller) {
		m_distPoller = distPoller;
	}

	@Column(name="eventSnmpHost", length=256)
	public String getEventSnmpHost() {
		return m_eventSnmpHost;
	}

	public void setEventSnmpHost(String eventsnmphost) {
		m_eventSnmpHost = eventsnmphost;
	}

	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="serviceId", nullable=true)
	public OnmsServiceType getServiceType() {
		return m_serviceType;
	}

	public void setServiceType(OnmsServiceType serviceType) {
		m_serviceType = serviceType;
	}

	@Column(name="eventSnmp", length=256)
	public String getEventSnmp() {
		return m_eventSnmp;
	}

	public void setEventSnmp(String eventsnmp) {
		m_eventSnmp = eventsnmp;
	}

	@Column(name="eventParms", length=1024)
	public String getEventParms() {
		return m_eventParms;
	}

	public void setEventParms(String eventparms) {
		m_eventParms = eventparms;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eventCreateTime", nullable=false)
	public Date getEventCreateTime() {
		return m_eventCreateTime;
	}

	public void setEventCreateTime(Date eventcreatetime) {
		m_eventCreateTime = eventcreatetime;
	}

	@Column(name="eventDescr", length=4000)
	public String getEventDescr() {
		return m_eventDescr;
	}

	public void setEventDescr(String eventdescr) {
		m_eventDescr = eventdescr;
	}

	@Column(name="eventLogGroup", length=32)
	public String getEventLogGroup() {
		return m_eventLogGroup;
	}

	public void setEventLogGroup(String eventloggroup) {
		m_eventLogGroup = eventloggroup;
	}

	@Column(name="eventLogMsg", length=256)
	public String getEventLogMsg() {
		return m_eventLogMsg;
	}

	public void setEventLogMsg(String eventlogmsg) {
		m_eventLogMsg = eventlogmsg;
	}

	@Column(name="eventSeverity", nullable=false)
	public Integer getEventSeverity() {
		return m_eventSeverity;
	}

	public void setEventSeverity(Integer severity) {
		m_eventSeverity = severity;
	}

	@Column(name="eventPathOutage", length=1024)
	public String getEventPathOutage() {
		return m_eventPathOutage;
	}

	public void setEventPathOutage(String eventpathoutage) {
		m_eventPathOutage = eventpathoutage;
	}

	@Column(name="eventCorrelation", length=1024)
	public String getEventCorrelation() {
		return m_eventCorrelation;
	}

	public void setEventCorrelation(String eventcorrelation) {
		m_eventCorrelation = eventcorrelation;
	}

	@Column(name="eventSuppressedCount")
	public Integer getEventSuppressedCount() {
		return m_eventSuppressedCount;
	}

	public void setEventSuppressedCount(Integer eventsuppressedcount) {
		m_eventSuppressedCount = eventsuppressedcount;
	}

	@Column(name="eventOperInstruct", length=1024)
	public String getEventOperInstruct() {
		return m_eventOperInstruct;
	}

	public void setEventOperInstruct(String eventoperinstruct) {
		m_eventOperInstruct = eventoperinstruct;
	}

	@Column(name="eventAutoAction", length=256)
	public String getEventAutoAction() {
		return m_eventAutoAction;
	}

	public void setEventAutoAction(String eventautoaction) {
		m_eventAutoAction = eventautoaction;
	}

	@Column(name="eventOperAction", length=256)
	public String getEventOperAction() {
		return m_eventOperAction;
	}

	public void setEventOperAction(String eventoperaction) {
		m_eventOperAction = eventoperaction;
	}

	@Column(name="eventOperActionMenuText", length=64)
	public String getEventOperActionMenuText() {
		return m_eventOperActionMenuText;
	}

	public void setEventOperActionMenuText(String eventOperActionMenuText) {
		m_eventOperActionMenuText = eventOperActionMenuText;
	}

	@Column(name="eventNotification", length=128)
	public String getEventNotification() {
		return m_eventNotification;
	}

	public void setEventNotification(String eventnotification) {
		m_eventNotification = eventnotification;
	}

	@Column(name="eventTTicket", length=128)
	public String getEventTTicket() {
		return m_eventTTicket;
	}

	public void setEventTTicket(String eventtticket) {
		m_eventTTicket = eventtticket;
	}

	@Column(name="eventTTicketState")
	public Integer getEventTTicketState() {
		return m_eventTTicketState;
	}

	public void setEventTTicketState(Integer eventtticketstate) {
		m_eventTTicketState = eventtticketstate;
	}

	@Column(name="eventForward", length=256)
	public String getEventForward() {
		return m_eventForward;
	}

	public void setEventForward(String eventforward) {
		m_eventForward = eventforward;
	}

	@Column(name="eventMouseOverText", length=64)
	public String getEventMouseOverText() {
		return m_eventMouseOverText;
	}

	public void setEventMouseOverText(String eventmouseovertext) {
		m_eventMouseOverText = eventmouseovertext;
	}

	/**
	 * TODO: Make this an Enum
	 */
	@Column(name="eventLog", length=1, nullable=false)
	public String getEventLog() {
		return m_eventLog;
	}

	public void setEventLog(String eventlog) {
		m_eventLog = eventlog;
	}

	/**
	 * TODO: make this an Enum
	 * 
	 */
	@Column(name="eventDisplay", length=1, nullable=false)
	public String getEventDisplay() {
		return m_eventDisplay;
	}

	public void setEventDisplay(String eventdisplay) {
		m_eventDisplay = eventdisplay;
	}

	@Column(name="eventAckUser", length=256)
	public String getEventAckUser() {
		return m_eventAckUser;
	}

	public void setEventAckUser(String eventackuser) {
		m_eventAckUser = eventackuser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eventAckTime")
	public Date getEventAckTime() {
		return m_eventAckTime;
	}

	public void setEventAckTime(Date eventacktime) {
		m_eventAckTime = eventacktime;
	}

	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="alarmId")
	public OnmsAlarm getAlarm() {
		return m_alarm;
	}

	public void setAlarm(OnmsAlarm alarm) {
		m_alarm = alarm;
	}

	@XmlIDREF
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="nodeId")
	public OnmsNode getNode() {
		return m_node;
	}

	public void setNode(OnmsNode node) {
		m_node = node;
	}

	@XmlTransient
	@OneToMany(mappedBy="event", fetch=FetchType.LAZY)
	public Set<OnmsNotification> getNotifications() {
		return m_notifications;
	}

	public void setNotifications(Set<OnmsNotification> notifications) {
		m_notifications = notifications;
	}

	@XmlTransient
	@OneToMany(mappedBy="serviceRegainedEvent", fetch=FetchType.LAZY)
	public Set<OnmsOutage> getAssociatedServiceRegainedOutages() {
		return m_associatedServiceRegainedOutages;
	}

	public void setAssociatedServiceRegainedOutages(Set<OnmsOutage> outagesBySvcregainedeventid) {
		m_associatedServiceRegainedOutages = outagesBySvcregainedeventid;
	}

	@XmlTransient
	@OneToMany(mappedBy="serviceLostEvent", fetch=FetchType.LAZY)
	public Set<OnmsOutage> getAssociatedServiceLostOutages() {
		return m_associatedServiceLostOutages;
	}

	public void setAssociatedServiceLostOutages(Set<OnmsOutage> outagesBySvclosteventid) {
		m_associatedServiceLostOutages = outagesBySvclosteventid;
	}

	public String toString() {
		return new ToStringCreator(this).append("eventid", getId())
				.toString();
	}

	public void visit(EntityVisitor visitor) {
		throw new RuntimeException("visitor method not implemented");
	}

	   @Column(name="ifIndex")
	    public Integer getIfIndex() {
	        return m_ifIndex;
	    }

	    public void setIfIndex(Integer ifIndex) {
	        m_ifIndex = ifIndex;
	    }

}
