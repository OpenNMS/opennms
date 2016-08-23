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

package org.opennms.plugins.elasticsearch.rest.archive;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.events.api.EventParameterUtils;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;


/**
 * <p>XmlOnmsEvent class.</p>
 * This class can unmarshal xml from OpenNMS ReST events interface
 */
@XmlRootElement(name="event")
public class XmlOnmsEvent  implements Serializable {

	public static final String NODE_LABEL="nodelabel";

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
	//private InetAddress m_ipAddr;

	/** persistent field */
	//private OnmsMonitoringSystem m_distPoller;

	/** nullable persistent field */
	private String m_eventSnmpHost;

	/** nullable persistent field */
	//private OnmsServiceType m_serviceType;

	/** nullable persistent field */
	private String m_eventSnmp;

	/** nullable persistent field */
	//private String m_eventParms;

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
	//private Integer m_ifIndex;

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
	//private String m_eventForward;

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
	//private OnmsAlarm m_alarm;

	/** persistent field */
	//private org.opennms.netmgt.model.OnmsNode m_node;

	/** persistent field */
	//private Set<OnmsNotification> m_notifications = new HashSet<OnmsNotification>();

	/** persistent field */
	//private Set<OnmsOutage> m_associatedServiceRegainedOutages = new HashSet<OnmsOutage>();

	/** persistent field */
	//private Set<OnmsOutage> m_associatedServiceLostOutages = new HashSet<OnmsOutage>();

	private String nodeLabel;

	private Integer nodeId;

	private String ipAddress;

	private List<OnmsEventParameter> eventParameters;


	/**
	 * default constructor
	 */
	public XmlOnmsEvent() {
	}


	/**
	 * <p>getId</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@XmlAttribute(name="id")
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
	@XmlElement(name="ipAddress")
	public String getIpAddr() {
		return this.ipAddress;
	}

	/**
	 * <p>setIpAddr</p>
	 *
	 * @param ipaddr a {@link java.lang.String} object.
	 */
	public void setIpAddr(String ipaddr) {
		this.ipAddress = ipaddr;
	}



	/**
	 * <p>getEventSnmpHost</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="snmpHost")
	public String getEventSnmpHost() {
		return m_eventSnmpHost;
	}



	/**
	 * <p>getEventSnmp</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="snmp")
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
	//	@XmlTransient
	//	public String getEventParms() {
	//		return m_eventParms;
	//	}


	@XmlElementWrapper(name="parameters")
	@XmlElement(name="parameter")
	public List<OnmsEventParameter> getEventParameters() {

		return this.eventParameters;
		//		if (m_eventParms == null) {
		//			return null;
		//		}
		//		return EventParameterUtils.decode(m_eventParms).stream().map(p -> new OnmsEventParameter(p)).collect(Collectors.toList());
	}

	public void setEventParameters(List<OnmsEventParameter> eventParameters){

		this.eventParameters=eventParameters;

	}

	//	/**
	//	 * <p>setEventParms</p>
	//	 *
	//	 * @param eventparms a {@link java.lang.String} object.
	//	 */
	//	public void setEventParms(String eventparms) {
	//		m_eventParms = eventparms;
	//	}

	/**
	 * <p>getEventCreateTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@XmlElement(name="createTime")
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

	//	/**
	//	 * <p>getEventForward</p>
	//	 *
	//	 * @return a {@link java.lang.String} object.
	//	 */
	//	@XmlTransient
	//	public String getEventForward() {
	//		return m_eventForward;
	//	}
	//
	//	/**
	//	 * <p>setEventForward</p>
	//	 *
	//	 * @param eventforward a {@link java.lang.String} object.
	//	 */
	//	public void setEventForward(String eventforward) {
	//		m_eventForward = eventforward;
	//	}

	/**
	 * <p>getEventMouseOverText</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="mouseOverText")
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



	@XmlElement(name="nodeId")
	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId){
		this.nodeId=nodeId;
	}



	@XmlElement(name="nodeLabel", required=false)
	public String getNodeLabel() {
		return nodeLabel;

	}

	public void setNodeLabel(String nodeLabel) {
		this.nodeLabel=nodeLabel;
	}


	//
	//	/**
	//	 * <p>getIfIndex</p>
	//	 *
	//	 * @return a {@link java.lang.Integer} object.
	//	 */
	//	public Integer getIfIndex() {
	//		return m_ifIndex;
	//	}
	//
	//	/**
	//	 * <p>setIfIndex</p>
	//	 *
	//	 * @param ifIndex a {@link java.lang.Integer} object.
	//	 */
	//	public void setIfIndex(Integer ifIndex) {
	//		m_ifIndex = ifIndex;
	//	}

	@Override
	public String toString() {
		return "XmlOnmsEvent [m_eventId=" + m_eventId + ", m_eventUei="
				+ m_eventUei + ", m_eventTime=" + m_eventTime
				+ ", m_eventHost=" + m_eventHost + ", m_eventSource="
				+ m_eventSource + ", m_eventSnmpHost=" + m_eventSnmpHost
				+ ", m_eventSnmp=" + m_eventSnmp + ", m_eventCreateTime="
				+ m_eventCreateTime + ", m_eventDescr=" + m_eventDescr
				+ ", m_eventLogGroup=" + m_eventLogGroup + ", m_eventLogMsg="
				+ m_eventLogMsg + ", m_eventSeverity=" + m_eventSeverity
				+ ", m_eventPathOutage=" + m_eventPathOutage
				+ ", m_eventCorrelation=" + m_eventCorrelation
				+ ", m_eventSuppressedCount=" + m_eventSuppressedCount
				+ ", m_eventOperInstruct=" + m_eventOperInstruct
				+ ", m_eventAutoAction=" + m_eventAutoAction
				+ ", m_eventOperAction=" + m_eventOperAction
				+ ", m_eventOperActionMenuText=" + m_eventOperActionMenuText
				+ ", m_eventNotification=" + m_eventNotification
				+ ", m_eventTTicket=" + m_eventTTicket
				+ ", m_eventTTicketState=" + m_eventTTicketState
				+ ", m_eventMouseOverText=" + m_eventMouseOverText
				+ ", m_eventLog=" + m_eventLog + ", m_eventDisplay="
				+ m_eventDisplay + ", m_eventAckUser=" + m_eventAckUser
				+ ", m_eventAckTime=" + m_eventAckTime + ", nodeLabel="
				+ nodeLabel + ", nodeId=" + nodeId + ", ipAddress=" + ipAddress
				+ ", eventParameters=" + eventParameters + "]";
	}


	public Event toEvent(){

		Event event = new Event();

		/** used in EventToIndex
		//event.getDbid();
		//event.getUei();
		//event.getCreationTime();
		//event.getSource();
		event.getInterfaceAddress();
		event.getService();
		//event.getSeverity();
		//event.getDescr();
		//event.getHost();
		//event.getParmCollection();
		event.getInterface();
		//event.getLogmsg().getContent();
		//event.getLogmsg().getDest();
		event.getNodeid();
		 **/

		if (this.getId()!=null) event.setDbid(this.getId());
		if (this.getEventUei() !=null ) event.setUei(this.getEventUei());
		if (this.getEventCreateTime() !=null ) event.setCreationTime(this.getEventCreateTime());
		//event.setSource(this.getEventSource();
		//event.setInterfaceAddress()
		//event.setService()
		if (this.getSeverityLabel() !=null ) event.setSeverity(this.getSeverityLabel());
		if (this.getEventDescr() !=null ) event.setDescr(this.getEventDescr());
		if (this.getEventHost()!=null ) event.setHost(this.getEventHost());
		
		List<Parm> parmColl=new ArrayList<Parm>();
		if (this.getEventParameters()!=null) {
			List<OnmsEventParameter> params = this.getEventParameters();
			
			for(OnmsEventParameter onmsEventParameter:params){
				
				String parmName = onmsEventParameter.getName();
				String type = onmsEventParameter.getType();
				String value = onmsEventParameter.getValue();
				
				Parm parm = new Parm();
				parm.setParmName(parmName);
				Value parmvalue = new Value();
				parmvalue.setType(type);
				parmvalue.setContent(value);
				parm.setValue(parmvalue);
				
				parmColl.add(parm);
			}
			
		}
		
		// add node label as param
		if ( this.getNodeLabel()!=null){
			Parm parm = new Parm();
			parm.setParmName(NODE_LABEL);
			Value parmValue = new Value();
			parm.setValue(parmValue);
			parmValue.setType("string");
			parmValue.setEncoding("text");
			parmValue.setContent(this.getNodeLabel());
			parmColl.add(parm);
		}
		event.setParmCollection(parmColl);
			
		//event.getInterface(this.getI)

		if (this.getEventLogMsg() !=null ) {
			Logmsg logmsg = new Logmsg();
			logmsg.setContent(this.getEventLogMsg());
			event.setLogmsg(logmsg );
		}

		if (this.getNodeId() !=null ) {
			Integer i = this.getNodeId();
			Long l = Long.valueOf(i.longValue());
			event.setNodeid(l);
		}

		return event;
	}

}
