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
	private String m_distPoller;

	/** nullable persistent field */
	private String m_eventSnmpHost;

	/** nullable persistent field */
	private Integer m_serviceId;

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
	private int m_eventSeverity;

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
	private String m_eventOperactionMenuText;

	/** nullable persistent field */
	private String m_eventNotification;

	/** nullable persistent field */
	private String m_eventtTicket;

	/** nullable persistent field */
	private Integer m_eventTTicketState;

	/** nullable persistent field */
	private String m_eventForward;

	/** nullable persistent field */
	private String m_eventMouseoverText;

	/** persistent field */
	private String m_eventLog;

	/** persistent field */
	private String m_eventDisplay;

	/** nullable persistent field */
	private String m_eventAckUser;

	/** nullable persistent field */
	private Date m_eventAckTime;

	/** nullable persistent field */
	private Integer m_alarmId;

	/** persistent field */
	private org.opennms.netmgt.model.OnmsNode m_node;

	/** persistent field */
	private Set m_notifications;

	/** persistent field */
	private Set m_outagesBySvcRegainedEventId;

	/** persistent field */
	private Set m_outagesBySvclostEventId;

	/** persistent field */
	private Set m_alarms;

	/** full constructor */
	public OnmsEvent(Integer eventid, String eventuei, Date eventtime,
			String eventhost, String eventsource, String ipaddr,
			String eventdpname, String eventsnmphost, Integer serviceid,
			String eventsnmp, String eventparms, Date eventcreatetime,
			String eventdescr, String eventloggroup, String eventlogmsg,
			int eventseverity, String eventpathoutage, String eventcorrelation,
			Integer eventsuppressedcount, String eventoperinstruct,
			String eventautoaction, String eventoperaction,
			String eventoperactionmenutext, String eventnotification,
			String eventtticket, Integer eventtticketstate,
			String eventforward, String eventmouseovertext, String eventlog,
			String eventdisplay, String eventackuser, Date eventacktime,
			Integer alarmid, org.opennms.netmgt.model.OnmsNode node,
			Set notifications, Set outagesBySvcregainedeventid,
			Set outagesBySvclosteventid, Set alarms) {
		m_eventId = eventid;
		m_eventUei = eventuei;
		m_eventTime = eventtime;
		m_eventHost = eventhost;
		m_eventSource = eventsource;
		m_ipAddr = ipaddr;
		m_distPoller = eventdpname;
		m_eventSnmpHost = eventsnmphost;
		m_serviceId = serviceid;
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
		m_eventOperactionMenuText = eventoperactionmenutext;
		m_eventNotification = eventnotification;
		m_eventtTicket = eventtticket;
		m_eventTTicketState = eventtticketstate;
		m_eventForward = eventforward;
		m_eventMouseoverText = eventmouseovertext;
		m_eventLog = eventlog;
		m_eventDisplay = eventdisplay;
		m_eventAckUser = eventackuser;
		m_eventAckTime = eventacktime;
		m_alarmId = alarmid;
		m_node = node;
		m_notifications = notifications;
		m_outagesBySvcRegainedEventId = outagesBySvcregainedeventid;
		m_outagesBySvclostEventId = outagesBySvclosteventid;
		m_alarms = alarms;
	}

	/** default constructor */
	public OnmsEvent() {
	}

	/** minimal constructor */
	public OnmsEvent(Integer eventid, String eventuei, Date eventtime,
			String eventsource, String eventdpname, Date eventcreatetime,
			int eventseverity, String eventlog, String eventdisplay,
			org.opennms.netmgt.model.OnmsNode node, Set notifications,
			Set outagesBySvcregainedeventid, Set outagesBySvclosteventid,
			Set alarms) {
		m_eventId = eventid;
		m_eventUei = eventuei;
		m_eventTime = eventtime;
		m_eventSource = eventsource;
		m_distPoller = eventdpname;
		m_eventCreateTime = eventcreatetime;
		m_eventSeverity = eventseverity;
		m_eventLog = eventlog;
		m_eventDisplay = eventdisplay;
		m_node = node;
		m_notifications = notifications;
		m_outagesBySvcRegainedEventId = outagesBySvcregainedeventid;
		m_outagesBySvclostEventId = outagesBySvclosteventid;
		m_alarms = alarms;
	}

	/**
	 * @hibernate.id generator-class="assigned" type="java.lang.Integer"
	 *               column="eventid"
	 * @hibernate.generator-param name="sequence" value="eventsNxtId"
	 */
	public Integer getEventId() {
		return m_eventId;
	}

	public void setEventId(Integer eventid) {
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
	public Date getEventtime() {
		return m_eventTime;
	}

	public void setEventtime(Date eventtime) {
		m_eventTime = eventtime;
	}

	/**
	 * @hibernate.property column="eventhost" length="256"
	 * 
	 */
	public String getEventhost() {
		return m_eventHost;
	}

	public void setEventhost(String eventhost) {
		m_eventHost = eventhost;
	}

	/**
	 * @hibernate.property column="eventsource" length="128" not-null="true"
	 * 
	 */
	public String getEventsource() {
		return m_eventSource;
	}

	public void setEventsource(String eventsource) {
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
	public String getDistPoller() {
		return m_distPoller;
	}

	public void setDistPoller(String eventdpname) {
		m_distPoller = eventdpname;
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
	public Integer getServiceId() {
		return m_serviceId;
	}

	public void setServiceId(Integer serviceid) {
		m_serviceId = serviceid;
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
	public int getEventSeverity() {
		return m_eventSeverity;
	}

	public void setEventSeverity(int eventseverity) {
		m_eventSeverity = eventseverity;
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
	public String getEventOperaction() {
		return m_eventOperAction;
	}

	public void setEventOperaction(String eventoperaction) {
		m_eventOperAction = eventoperaction;
	}

	/**
	 * @hibernate.property column="eventoperactionmenutext" length="64"
	 * 
	 */
	public String getEventOperactionMenuText() {
		return m_eventOperactionMenuText;
	}

	public void setEventOperactionMenuText(String eventoperactionmenutext) {
		m_eventOperactionMenuText = eventoperactionmenutext;
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
	public String getEventtTicket() {
		return m_eventtTicket;
	}

	public void setEventtTicket(String eventtticket) {
		m_eventtTicket = eventtticket;
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
	public String getEventMouseoverText() {
		return m_eventMouseoverText;
	}

	public void setEventMouseoverText(String eventmouseovertext) {
		m_eventMouseoverText = eventmouseovertext;
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
	public Integer getAlarmId() {
		return m_alarmId;
	}

	public void setAlarmId(Integer alarmid) {
		m_alarmId = alarmid;
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

	/**
	 * @hibernate.set lazy="true" inverse="true" cascade="none"
	 * @hibernate.key column="lasteventid"
	 * @hibernate.one-to-many class="org.opennms.netmgt.model.OnmsAlarm"
	 * 
	 * old XDoclet1 Tags hibernate.collection-key column="lasteventid"
	 * hibernate.collection-one-to-many
	 * class="org.opennms.netmgt.model.OnmsAlarm"
	 * 
	 */
	public Set getAlarms() {
		return m_alarms;
	}

	public void setAlarms(Set alarms) {
		m_alarms = alarms;
	}

	public String toString() {
		return new ToStringCreator(this).append("eventid", getEventId())
				.toString();
	}

	public void visit(EntityVisitor visitor) {
		throw new RuntimeException("visitor method not implemented");
	}

}
