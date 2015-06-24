/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.net.InetAddress;
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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.core.network.InetAddressXmlAdapter;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>OnmsEvent class.</p>
 */
@XmlRootElement(name="event")
@Entity
@Table(name="events")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
	private InetAddress m_ipAddr;

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

	/**
	 * full constructor
	 *
	 * @param eventid a {@link java.lang.Integer} object.
	 * @param eventuei a {@link java.lang.String} object.
	 * @param eventtime a {@link java.util.Date} object.
	 * @param eventhost a {@link java.lang.String} object.
	 * @param eventsource a {@link java.lang.String} object.
	 * @param ipaddr a {@link java.lang.String} object.
	 * @param distPoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
	 * @param eventsnmphost a {@link java.lang.String} object.
	 * @param service a {@link org.opennms.netmgt.model.OnmsServiceType} object.
	 * @param eventsnmp a {@link java.lang.String} object.
	 * @param eventparms a {@link java.lang.String} object.
	 * @param eventcreatetime a {@link java.util.Date} object.
	 * @param eventdescr a {@link java.lang.String} object.
	 * @param eventloggroup a {@link java.lang.String} object.
	 * @param eventlogmsg a {@link java.lang.String} object.
	 * @param eventseverity a {@link java.lang.Integer} object.
	 * @param eventpathoutage a {@link java.lang.String} object.
	 * @param eventcorrelation a {@link java.lang.String} object.
	 * @param eventsuppressedcount a {@link java.lang.Integer} object.
	 * @param eventoperinstruct a {@link java.lang.String} object.
	 * @param eventautoaction a {@link java.lang.String} object.
	 * @param eventoperaction a {@link java.lang.String} object.
	 * @param eventoperactionmenutext a {@link java.lang.String} object.
	 * @param eventnotification a {@link java.lang.String} object.
	 * @param eventtticket a {@link java.lang.String} object.
	 * @param eventtticketstate a {@link java.lang.Integer} object.
	 * @param eventforward a {@link java.lang.String} object.
	 * @param eventmouseovertext a {@link java.lang.String} object.
	 * @param eventlog a {@link java.lang.String} object.
	 * @param eventdisplay a {@link java.lang.String} object.
	 * @param eventackuser a {@link java.lang.String} object.
	 * @param eventacktime a {@link java.util.Date} object.
	 * @param alarm a {@link org.opennms.netmgt.model.OnmsAlarm} object.
	 * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
	 * @param notifications a {@link java.util.Set} object.
	 * @param outagesBySvcregainedeventid a {@link java.util.Set} object.
	 * @param outagesBySvclosteventid a {@link java.util.Set} object.
	 */
	public OnmsEvent(Integer eventid, String eventuei, Date eventtime,
			String eventhost, String eventsource, InetAddress ipaddr,
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

	/**
	 * default constructor
	 */
	public OnmsEvent() {
	}

	/**
	 * minimal constructor
	 *
	 * @param eventid a {@link java.lang.Integer} object.
	 * @param eventuei a {@link java.lang.String} object.
	 * @param eventtime a {@link java.util.Date} object.
	 * @param eventsource a {@link java.lang.String} object.
	 * @param distPoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
	 * @param eventcreatetime a {@link java.util.Date} object.
	 * @param eventseverity a {@link java.lang.Integer} object.
	 * @param eventlog a {@link java.lang.String} object.
	 * @param eventdisplay a {@link java.lang.String} object.
	 * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
	 * @param notifications a {@link java.util.Set} object.
	 * @param outagesBySvcregainedeventid a {@link java.util.Set} object.
	 * @param outagesBySvclosteventid a {@link java.util.Set} object.
	 * @param alarms a {@link java.util.Set} object.
	 */
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

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @XmlAttribute(name="id")
    @Column(name="eventId", nullable=false)
    @SequenceGenerator(name="eventSequence", sequenceName="eventsNxtId")
    @GeneratedValue(generator="eventSequence")
	public Integer getId() {
		return m_eventId;
	}

	/**
	 * <p>setId</p>
	 *
	 * @param eventid a {@link java.lang.Integer} object.
	 */
	public void setId(Integer eventid) {
		m_eventId = eventid;
	}

	/**
	 * <p>getEventUei</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="uei")
	@Column(name="eventUei", length=256, nullable=false)
	public String getEventUei() {
		return m_eventUei;
	}

	/**
	 * <p>setEventUei</p>
	 *
	 * @param eventuei a {@link java.lang.String} object.
	 */
	public void setEventUei(String eventuei) {
		m_eventUei = eventuei;
	}

	/**
	 * <p>getEventTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@XmlElement(name="time")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eventTime", nullable=false)
	public Date getEventTime() {
		return m_eventTime;
	}

	/**
	 * <p>setEventTime</p>
	 *
	 * @param eventtime a {@link java.util.Date} object.
	 */
	public void setEventTime(Date eventtime) {
		m_eventTime = eventtime;
	}

	/**
	 * <p>getEventHost</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="host")
	@Column(name="eventHost", length=256)
	public String getEventHost() {
		return m_eventHost;
	}

	/**
	 * <p>setEventHost</p>
	 *
	 * @param eventhost a {@link java.lang.String} object.
	 */
	public void setEventHost(String eventhost) {
		m_eventHost = eventhost;
	}

	/**
	 * <p>getEventSource</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="source")
	@Column(name="eventSource", length=128, nullable=false)
	public String getEventSource() {
		return m_eventSource;
	}

	/**
	 * <p>setEventSource</p>
	 *
	 * @param eventsource a {@link java.lang.String} object.
	 */
	public void setEventSource(String eventsource) {
		m_eventSource = eventsource;
	}

	/**
	 * <p>getIpAddr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Column(name="ipAddr")
	@XmlElement(name="ipAddress")
	@Type(type="org.opennms.netmgt.model.InetAddressUserType")
	@XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
	public InetAddress getIpAddr() {
		return m_ipAddr;
	}

	/**
	 * <p>setIpAddr</p>
	 *
	 * @param ipaddr a {@link java.lang.String} object.
	 */
	public void setIpAddr(InetAddress ipaddr) {
		m_ipAddr = ipaddr;
	}

	/**
	 * <p>getDistPoller</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
	 */
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="eventDpName", nullable=false)
	public OnmsDistPoller getDistPoller() {
		return m_distPoller;
	}

	/**
	 * <p>setDistPoller</p>
	 *
	 * @param distPoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
	 */
	public void setDistPoller(OnmsDistPoller distPoller) {
		m_distPoller = distPoller;
	}

	/**
	 * <p>getEventSnmpHost</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="snmpHost")
	@Column(name="eventSnmpHost", length=256)
	public String getEventSnmpHost() {
		return m_eventSnmpHost;
	}

	/**
	 * <p>setEventSnmpHost</p>
	 *
	 * @param eventsnmphost a {@link java.lang.String} object.
	 */
	public void setEventSnmpHost(String eventsnmphost) {
		m_eventSnmpHost = eventsnmphost;
	}

	/**
	 * <p>getServiceType</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsServiceType} object.
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="serviceId", nullable=true)
	public OnmsServiceType getServiceType() {
		return m_serviceType;
	}

	/**
	 * <p>setServiceType</p>
	 *
	 * @param serviceType a {@link org.opennms.netmgt.model.OnmsServiceType} object.
	 */
	public void setServiceType(OnmsServiceType serviceType) {
		m_serviceType = serviceType;
	}

	/**
	 * <p>getEventSnmp</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="snmp")
	@Column(name="eventSnmp", length=256)
	public String getEventSnmp() {
		return m_eventSnmp;
	}

	/**
	 * <p>setEventSnmp</p>
	 *
	 * @param eventsnmp a {@link java.lang.String} object.
	 */
	public void setEventSnmp(String eventsnmp) {
		m_eventSnmp = eventsnmp;
	}

	/**
	 * <p>getEventParms</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="parms")
	@Column(name="eventParms", length=1024)
	public String getEventParms() {
		return m_eventParms;
	}

	/**
	 * <p>setEventParms</p>
	 *
	 * @param eventparms a {@link java.lang.String} object.
	 */
	public void setEventParms(String eventparms) {
		m_eventParms = eventparms;
	}

	/**
	 * <p>getEventCreateTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@XmlElement(name="createTime")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eventCreateTime", nullable=false)
	public Date getEventCreateTime() {
		return m_eventCreateTime;
	}

	/**
	 * <p>setEventCreateTime</p>
	 *
	 * @param eventcreatetime a {@link java.util.Date} object.
	 */
	public void setEventCreateTime(Date eventcreatetime) {
		m_eventCreateTime = eventcreatetime;
	}

	/**
	 * <p>getEventDescr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="description")
	@Column(name="eventDescr", length=4000)
	public String getEventDescr() {
		return m_eventDescr;
	}

	/**
	 * <p>setEventDescr</p>
	 *
	 * @param eventdescr a {@link java.lang.String} object.
	 */
	public void setEventDescr(String eventdescr) {
		m_eventDescr = eventdescr;
	}

	/**
	 * <p>getEventLogGroup</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="logGroup")
	@Column(name="eventLogGroup", length=32)
	public String getEventLogGroup() {
		return m_eventLogGroup;
	}

	/**
	 * <p>setEventLogGroup</p>
	 *
	 * @param eventloggroup a {@link java.lang.String} object.
	 */
	public void setEventLogGroup(String eventloggroup) {
		m_eventLogGroup = eventloggroup;
	}

	/**
	 * <p>getEventLogMsg</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="logMessage")
	@Column(name="eventLogMsg", length=1024)
	public String getEventLogMsg() {
		return m_eventLogMsg;
	}

	/**
	 * <p>setEventLogMsg</p>
	 *
	 * @param eventlogmsg a {@link java.lang.String} object.
	 */
	public void setEventLogMsg(String eventlogmsg) {
		m_eventLogMsg = eventlogmsg;
	}

	/**
	 * <p>getEventSeverity</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@XmlTransient
	@Column(name="eventSeverity", nullable=false)
	public Integer getEventSeverity() {
		return m_eventSeverity;
	}

	/**
	 * <p>setEventSeverity</p>
	 *
	 * @param severity a {@link java.lang.Integer} object.
	 */
	public void setEventSeverity(Integer severity) {
		m_eventSeverity = severity;
	}

    /**
     * <p>getSeverityLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    @XmlAttribute(name="severity")
    public String getSeverityLabel() {
        return OnmsSeverity.get(m_eventSeverity).name();
    }

    /**
     * <p>setSeverityLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setSeverityLabel(String label) {
        m_eventSeverity = OnmsSeverity.get(label).getId();
    }
    

	/**
	 * <p>getEventPathOutage</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="pathOutage")
	@Column(name="eventPathOutage", length=1024)
	public String getEventPathOutage() {
		return m_eventPathOutage;
	}

	/**
	 * <p>setEventPathOutage</p>
	 *
	 * @param eventpathoutage a {@link java.lang.String} object.
	 */
	public void setEventPathOutage(String eventpathoutage) {
		m_eventPathOutage = eventpathoutage;
	}

	/**
	 * <p>getEventCorrelation</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="correlation")
	@Column(name="eventCorrelation", length=1024)
	public String getEventCorrelation() {
		return m_eventCorrelation;
	}

	/**
	 * <p>setEventCorrelation</p>
	 *
	 * @param eventcorrelation a {@link java.lang.String} object.
	 */
	public void setEventCorrelation(String eventcorrelation) {
		m_eventCorrelation = eventcorrelation;
	}

	/**
	 * <p>getEventSuppressedCount</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@XmlElement(name="suppressedCount")
	@Column(name="eventSuppressedCount")
	public Integer getEventSuppressedCount() {
		return m_eventSuppressedCount;
	}

	/**
	 * <p>setEventSuppressedCount</p>
	 *
	 * @param eventsuppressedcount a {@link java.lang.Integer} object.
	 */
	public void setEventSuppressedCount(Integer eventsuppressedcount) {
		m_eventSuppressedCount = eventsuppressedcount;
	}

	/**
	 * <p>getEventOperInstruct</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="operatorInstructions")
	@Column(name="eventOperInstruct")
	public String getEventOperInstruct() {
		return m_eventOperInstruct;
	}

	/**
	 * <p>setEventOperInstruct</p>
	 *
	 * @param eventoperinstruct a {@link java.lang.String} object.
	 */
	public void setEventOperInstruct(String eventoperinstruct) {
		m_eventOperInstruct = eventoperinstruct;
	}

	/**
	 * <p>getEventAutoAction</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="autoAction")
	@Column(name="eventAutoAction", length=256)
	public String getEventAutoAction() {
		return m_eventAutoAction;
	}

	/**
	 * <p>setEventAutoAction</p>
	 *
	 * @param eventautoaction a {@link java.lang.String} object.
	 */
	public void setEventAutoAction(String eventautoaction) {
		m_eventAutoAction = eventautoaction;
	}

	/**
	 * <p>getEventOperAction</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="operatorAction")
	@Column(name="eventOperAction", length=256)
	public String getEventOperAction() {
		return m_eventOperAction;
	}

	/**
	 * <p>setEventOperAction</p>
	 *
	 * @param eventoperaction a {@link java.lang.String} object.
	 */
	public void setEventOperAction(String eventoperaction) {
		m_eventOperAction = eventoperaction;
	}

	/**
	 * <p>getEventOperActionMenuText</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="operationActionMenuText")
	@Column(name="eventOperActionMenuText", length=64)
	public String getEventOperActionMenuText() {
		return m_eventOperActionMenuText;
	}

	/**
	 * <p>setEventOperActionMenuText</p>
	 *
	 * @param eventOperActionMenuText a {@link java.lang.String} object.
	 */
	public void setEventOperActionMenuText(String eventOperActionMenuText) {
		m_eventOperActionMenuText = eventOperActionMenuText;
	}

	/**
	 * <p>getEventNotification</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="notification")
	@Column(name="eventNotification", length=128)
	public String getEventNotification() {
		return m_eventNotification;
	}

	/**
	 * <p>setEventNotification</p>
	 *
	 * @param eventnotification a {@link java.lang.String} object.
	 */
	public void setEventNotification(String eventnotification) {
		m_eventNotification = eventnotification;
	}

	/**
	 * <p>getEventTTicket</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="troubleTicket")
	@Column(name="eventTTicket", length=128)
	public String getEventTTicket() {
		return m_eventTTicket;
	}

	/**
	 * <p>setEventTTicket</p>
	 *
	 * @param eventtticket a {@link java.lang.String} object.
	 */
	public void setEventTTicket(String eventtticket) {
		m_eventTTicket = eventtticket;
	}

	/**
	 * <p>getEventTTicketState</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@XmlElement(name="troubleTicketState")
	@Column(name="eventTTicketState")
	public Integer getEventTTicketState() {
		return m_eventTTicketState;
	}

	/**
	 * <p>setEventTTicketState</p>
	 *
	 * @param eventtticketstate a {@link java.lang.Integer} object.
	 */
	public void setEventTTicketState(Integer eventtticketstate) {
		m_eventTTicketState = eventtticketstate;
	}

	/**
	 * <p>getEventForward</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlTransient
	@Column(name="eventForward", length=256)
	public String getEventForward() {
		return m_eventForward;
	}

	/**
	 * <p>setEventForward</p>
	 *
	 * @param eventforward a {@link java.lang.String} object.
	 */
	public void setEventForward(String eventforward) {
		m_eventForward = eventforward;
	}

	/**
	 * <p>getEventMouseOverText</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="mouseOverText")
	@Column(name="eventMouseOverText", length=64)
	public String getEventMouseOverText() {
		return m_eventMouseOverText;
	}

	/**
	 * <p>setEventMouseOverText</p>
	 *
	 * @param eventmouseovertext a {@link java.lang.String} object.
	 */
	public void setEventMouseOverText(String eventmouseovertext) {
		m_eventMouseOverText = eventmouseovertext;
	}

	/**
	 * TODO: Make this an Enum
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="log")
	@Column(name="eventLog", length=1, nullable=false)
	public String getEventLog() {
		return m_eventLog;
	}

	/**
	 * <p>setEventLog</p>
	 *
	 * @param eventlog a {@link java.lang.String} object.
	 */
	public void setEventLog(String eventlog) {
		m_eventLog = eventlog;
	}

	/**
	 * TODO: make this an Enum
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="display")
	@Column(name="eventDisplay", length=1, nullable=false)
	public String getEventDisplay() {
		return m_eventDisplay;
	}

	/**
	 * <p>setEventDisplay</p>
	 *
	 * @param eventdisplay a {@link java.lang.String} object.
	 */
	public void setEventDisplay(String eventdisplay) {
		m_eventDisplay = eventdisplay;
	}

	/**
	 * <p>getEventAckUser</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="ackUser")
	@Column(name="eventAckUser", length=256)
	public String getEventAckUser() {
		return m_eventAckUser;
	}

	/**
	 * <p>setEventAckUser</p>
	 *
	 * @param eventackuser a {@link java.lang.String} object.
	 */
	public void setEventAckUser(String eventackuser) {
		m_eventAckUser = eventackuser;
	}
	
	/**
	 * <p>getEventAckTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@XmlElement(name="ackTime")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eventAckTime")
	public Date getEventAckTime() {
		return m_eventAckTime;
	}

	/**
	 * <p>setEventAckTime</p>
	 *
	 * @param eventacktime a {@link java.util.Date} object.
	 */
	public void setEventAckTime(Date eventacktime) {
		m_eventAckTime = eventacktime;
	}

	/**
	 * <p>getAlarm</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsAlarm} object.
	 */
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="alarmId")
	public OnmsAlarm getAlarm() {
		return m_alarm;
	}

	/**
	 * <p>setAlarm</p>
	 *
	 * @param alarm a {@link org.opennms.netmgt.model.OnmsAlarm} object.
	 */
	public void setAlarm(OnmsAlarm alarm) {
		m_alarm = alarm;
	}

	/**
	 * <p>getNode</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
	 */
	@XmlTransient
	@JsonIgnore
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="nodeId")
	public OnmsNode getNode() {
		return m_node;
	}

    @Transient
    @XmlElement(name="nodeId")
    public Integer getNodeId() {
        try {
            return m_node != null ? m_node.getId() : null;
        } catch (ObjectNotFoundException e) {
            return null;
        }
    }


    @Transient
    @XmlElement(name="nodeLabel", required=false)
    public String getNodeLabel() {
        try{
            if (m_node == null) return null;
            return m_node.getLabel();
        } catch (ObjectNotFoundException e){
            return "";
        }

    }

	/**
	 * <p>setNode</p>
	 *
	 * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
	 */
	public void setNode(OnmsNode node) {
		m_node = node;
	}

	/**
	 * <p>getNotifications</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	@XmlTransient
	@OneToMany(mappedBy="event", fetch=FetchType.LAZY)
	public Set<OnmsNotification> getNotifications() {
		return m_notifications;
	}

	/**
	 * <p>setNotifications</p>
	 *
	 * @param notifications a {@link java.util.Set} object.
	 */
	public void setNotifications(Set<OnmsNotification> notifications) {
		m_notifications = notifications;
	}

	/**
	 * <p>getAssociatedServiceRegainedOutages</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	@XmlTransient
	@OneToMany(mappedBy="serviceRegainedEvent", fetch=FetchType.LAZY)
	public Set<OnmsOutage> getAssociatedServiceRegainedOutages() {
		return m_associatedServiceRegainedOutages;
	}

	/**
	 * <p>setAssociatedServiceRegainedOutages</p>
	 *
	 * @param outagesBySvcregainedeventid a {@link java.util.Set} object.
	 */
	public void setAssociatedServiceRegainedOutages(Set<OnmsOutage> outagesBySvcregainedeventid) {
		m_associatedServiceRegainedOutages = outagesBySvcregainedeventid;
	}

	/**
	 * <p>getAssociatedServiceLostOutages</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	@XmlTransient
	@OneToMany(mappedBy="serviceLostEvent", fetch=FetchType.LAZY)
	public Set<OnmsOutage> getAssociatedServiceLostOutages() {
		return m_associatedServiceLostOutages;
	}

	/**
	 * <p>setAssociatedServiceLostOutages</p>
	 *
	 * @param outagesBySvclosteventid a {@link java.util.Set} object.
	 */
	public void setAssociatedServiceLostOutages(Set<OnmsOutage> outagesBySvclosteventid) {
		m_associatedServiceLostOutages = outagesBySvclosteventid;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return new ToStringCreator(this).append("eventid", getId())
		        .append("eventuei", getEventUei())
				.toString();
	}

	/** {@inheritDoc} */
        @Override
	public void visit(EntityVisitor visitor) {
		throw new RuntimeException("visitor method not implemented");
	}

	   /**
	    * <p>getIfIndex</p>
	    *
	    * @return a {@link java.lang.Integer} object.
	    */
	   @Column(name="ifIndex")
	    public Integer getIfIndex() {
	        return m_ifIndex;
	    }

	    /**
	     * <p>setIfIndex</p>
	     *
	     * @param ifIndex a {@link java.lang.Integer} object.
	     */
	    public void setIfIndex(Integer ifIndex) {
	        m_ifIndex = ifIndex;
	    }

}
