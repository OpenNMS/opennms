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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.netmgt.events.api.EventParameterUtils;
import org.opennms.netmgt.xml.event.Event;

import com.google.common.base.MoreObjects;

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
	@Id
	@Column(name="eventId", nullable=false)
	@SequenceGenerator(name="eventSequence", sequenceName="eventsNxtId")
	@GeneratedValue(generator="eventSequence")
	private Integer eventId;

	/** persistent field */
	@Column(name="eventUei", length=256, nullable=false)
	private String eventUei;

	/** persistent field */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eventTime", nullable=false)
	private Date eventTime;

	/** nullable persistent field */
	@Column(name="eventHost", length=256)
	private String eventHost;

	/** persistent field */
	@Column(name="eventSource", length=128, nullable=false)
	private String eventSource;

	/** nullable persistent field */
	@Column(name="ipAddr")
	@Type(type="org.opennms.netmgt.model.InetAddressUserType")
	private InetAddress ipAddr;

	/** persistent field */
	@ManyToOne
	@JoinColumn(name="systemId", nullable=false)
	private OnmsMonitoringSystem distPoller;

	/** nullable persistent field */
	@Column(name="eventSnmpHost", length=256)
	private String eventSnmpHost;

	/** nullable persistent field */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="serviceId", nullable=true)
	private OnmsServiceType serviceType;

	/** nullable persistent field */
	@Column(name="eventSnmp", length=256)
	private String eventSnmp;

	@OneToMany(mappedBy="event", cascade=CascadeType.ALL)
	private List<OnmsEventParameter> eventParameters;

	/** persistent field */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eventCreateTime", nullable=false)
	private Date eventCreateTime;

	/** nullable persistent field */
	@Column(name="eventDescr", length=4000)
	private String eventDescr;

	/** nullable persistent field */
	@Column(name="eventLogGroup", length=32)
	private String eventLogGroup;

	/** nullable persistent field */
	@Column(name="eventLogMsg", length=1024)
	private String eventLogMsg;

	/** persistent field */
	@Column(name="eventSeverity", nullable=false)
	private Integer eventSeverity;

	/** nullable persistent field */
	@Column(name="ifIndex")
    private Integer ifIndex;

	/** nullable persistent field */
	@Column(name="eventPathOutage", length=1024)
	private String eventPathOutage;

	/** nullable persistent field */
	@Column(name="eventCorrelation", length=1024)
	private String eventCorrelation;

	/** nullable persistent field */
	@Column(name="eventSuppressedCount")
	private Integer eventSuppressedCount;

	/** nullable persistent field */
	@Column(name="eventOperInstruct")
	private String eventOperInstruct;

	/** nullable persistent field */
	@Column(name="eventAutoAction", length=256)
	private String eventAutoAction;

	/** nullable persistent field */
	@Column(name="eventOperAction", length=256)
	private String eventOperAction;

	/** nullable persistent field */
	@Column(name="eventOperActionMenuText", length=64)
	private String eventOperActionMenuText;

	/** nullable persistent field */
	@Column(name="eventNotification", length=128)
	private String eventNotification;

	/** nullable persistent field */
	@Column(name="eventTTicket", length=128)
	private String eventTTicket;

	/** nullable persistent field */
	@Column(name="eventTTicketState")
	private Integer eventTTicketState;

	/** nullable persistent field */
	@Column(name="eventForward", length=256)
	private String eventForward;

	/** nullable persistent field */
	@Column(name="eventMouseOverText", length=64)
	private String eventMouseOverText;

	/** persistent field */
	@Column(name="eventLog", length=1, nullable=false)
	private String eventLog;

	/** persistent field */
	@Column(name="eventDisplay", length=1, nullable=false)
	private String eventDisplay;

	/** nullable persistent field */
	@Column(name="eventAckUser", length=256)
	private String eventAckUser;

	/** nullable persistent field */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eventAckTime")
	private Date eventAckTime;

	/** nullable persistent field */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="alarmId")
	private OnmsAlarm alarm;

	/** persistent field */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="nodeId")
	private org.opennms.netmgt.model.OnmsNode node;

	/** persistent field */
	@OneToMany(mappedBy="event", fetch=FetchType.LAZY)
	private Set<OnmsNotification> notifications = new HashSet<>();

	/** persistent field */
	@OneToMany(mappedBy="serviceRegainedEvent", fetch=FetchType.LAZY)
	private Set<OnmsOutage> associatedServiceRegainedOutages = new HashSet<>();

	/** persistent field */
	@OneToMany(mappedBy="serviceLostEvent", fetch=FetchType.LAZY)
	private Set<OnmsOutage> associatedServiceLostOutages = new HashSet<>();

	/**
	 * default constructor
	 */
	public OnmsEvent() {
	}

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
	@XmlAttribute(name="id")
	public Integer getId() {
		return eventId;
	}

	/**
	 * <p>setId</p>
	 *
	 * @param eventid a {@link java.lang.Integer} object.
	 */
	public void setId(Integer eventid) {
		eventId = eventid;
	}

	/**
	 * <p>getEventUei</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="uei")
	public String getEventUei() {
		return eventUei;
	}

	/**
	 * <p>setEventUei</p>
	 *
	 * @param eventuei a {@link java.lang.String} object.
	 */
	public void setEventUei(String eventuei) {
		eventUei = eventuei;
	}

	/**
	 * <p>getEventTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@XmlElement(name="time")
	public Date getEventTime() {
		return eventTime;
	}

	/**
	 * <p>setEventTime</p>
	 *
	 * @param eventtime a {@link java.util.Date} object.
	 */
	public void setEventTime(Date eventtime) {
		eventTime = eventtime;
	}

	/**
	 * <p>getEventHost</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="host")
	public String getEventHost() {
		return eventHost;
	}

	/**
	 * <p>setEventHost</p>
	 *
	 * @param eventhost a {@link java.lang.String} object.
	 */
	public void setEventHost(String eventhost) {
		eventHost = eventhost;
	}

	/**
	 * <p>getEventSource</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="source")
	public String getEventSource() {
		return eventSource;
	}

	/**
	 * <p>setEventSource</p>
	 *
	 * @param eventsource a {@link java.lang.String} object.
	 */
	public void setEventSource(String eventsource) {
		eventSource = eventsource;
	}

	/**
	 * <p>getIpAddr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="ipAddress")
	@XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
	public InetAddress getIpAddr() {
		return ipAddr;
	}

	/**
	 * <p>setIpAddr</p>
	 *
	 * @param ipaddr a {@link java.lang.String} object.
	 */
	public void setIpAddr(InetAddress ipaddr) {
		ipAddr = ipaddr;
	}

	/**
	 * <p>getDistPoller</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsMonitoringSystem} object.
	 */
	@XmlTransient
	public OnmsMonitoringSystem getDistPoller() {
		return distPoller;
	}

	/**
	 * <p>setDistPoller</p>
	 *
	 * @param distPoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
	 */
	public void setDistPoller(OnmsMonitoringSystem distPoller) {
		this.distPoller = distPoller;
	}

	/**
	 * <p>getEventSnmpHost</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="snmpHost")
	public String getEventSnmpHost() {
		return eventSnmpHost;
	}

	/**
	 * <p>setEventSnmpHost</p>
	 *
	 * @param eventsnmphost a {@link java.lang.String} object.
	 */
	public void setEventSnmpHost(String eventsnmphost) {
		eventSnmpHost = eventsnmphost;
	}

	/**
	 * <p>getServiceType</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsServiceType} object.
	 */
	public OnmsServiceType getServiceType() {
		return serviceType;
	}

	/**
	 * <p>setServiceType</p>
	 *
	 * @param serviceType a {@link org.opennms.netmgt.model.OnmsServiceType} object.
	 */
	public void setServiceType(OnmsServiceType serviceType) {
		this.serviceType = serviceType;
	}

	/**
	 * <p>getEventSnmp</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="snmp")
	public String getEventSnmp() {
		return eventSnmp;
	}

	/**
	 * <p>setEventSnmp</p>
	 *
	 * @param eventsnmp a {@link java.lang.String} object.
	 */
	public void setEventSnmp(String eventsnmp) {
		eventSnmp = eventsnmp;
	}

	@XmlElementWrapper(name="parameters")
	@XmlElement(name="parameter")
	public List<OnmsEventParameter> getEventParameters() {
		if(this.eventParameters != null) {
			this.eventParameters.sort(Comparator.comparing(OnmsEventParameter::getPosition));
		}
		return this.eventParameters;
	}

	public void setEventParameters(List<OnmsEventParameter> eventParameters) {
		this.eventParameters = eventParameters;
		setPositionsOnParameters(this.eventParameters);
	}

	public void setEventParametersFromEvent(final Event event) {
		this.eventParameters = EventParameterUtils.normalizePreserveOrder(event.getParmCollection()).stream()
				.map(p -> new OnmsEventParameter(this, p))
				.collect(Collectors.toList());
		setPositionsOnParameters(eventParameters);
	}

	public void addEventParameter(OnmsEventParameter parameter) {
		if (eventParameters == null) {
			eventParameters = new ArrayList<>();
		}
		if (eventParameters.contains(parameter)) {
			eventParameters.remove(parameter);
		}
		eventParameters.add(parameter);
        setPositionsOnParameters(eventParameters);
	}

	/**
     * We need this method to preserve the order in the m_eventParameters when saved and retrieved from the database.
     * There might be a more elegant solution via JPA but none seems to work in our context, see also:
     * https://issues.opennms.org/browse/NMS-9827
     */
    private void setPositionsOnParameters(List<OnmsEventParameter> parameters) {
        if (parameters != null) {
            // give each parameter a distinct position
            for (int i = 0; i < parameters.size(); i++) {
                parameters.get(i).setPosition(i);
            }
        }
    }

	/**
	 * <p>getEventCreateTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@XmlElement(name="createTime")
	public Date getEventCreateTime() {
		return eventCreateTime;
	}

	/**
	 * <p>setEventCreateTime</p>
	 *
	 * @param eventcreatetime a {@link java.util.Date} object.
	 */
	public void setEventCreateTime(Date eventcreatetime) {
		eventCreateTime = eventcreatetime;
	}

	/**
	 * <p>getEventDescr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="description")
	public String getEventDescr() {
		return eventDescr;
	}

	/**
	 * <p>setEventDescr</p>
	 *
	 * @param eventdescr a {@link java.lang.String} object.
	 */
	public void setEventDescr(String eventdescr) {
		eventDescr = eventdescr;
	}

	/**
	 * <p>getEventLogGroup</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="logGroup")
	public String getEventLogGroup() {
		return eventLogGroup;
	}

	/**
	 * <p>setEventLogGroup</p>
	 *
	 * @param eventloggroup a {@link java.lang.String} object.
	 */
	public void setEventLogGroup(String eventloggroup) {
		eventLogGroup = eventloggroup;
	}

	/**
	 * <p>getEventLogMsg</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="logMessage")
	public String getEventLogMsg() {
		return eventLogMsg;
	}

	/**
	 * <p>setEventLogMsg</p>
	 *
	 * @param eventlogmsg a {@link java.lang.String} object.
	 */
	public void setEventLogMsg(String eventlogmsg) {
		eventLogMsg = eventlogmsg;
	}

	/**
	 * <p>getEventSeverity</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@XmlTransient
	public Integer getEventSeverity() {
		return eventSeverity;
	}

	/**
	 * <p>setEventSeverity</p>
	 *
	 * @param severity a {@link java.lang.Integer} object.
	 */
	public void setEventSeverity(Integer severity) {
		eventSeverity = severity;
	}

    /**
     * <p>getSeverityLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlAttribute(name="severity")
    public String getSeverityLabel() {
        return OnmsSeverity.get(eventSeverity).name();
    }

    /**
     * <p>setSeverityLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setSeverityLabel(String label) {
        eventSeverity = OnmsSeverity.get(label).getId();
    }


	/**
	 * <p>getEventPathOutage</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="pathOutage")
	public String getEventPathOutage() {
		return eventPathOutage;
	}

	/**
	 * <p>setEventPathOutage</p>
	 *
	 * @param eventpathoutage a {@link java.lang.String} object.
	 */
	public void setEventPathOutage(String eventpathoutage) {
		eventPathOutage = eventpathoutage;
	}

	/**
	 * <p>getEventCorrelation</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="correlation")
	public String getEventCorrelation() {
		return eventCorrelation;
	}

	/**
	 * <p>setEventCorrelation</p>
	 *
	 * @param eventcorrelation a {@link java.lang.String} object.
	 */
	public void setEventCorrelation(String eventcorrelation) {
		eventCorrelation = eventcorrelation;
	}

	/**
	 * <p>getEventSuppressedCount</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@XmlElement(name="suppressedCount")
	public Integer getEventSuppressedCount() {
		return eventSuppressedCount;
	}

	/**
	 * <p>setEventSuppressedCount</p>
	 *
	 * @param eventsuppressedcount a {@link java.lang.Integer} object.
	 */
	public void setEventSuppressedCount(Integer eventsuppressedcount) {
		eventSuppressedCount = eventsuppressedcount;
	}

	/**
	 * <p>getEventOperInstruct</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="operatorInstructions")
	public String getEventOperInstruct() {
		return eventOperInstruct;
	}

	/**
	 * <p>setEventOperInstruct</p>
	 *
	 * @param eventoperinstruct a {@link java.lang.String} object.
	 */
	public void setEventOperInstruct(String eventoperinstruct) {
		eventOperInstruct = eventoperinstruct;
	}

	/**
	 * <p>getEventAutoAction</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="autoAction")
	public String getEventAutoAction() {
		return eventAutoAction;
	}

	/**
	 * <p>setEventAutoAction</p>
	 *
	 * @param eventautoaction a {@link java.lang.String} object.
	 */
	public void setEventAutoAction(String eventautoaction) {
		eventAutoAction = eventautoaction;
	}

	/**
	 * <p>getEventOperAction</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="operatorAction")
	public String getEventOperAction() {
		return eventOperAction;
	}

	/**
	 * <p>setEventOperAction</p>
	 *
	 * @param eventoperaction a {@link java.lang.String} object.
	 */
	public void setEventOperAction(String eventoperaction) {
		eventOperAction = eventoperaction;
	}

	/**
	 * <p>getEventOperActionMenuText</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="operationActionMenuText")
	public String getEventOperActionMenuText() {
		return eventOperActionMenuText;
	}

	/**
	 * <p>setEventOperActionMenuText</p>
	 *
	 * @param eventOperActionMenuText a {@link java.lang.String} object.
	 */
	public void setEventOperActionMenuText(String eventOperActionMenuText) {
		this.eventOperActionMenuText = eventOperActionMenuText;
	}

	/**
	 * <p>getEventNotification</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="notification")
	public String getEventNotification() {
		return eventNotification;
	}

	/**
	 * <p>setEventNotification</p>
	 *
	 * @param eventnotification a {@link java.lang.String} object.
	 */
	public void setEventNotification(String eventnotification) {
		eventNotification = eventnotification;
	}

	/**
	 * <p>getEventTTicket</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="troubleTicket")
	public String getEventTTicket() {
		return eventTTicket;
	}

	/**
	 * <p>setEventTTicket</p>
	 *
	 * @param eventtticket a {@link java.lang.String} object.
	 */
	public void setEventTTicket(String eventtticket) {
		eventTTicket = eventtticket;
	}

	/**
	 * <p>getEventTTicketState</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@XmlElement(name="troubleTicketState")
	public Integer getEventTTicketState() {
		return eventTTicketState;
	}

	/**
	 * <p>setEventTTicketState</p>
	 *
	 * @param eventtticketstate a {@link java.lang.Integer} object.
	 */
	public void setEventTTicketState(Integer eventtticketstate) {
		eventTTicketState = eventtticketstate;
	}

	/**
	 * <p>getEventForward</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlTransient
	public String getEventForward() {
		return eventForward;
	}

	/**
	 * <p>setEventForward</p>
	 *
	 * @param eventforward a {@link java.lang.String} object.
	 */
	public void setEventForward(String eventforward) {
		eventForward = eventforward;
	}

	/**
	 * <p>getEventMouseOverText</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="mouseOverText")
	public String getEventMouseOverText() {
		return eventMouseOverText;
	}

	/**
	 * <p>setEventMouseOverText</p>
	 *
	 * @param eventmouseovertext a {@link java.lang.String} object.
	 */
	public void setEventMouseOverText(String eventmouseovertext) {
		eventMouseOverText = eventmouseovertext;
	}

	/**
	 * TODO: Make this an Enum
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="log")
	public String getEventLog() {
		return eventLog;
	}

	/**
	 * <p>setEventLog</p>
	 *
	 * @param eventlog a {@link java.lang.String} object.
	 */
	public void setEventLog(String eventlog) {
		eventLog = eventlog;
	}

	/**
	 * TODO: make this an Enum
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="display")
	public String getEventDisplay() {
		return eventDisplay;
	}

	/**
	 * <p>setEventDisplay</p>
	 *
	 * @param eventdisplay a {@link java.lang.String} object.
	 */
	public void setEventDisplay(String eventdisplay) {
		eventDisplay = eventdisplay;
	}

	/**
	 * <p>getEventAckUser</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlElement(name="ackUser")
	public String getEventAckUser() {
		return eventAckUser;
	}

	/**
	 * <p>setEventAckUser</p>
	 *
	 * @param eventackuser a {@link java.lang.String} object.
	 */
	public void setEventAckUser(String eventackuser) {
		eventAckUser = eventackuser;
	}

	/**
	 * <p>getEventAckTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@XmlElement(name="ackTime")
	public Date getEventAckTime() {
		return eventAckTime;
	}

	/**
	 * <p>setEventAckTime</p>
	 *
	 * @param eventacktime a {@link java.util.Date} object.
	 */
	public void setEventAckTime(Date eventacktime) {
		eventAckTime = eventacktime;
	}

	/**
	 * <p>getAlarm</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsAlarm} object.
	 */
	@XmlTransient
	public OnmsAlarm getAlarm() {
		return alarm;
	}

	/**
	 * <p>setAlarm</p>
	 *
	 * @param alarm a {@link org.opennms.netmgt.model.OnmsAlarm} object.
	 */
	public void setAlarm(OnmsAlarm alarm) {
		this.alarm = alarm;
	}

	/**
	 * <p>getNode</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
	 */
	@XmlTransient
	@JsonIgnore
	public OnmsNode getNode() {
		return node;
	}

    @XmlElement(name="nodeId")
    public Integer getNodeId() {
        try {
            return node != null ? node.getId() : null;
        } catch (ObjectNotFoundException e) {
            return null;
        }
    }

    @XmlElement(name="nodeLabel", required=false)
    public String getNodeLabel() {
        try{
            if (node == null) return null;
            return node.getLabel();
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
		this.node = node;
	}

	/**
	 * <p>getNotifications</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	@XmlTransient
	public Set<OnmsNotification> getNotifications() {
		return notifications;
	}

	/**
	 * <p>setNotifications</p>
	 *
	 * @param notifications a {@link java.util.Set} object.
	 */
	public void setNotifications(Set<OnmsNotification> notifications) {
		this.notifications = notifications;
	}

	/**
	 * <p>getAssociatedServiceRegainedOutages</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	@XmlTransient
	public Set<OnmsOutage> getAssociatedServiceRegainedOutages() {
		return associatedServiceRegainedOutages;
	}

	/**
	 * <p>setAssociatedServiceRegainedOutages</p>
	 *
	 * @param outagesBySvcregainedeventid a {@link java.util.Set} object.
	 */
	public void setAssociatedServiceRegainedOutages(Set<OnmsOutage> outagesBySvcregainedeventid) {
		associatedServiceRegainedOutages = outagesBySvcregainedeventid;
	}

	/**
	 * <p>getAssociatedServiceLostOutages</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	@XmlTransient
	public Set<OnmsOutage> getAssociatedServiceLostOutages() {
		return associatedServiceLostOutages;
	}

	/**
	 * <p>setAssociatedServiceLostOutages</p>
	 *
	 * @param outagesBySvclosteventid a {@link java.util.Set} object.
	 */
	public void setAssociatedServiceLostOutages(Set<OnmsOutage> outagesBySvclosteventid) {
		associatedServiceLostOutages = outagesBySvclosteventid;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
            return MoreObjects.toStringHelper(this).add("eventid", getId())
		        .add("eventuei", getEventUei())
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
	    public Integer getIfIndex() {
	        return ifIndex;
	    }

	    /**
	     * <p>setIfIndex</p>
	     *
	     * @param ifIndex a {@link java.lang.Integer} object.
	     */
	    public void setIfIndex(Integer ifIndex) {
	        this.ifIndex = ifIndex;
	    }
}
