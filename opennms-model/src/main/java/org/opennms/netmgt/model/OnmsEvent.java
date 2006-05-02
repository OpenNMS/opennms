package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.springframework.core.style.ToStringCreator;

/**
 * @hibernate.class table="events"
 * 
 */
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
	private Set m_notifications;

	/** persistent field */
	private Set m_outagesBySvcRegainedEventId;

	/** persistent field */
	private Set m_outagesBySvclostEventId;

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
			Set notifications, Set outagesBySvcregainedeventid,
			Set outagesBySvclosteventid) {
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
		m_outagesBySvcRegainedEventId = outagesBySvcregainedeventid;
		m_outagesBySvclostEventId = outagesBySvclosteventid;
	}

	/** default constructor */
	public OnmsEvent() {
	}

	/** minimal constructor */
	public OnmsEvent(Integer eventid, String eventuei, Date eventtime,
			String eventsource, OnmsDistPoller distPoller, Date eventcreatetime,
			Integer eventseverity, String eventlog, String eventdisplay,
			org.opennms.netmgt.model.OnmsNode node, Set notifications,
			Set outagesBySvcregainedeventid, Set outagesBySvclosteventid,
			Set alarms) {
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
		m_outagesBySvcRegainedEventId = outagesBySvcregainedeventid;
		m_outagesBySvclostEventId = outagesBySvclosteventid;
	}

	/**
	 * @hibernate.id generator-class="assigned" type="java.lang.Integer"
	 *               column="eventid"
	 * @hibernate.generator-param name="sequence" value="eventsNxtId"
	 */
	public Integer getId() {
		return m_eventId;
	}

	public void setId(Integer eventid) {
		m_eventId = eventid;
	}

	/**
	 * @hibernate.property column="eventuei" length="256" not-null="true"
	 * 
	 */
	public String getEventUei() {
		return m_eventUei;
	}

	public void setEventUei(String eventuei) {
		m_eventUei = eventuei;
	}

	/**
	 * @hibernate.property column="eventtime" length="8" not-null="true"
	 * 
	 */
	public Date getEventTime() {
		return m_eventTime;
	}

	public void setEventTime(Date eventtime) {
		m_eventTime = eventtime;
	}

	/**
	 * @hibernate.property column="eventhost" length="256"
	 * 
	 */
	public String getEventHost() {
		return m_eventHost;
	}

	public void setEventHost(String eventhost) {
		m_eventHost = eventhost;
	}

	/**
	 * @hibernate.property column="eventsource" length="128" not-null="true"
	 * 
	 */
	public String getEventSource() {
		return m_eventSource;
	}

	public void setEventSource(String eventsource) {
		m_eventSource = eventsource;
	}

	/**
	 * @hibernate.property column="ipaddr" length="16"
	 * 
	 */
	public String getIpAddr() {
		return m_ipAddr;
	}

	public void setIpAddr(String ipaddr) {
		m_ipAddr = ipaddr;
	}

	/**
	 * @hibernate.property column="eventdpname" length="12" not-null="true"
	 * 
	 */
	public OnmsDistPoller getDistPoller() {
		return m_distPoller;
	}

	public void setDistPoller(OnmsDistPoller distPoller) {
		m_distPoller = distPoller;
	}

	/**
	 * @hibernate.property column="eventsnmphost" length="256"
	 * 
	 */
	public String getEventSnmpHost() {
		return m_eventSnmpHost;
	}

	public void setEventSnmpHost(String eventsnmphost) {
		m_eventSnmpHost = eventsnmphost;
	}

	/**
	 * @hibernate.property column="serviceid" length="4"
	 * 
	 */
	public OnmsServiceType getServiceType() {
		return m_serviceType;
	}

	public void setServiceType(OnmsServiceType serviceType) {
		m_serviceType = serviceType;
	}

	/**
	 * @hibernate.property column="eventsnmp" length="256"
	 * 
	 */
	public String getEventSnmp() {
		return m_eventSnmp;
	}

	public void setEventSnmp(String eventsnmp) {
		m_eventSnmp = eventsnmp;
	}

	/**
	 * @hibernate.property column="eventparms" length="1024"
	 * 
	 */
	public String getEventParms() {
		return m_eventParms;
	}

	public void setEventParms(String eventparms) {
		m_eventParms = eventparms;
	}

	/**
	 * @hibernate.property column="eventcreatetime" length="8" not-null="true"
	 * 
	 */
	public Date getEventCreateTime() {
		return m_eventCreateTime;
	}

	public void setEventCreateTime(Date eventcreatetime) {
		m_eventCreateTime = eventcreatetime;
	}

	/**
	 * @hibernate.property column="eventdescr" length="4000"
	 * 
	 */
	public String getEventDescr() {
		return m_eventDescr;
	}

	public void setEventDescr(String eventdescr) {
		m_eventDescr = eventdescr;
	}

	/**
	 * @hibernate.property column="eventloggroup" length="32"
	 * 
	 */
	public String getEventLogGroup() {
		return m_eventLogGroup;
	}

	public void setEventLogGroup(String eventloggroup) {
		m_eventLogGroup = eventloggroup;
	}

	/**
	 * @hibernate.property column="eventlogmsg" length="256"
	 * 
	 */
	public String getEventLogMsg() {
		return m_eventLogMsg;
	}

	public void setEventLogMsg(String eventlogmsg) {
		m_eventLogMsg = eventlogmsg;
	}

	/**
	 * @hibernate.property column="eventseverity" length="4" not-null="true"
	 * 
	 */
	public Integer getEventSeverity() {
		return m_eventSeverity;
	}

	public void setEventSeverity(Integer severity) {
		m_eventSeverity = severity;
	}

	/**
	 * @hibernate.property column="eventpathoutage" length="1024"
	 * 
	 */
	public String getEventPathOutage() {
		return m_eventPathOutage;
	}

	public void setEventPathOutage(String eventpathoutage) {
		m_eventPathOutage = eventpathoutage;
	}

	/**
	 * @hibernate.property column="eventcorrelation" length="1024"
	 * 
	 */
	public String getEventCorrelation() {
		return m_eventCorrelation;
	}

	public void setEventCorrelation(String eventcorrelation) {
		m_eventCorrelation = eventcorrelation;
	}

	/**
	 * @hibernate.property column="eventsuppressedcount" length="4"
	 * 
	 */
	public Integer getEventSuppressedCount() {
		return m_eventSuppressedCount;
	}

	public void setEventSuppressedCount(Integer eventsuppressedcount) {
		m_eventSuppressedCount = eventsuppressedcount;
	}

	/**
	 * @hibernate.property column="eventoperinstruct" length="1024"
	 * 
	 */
	public String getEventOperInstruct() {
		return m_eventOperInstruct;
	}

	public void setEventOperInstruct(String eventoperinstruct) {
		m_eventOperInstruct = eventoperinstruct;
	}

	/**
	 * @hibernate.property column="eventautoaction" length="256"
	 * 
	 */
	public String getEventAutoAction() {
		return m_eventAutoAction;
	}

	public void setEventAutoAction(String eventautoaction) {
		m_eventAutoAction = eventautoaction;
	}

	/**
	 * @hibernate.property column="eventoperaction" length="256"
	 * 
	 */
	public String getEventOperAction() {
		return m_eventOperAction;
	}

	public void setEventOperAction(String eventoperaction) {
		m_eventOperAction = eventoperaction;
	}

	/**
	 * @hibernate.property column="eventoperactionmenutext" length="64"
	 * 
	 */
	public String getEventOperActionMenuText() {
		return m_eventOperActionMenuText;
	}

	public void setEventOperActionMenuText(String eventOperActionMenuText) {
		m_eventOperActionMenuText = eventOperActionMenuText;
	}

	/**
	 * @hibernate.property column="eventnotification" length="128"
	 * 
	 */
	public String getEventNotification() {
		return m_eventNotification;
	}

	public void setEventNotification(String eventnotification) {
		m_eventNotification = eventnotification;
	}

	/**
	 * @hibernate.property column="eventtticket" length="128"
	 * 
	 */
	public String getEventTTicket() {
		return m_eventTTicket;
	}

	public void setEventTTicket(String eventtticket) {
		m_eventTTicket = eventtticket;
	}

	/**
	 * @hibernate.property column="eventtticketstate" length="4"
	 * 
	 */
	public Integer getEventTTicketState() {
		return m_eventTTicketState;
	}

	public void setEventTTicketState(Integer eventtticketstate) {
		m_eventTTicketState = eventtticketstate;
	}

	/**
	 * @hibernate.property column="eventforward" length="256"
	 * 
	 */
	public String getEventForward() {
		return m_eventForward;
	}

	public void setEventForward(String eventforward) {
		m_eventForward = eventforward;
	}

	/**
	 * @hibernate.property column="eventmouseovertext" length="64"
	 * 
	 */
	public String getEventMouseOverText() {
		return m_eventMouseOverText;
	}

	public void setEventMouseOverText(String eventmouseovertext) {
		m_eventMouseOverText = eventmouseovertext;
	}

	/**
	 * @hibernate.property column="eventlog" length="1" not-null="true"
	 * 
	 */
	public String getEventLog() {
		return m_eventLog;
	}

	public void setEventLog(String eventlog) {
		m_eventLog = eventlog;
	}

	/**
	 * @hibernate.property column="eventdisplay" length="1" not-null="true"
	 * 
	 */
	public String getEventDisplay() {
		return m_eventDisplay;
	}

	public void setEventDisplay(String eventdisplay) {
		m_eventDisplay = eventdisplay;
	}

	/**
	 * @hibernate.property column="eventackuser" length="256"
	 * 
	 */
	public String getEventAckUser() {
		return m_eventAckUser;
	}

	public void setEventAckUser(String eventackuser) {
		m_eventAckUser = eventackuser;
	}

	/**
	 * @hibernate.property column="eventacktime" length="8"
	 * 
	 */
	public Date getEventAckTime() {
		return m_eventAckTime;
	}

	public void setEventAckTime(Date eventacktime) {
		m_eventAckTime = eventacktime;
	}

	/**
	 * @hibernate.property column="alarmid" length="4"
	 * 
	 */
	public OnmsAlarm getAlarm() {
		return m_alarm;
	}

	public void setAlarm(OnmsAlarm alarm) {
		m_alarm = alarm;
	}

	/**
	 * @hibernate.many-to-one not-null="true"
	 * @hibernate.column name="nodeid"
	 * 
	 */
	public org.opennms.netmgt.model.OnmsNode getNode() {
		return m_node;
	}

	public void setNode(org.opennms.netmgt.model.OnmsNode node) {
		m_node = node;
	}

	/**
	 * @hibernate.set lazy="true" inverse="true" cascade="none"
	 * @hibernate.key column="eventid"
	 * @hibernate.one-to-many class="org.opennms.netmgt.model.OnmsNotification"
	 * 
	 * old XDoclet1 Tags hibernate.collection-key column="eventid"
	 * hibernate.collection-one-to-many
	 * class="org.opennms.netmgt.model.OnmsNotification"
	 * 
	 */
	public Set getNotifications() {
		return m_notifications;
	}

	public void setNotifications(Set notifications) {
		m_notifications = notifications;
	}

	/**
	 * @hibernate.set lazy="true" inverse="true" cascade="none"
	 * @hibernate.key column="svcregainedeventid"
	 * @hibernate.one-to-many class="org.opennms.netmgt.model.OnmsOutage"
	 * 
	 * old XDoclet1 Tags hibernate.collection-key column="svcregainedeventid"
	 * hibernate.collection-one-to-many
	 * class="org.opennms.netmgt.model.OnmsOutage"
	 * 
	 */
	public Set getOutagesBySvcRegainedEventId() {
		return m_outagesBySvcRegainedEventId;
	}

	public void setOutagesBySvcRegainedEventId(Set outagesBySvcregainedeventid) {
		m_outagesBySvcRegainedEventId = outagesBySvcregainedeventid;
	}

	/**
	 * @hibernate.set lazy="true" inverse="true" cascade="none"
	 * @hibernate.key column="svclosteventid"
	 * @hibernate.one-to-many class="org.opennms.netmgt.model.OnmsOutage"
	 * 
	 * old XDoclet1 Tags hibernate.collection-key column="svclosteventid"
	 * hibernate.collection-one-to-many
	 * class="org.opennms.netmgt.model.OnmsOutage"
	 * 
	 */
	public Set getOutagesBySvclostEventId() {
		return m_outagesBySvclostEventId;
	}

	public void setOutagesBySvclostEventId(Set outagesBySvclosteventid) {
		m_outagesBySvclostEventId = outagesBySvclosteventid;
	}

	public String toString() {
		return new ToStringCreator(this).append("eventid", getId())
				.toString();
	}

	public void visit(EntityVisitor visitor) {
		throw new RuntimeException("visitor method not implemented");
	}

}
