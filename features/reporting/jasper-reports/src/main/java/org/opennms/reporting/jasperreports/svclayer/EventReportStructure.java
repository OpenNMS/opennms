package org.opennms.reporting.jasperreports.svclayer;

public class EventReportStructure {

	/** The alarmId if reduced. Can be null. */
	private Integer m_alarmId;

	/** The human-readable name of the node of this alarm. Can be null. */
	private String m_nodeLabel;

	/** Unique identifier for the event, cannot be null */
	private Integer m_eventId;

	/** Universal Event Identifer (UEI) for this event, cannot be null */
	private String m_eventUEI;

	/** nullable persistent field */
	private Integer m_nodeId;

	/** persistent field */
	private String m_eventTime;

	/** nullable persistent field */
	private String m_eventHost;

	/** persistent field */
	private String m_eventSource;

	/** nullable persistent field */
	private String m_ipAddr;

	/** nullable persistent field */
	private String m_eventDpName;

	/** nullable persistent field */
	private String m_eventSnmpHost;

	/** nullable persistent field */
	private Integer m_serviceId;

	/** nullable persistent field */
	private String m_eventSnmp;

	/** persistent field */
	private String m_eventParms;

	/** persistent field */
	private String m_eventCreateTime;

	/** nullable persistent field */
	private String m_eventDescr;

	/** nullable persistent field */
	private String m_eventLogGroup;

	/** nullable persistent field */
	private String m_eventLogMsg;

	/** persistent field */
	private String m_eventSeverity;

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
	private String m_eventAckTime;

	/** nullable persistent field */
	private Integer m_ifIndex;

	/**
	 * <p>
	 * Getter for the field <code>nodeLabel</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getNodeLabel() {
		return m_nodeLabel;
	}

	/**
	 * <p>
	 * Setter for the field <code>nodeLabel</code>.
	 * </p>
	 * 
	 * @param nodeLabel
	 *            a {@link java.lang.String} object.
	 */
	public void setNodeLabel(String nodeLabel) {
		m_nodeLabel = nodeLabel;
	}

	/**
	 * <p>
	 * Getter for the field <code>eventId</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getEventId() {
		return m_eventId;
	}

	/**
	 * <p>
	 * Setter for the field <code>eventId</code>.
	 * </p>
	 * 
	 * @param eventId
	 *            an Integer.
	 */
	public void setEventId(Integer eventId) {
		m_eventId = eventId;
	}

	/**
	 * <p>
	 * Getter for the field <code>alarmId</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getAlarmId() {
		return m_alarmId;
	}

	/**
	 * <p>
	 * Setter for the field <code>alarmId</code>.
	 * </p>
	 * 
	 * @param alarmId
	 *            a {@link java.lang.Integer} object.
	 */
	public void setAlarmId(Integer alarmId) {
		m_alarmId = alarmId;
	}

	/**
	 * <p>
	 * Getter for the field <code>eventUEI</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventUEI() {
		return m_eventUEI;
	}

	/**
	 * <p>
	 * Setter for the field <code>eventUEI</code>.
	 * </p>
	 * 
	 * @param eventUEI
	 *            a {@link java.lang.String} object.
	 */
	public void setEventUEI(String eventUEI) {
		m_eventUEI = eventUEI;
	}

	/**
	 * <p>
	 * Getter for the field <code>eventDpName</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventDpName() {
		return m_eventDpName;
	}

	/**
	 * <p>
	 * Setter for the field <code>eventDpName</code>.
	 * </p>
	 * 
	 * @param eventDpName
	 *            a {@link java.lang.String} object.
	 */
	public void setEventDpName(String eventDpName) {
		this.m_eventDpName = eventDpName;
	}

	/**
	 * <p>
	 * getNodeId
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getNodeId() {
		return m_nodeId;
	}

	/**
	 * <p>
	 * Setter for the field <code>nodeId</code>.
	 * </p>
	 * 
	 * @param nodeId
	 *            a {@link java.lang.Integer} object.
	 */
	public void setNodeId(Integer nodeId) {
		m_nodeId = nodeId;
	}

	/**
	 * <p>
	 * getServiceId
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getServiceId() {
		return m_serviceId;
	}

	/**
	 * <p>
	 * setServiceID
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public void setServiceID(Integer serviceId) {
		this.m_serviceId = serviceId;
	}

	/**
	 * <p>
	 * getEventParms
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventParms() {
		return m_eventParms;
	}

	/**
	 * <p>
	 * setEventParms
	 * </p>
	 * 
	 * @param eventparms
	 *            a {@link java.lang.String} object.
	 */
	public void setEventParms(String eventParms) {
		m_eventParms = eventParms;
	}

	/**
	 * <p>
	 * getIfIndex
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getIfIndex() {
		return m_ifIndex;
	}

	/**
	 * <p>
	 * setIfIndex
	 * </p>
	 * 
	 * @param ifIndex
	 *            a {@link java.lang.Integer} object.
	 */
	public void setIfIndex(Integer ifIndex) {
		m_ifIndex = ifIndex;
	}

	/**
	 * <p>
	 * getEventTime
	 * </p>
	 * 
	 * @return a {@link java.util.String} object.
	 */
	public String getEventTime() {
		return m_eventTime;
	}

	/**
	 * <p>
	 * setEventTime
	 * </p>
	 * 
	 * @param eventtime
	 *            a {@link java.util.String} object.
	 */
	public void setEventTime(String eventtime) {
		m_eventTime = eventtime;
	}

	/**
	 * <p>
	 * getEventHost
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventHost() {
		return m_eventHost;
	}

	/**
	 * <p>
	 * setEventHost
	 * </p>
	 * 
	 * @param eventhost
	 *            a {@link java.lang.String} object.
	 */
	public void setEventHost(String eventhost) {
		m_eventHost = eventhost;
	}

	/**
	 * <p>
	 * getEventSource
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventSource() {
		return m_eventSource;
	}

	/**
	 * <p>
	 * setEventSource
	 * </p>
	 * 
	 * @param eventsource
	 *            a {@link java.lang.String} object.
	 */
	public void setEventSource(String eventsource) {
		m_eventSource = eventsource;
	}

	/**
	 * <p>
	 * getIpAddr
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getIpAddr() {
		return m_ipAddr;
	}

	/**
	 * <p>
	 * setIpAddr
	 * </p>
	 * 
	 * @param ipaddr
	 *            a {@link java.lang.String} object.
	 */
	public void setIpAddr(String ipaddr) {
		m_ipAddr = ipaddr;
	}

	/**
	 * <p>
	 * getEventSnmpHost
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventSnmpHost() {
		return m_eventSnmpHost;
	}

	/**
	 * <p>
	 * setEventSnmpHost
	 * </p>
	 * 
	 * @param eventsnmphost
	 *            a {@link java.lang.String} object.
	 */
	public void setEventSnmpHost(String eventsnmphost) {
		m_eventSnmpHost = eventsnmphost;
	}

	/**
	 * <p>
	 * getEventSnmp
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventSnmp() {
		return m_eventSnmp;
	}

	/**
	 * <p>
	 * setEventSnmp
	 * </p>
	 * 
	 * @param eventsnmp
	 *            a {@link java.lang.String} object.
	 */
	public void setEventSnmp(String eventsnmp) {
		m_eventSnmp = eventsnmp;
	}

	/**
	 * <p>
	 * getEventCreateTime
	 * </p>
	 * 
	 * @return a {@link java.util.String} object.
	 */
	public String getEventCreateTime() {
		return m_eventCreateTime;
	}

	/**
	 * <p>
	 * setEventCreateTime
	 * </p>
	 * 
	 * @param eventcreatetime
	 *            a {@link java.util.String} object.
	 */
	public void setEventCreateTime(String eventcreatetime) {
		m_eventCreateTime = eventcreatetime;
	}

	/**
	 * <p>
	 * getEventCreateTime
	 * </p>
	 * 
	 * @return a {@link java.util.String} object.
	 */
	public String getCreateTime() {
		return m_eventCreateTime;
	}

	/**
	 * <p>
	 * setEventCreateTime
	 * </p>
	 * 
	 * @param eventcreatetime
	 *            a {@link java.util.String} object.
	 */
	public void setCreateTime(String eventcreatetime) {
		m_eventCreateTime = eventcreatetime;
	}

	/**
	 * <p>
	 * getEventDescr
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventDescr() {
		return m_eventDescr;
	}

	/**
	 * <p>
	 * setEventDescr
	 * </p>
	 * 
	 * @param eventdescr
	 *            a {@link java.lang.String} object.
	 */
	public void setEventDescr(String eventdescr) {
		m_eventDescr = eventdescr;
	}

	/**
	 * <p>
	 * getEventLogGroup
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventLogGroup() {
		return m_eventLogGroup;
	}

	/**
	 * <p>
	 * setEventLogGroup
	 * </p>
	 * 
	 * @param eventloggroup
	 *            a {@link java.lang.String} object.
	 */
	public void setEventLogGroup(String eventloggroup) {
		m_eventLogGroup = eventloggroup;
	}

	/**
	 * <p>
	 * getEventLogMsg
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventLogMsg() {
		return m_eventLogMsg;
	}

	/**
	 * <p>
	 * setEventLogMsg
	 * </p>
	 * 
	 * @param eventlogmsg
	 *            a {@link java.lang.String} object.
	 */
	public void setEventLogMsg(String eventlogmsg) {
		m_eventLogMsg = eventlogmsg;
	}

	/**
	 * <p>
	 * getEventSeverity
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public String getEventSeverity() {
		return m_eventSeverity;
	}

	/**
	 * <p>
	 * setEventSeverity
	 * </p>
	 * 
	 * @param severity
	 *            a {@link java.lang.Integer} object.
	 */
	public void setEventSeverity(String severity) {
		m_eventSeverity = severity;
	}

	/**
	 * <p>
	 * getEventPathOutage
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventPathOutage() {
		return m_eventPathOutage;
	}

	/**
	 * <p>
	 * setEventPathOutage
	 * </p>
	 * 
	 * @param eventpathoutage
	 *            a {@link java.lang.String} object.
	 */
	public void setEventPathOutage(String eventpathoutage) {
		m_eventPathOutage = eventpathoutage;
	}

	/**
	 * <p>
	 * getEventCorrelation
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventCorrelation() {
		return m_eventCorrelation;
	}

	/**
	 * <p>
	 * setEventCorrelation
	 * </p>
	 * 
	 * @param eventcorrelation
	 *            a {@link java.lang.String} object.
	 */
	public void setEventCorrelation(String eventcorrelation) {
		m_eventCorrelation = eventcorrelation;
	}

	/**
	 * <p>
	 * getEventSuppressedCount
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getEventSuppressedCount() {
		return m_eventSuppressedCount;
	}

	/**
	 * <p>
	 * setEventSuppressedCount
	 * </p>
	 * 
	 * @param eventsuppressedcount
	 *            a {@link java.lang.Integer} object.
	 */
	public void setEventSuppressedCount(Integer eventsuppressedcount) {
		m_eventSuppressedCount = eventsuppressedcount;
	}

	/**
	 * <p>
	 * getEventOperInstruct
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventOperInstruct() {
		return m_eventOperInstruct;
	}

	/**
	 * <p>
	 * setEventOperInstruct
	 * </p>
	 * 
	 * @param eventoperinstruct
	 *            a {@link java.lang.String} object.
	 */
	public void setEventOperInstruct(String eventoperinstruct) {
		m_eventOperInstruct = eventoperinstruct;
	}

	/**
	 * <p>
	 * getEventAutoAction
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventAutoAction() {
		return m_eventAutoAction;
	}

	/**
	 * <p>
	 * setEventAutoAction
	 * </p>
	 * 
	 * @param eventautoaction
	 *            a {@link java.lang.String} object.
	 */
	public void setEventAutoAction(String eventautoaction) {
		m_eventAutoAction = eventautoaction;
	}

	/**
	 * <p>
	 * getEventOperAction
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventOperAction() {
		return m_eventOperAction;
	}

	/**
	 * <p>
	 * setEventOperAction
	 * </p>
	 * 
	 * @param eventoperaction
	 *            a {@link java.lang.String} object.
	 */
	public void setEventOperAction(String eventoperaction) {
		m_eventOperAction = eventoperaction;
	}

	/**
	 * <p>
	 * getEventOperActionMenuText
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventOperActionMenuText() {
		return m_eventOperActionMenuText;
	}

	/**
	 * <p>
	 * setEventOperActionMenuText
	 * </p>
	 * 
	 * @param eventOperActionMenuText
	 *            a {@link java.lang.String} object.
	 */
	public void setEventOperActionMenuText(String eventOperActionMenuText) {
		m_eventOperActionMenuText = eventOperActionMenuText;
	}

	/**
	 * <p>
	 * getEventNotification
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventNotification() {
		return m_eventNotification;
	}

	/**
	 * <p>
	 * setEventNotification
	 * </p>
	 * 
	 * @param eventnotification
	 *            a {@link java.lang.String} object.
	 */
	public void setEventNotification(String eventnotification) {
		m_eventNotification = eventnotification;
	}

	/**
	 * <p>
	 * getEventTTicket
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventTTicket() {
		return m_eventTTicket;
	}

	/**
	 * <p>
	 * setEventTTicket
	 * </p>
	 * 
	 * @param eventtticket
	 *            a {@link java.lang.String} object.
	 */
	public void setEventTTicket(String eventtticket) {
		m_eventTTicket = eventtticket;
	}

	/**
	 * <p>
	 * getEventTTicketState
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getEventTTicketState() {
		return m_eventTTicketState;
	}

	/**
	 * <p>
	 * setEventTTicketState
	 * </p>
	 * 
	 * @param eventtticketstate
	 *            a {@link java.lang.Integer} object.
	 */
	public void setEventTTicketState(Integer eventtticketstate) {
		m_eventTTicketState = eventtticketstate;
	}

	/**
	 * <p>
	 * getEventForward
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventForward() {
		return m_eventForward;
	}

	/**
	 * <p>
	 * setEventForward
	 * </p>
	 * 
	 * @param eventforward
	 *            a {@link java.lang.String} object.
	 */
	public void setEventForward(String eventforward) {
		m_eventForward = eventforward;
	}

	/**
	 * <p>
	 * getEventMouseOverText
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventMouseOverText() {
		return m_eventMouseOverText;
	}

	/**
	 * <p>
	 * setEventMouseOverText
	 * </p>
	 * 
	 * @param eventmouseovertext
	 *            a {@link java.lang.String} object.
	 */
	public void setEventMouseOverText(String eventmouseovertext) {
		m_eventMouseOverText = eventmouseovertext;
	}

	/**
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventLog() {
		return m_eventLog;
	}

	/**
	 * <p>
	 * setEventLog
	 * </p>
	 * 
	 * @param eventlog
	 *            a {@link java.lang.String} object.
	 */
	public void setEventLog(String eventlog) {
		m_eventLog = eventlog;
	}

	/**
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventDisplay() {
		return m_eventDisplay;
	}

	/**
	 * <p>
	 * setEventDisplay
	 * </p>
	 * 
	 * @param eventdisplay
	 *            a {@link java.lang.String} object.
	 */
	public void setEventDisplay(String eventdisplay) {
		m_eventDisplay = eventdisplay;
	}

	/**
	 * <p>
	 * getEventAckUser
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventAckUser() {
		return m_eventAckUser;
	}

	/**
	 * <p>
	 * setEventAckUser
	 * </p>
	 * 
	 * @param eventackuser
	 *            a {@link java.lang.String} object.
	 */
	public void setEventAckUser(String eventackuser) {
		m_eventAckUser = eventackuser;
	}

	/**
	 * <p>
	 * getEventAckTime
	 * </p>
	 * 
	 * @return a {@link java.util.String} object.
	 */
	public String getEventAckTime() {
		return m_eventAckTime;
	}

	/**
	 * <p>
	 * setEventAckTime
	 * </p>
	 * 
	 * @param eventacktime
	 *            a {@link java.util.String} object.
	 */
	public void setEventAckTime(String eventacktime) {
		m_eventAckTime = eventacktime;
	}
}