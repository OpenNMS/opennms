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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.core.network.InetAddressXmlAdapter;
import com.google.common.base.MoreObjects;

/**
 * <p>OnmsAlarm class.</p>
 */
@XmlRootElement(name="alarm")
@Entity
@Table(name="alarms")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OnmsAlarm implements Acknowledgeable, Serializable {
    private static final long serialVersionUID = 7275548439687562161L;
    
    /** Constant <code>PROBLEM_TYPE=1</code> */
    public static final int PROBLEM_TYPE = 1;
    
    /** Constant <code>RESOLUTION_TYPE=2</code> */
    public static final int RESOLUTION_TYPE = 2;

    /** Constant <code>PROBLEM_WITHOUT_RESOLUTION_TYPE=3</code> */
    public static final int PROBLEM_WITHOUT_RESOLUTION_TYPE = 3;

    public static final String ARCHIVED = "Archived";

    /** identifier field */
    private Integer m_id;

    /** persistent field */
    private String m_uei;

    /** persistent field */
    private OnmsMonitoringSystem m_distPoller;

    /** nullable persistent field */
    private OnmsNode m_node;

    /** nullable persistent field */
    private InetAddress m_ipAddr;

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
    private OnmsSeverity m_severity = OnmsSeverity.INDETERMINATE;

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
    private String m_clearKey;

    /** persistent field */
    private OnmsEvent m_lastEvent;
    
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

    private OnmsMemo m_stickyMemo;
    
    private OnmsReductionKeyMemo m_reductionKeyMemo;

    private Set<AlarmAssociation> m_associatedAlarms = new HashSet<>();

    private Set<OnmsAlarm> m_relatedSituations = new HashSet<>();

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
        this.m_severity = OnmsSeverity.get(severity);
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
    @Column(name="alarmId", nullable=false)
    @XmlAttribute(name="id")
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
    @XmlElement(name="uei")
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
	@XmlTransient
    @ManyToOne
    @JoinColumn(name="systemId", nullable=false)
    public OnmsMonitoringSystem getDistPoller() {
        return this.m_distPoller;
    }

    /**
     * <p>setDistPoller</p>
     *
     * @param distPoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
     */
    public void setDistPoller(OnmsMonitoringSystem distPoller) {
        this.m_distPoller = distPoller;
    }

    // TODO change this to an Entity and remove nodeid, ipaddr, serviceid
	/**
	 * <p>getNode</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
	 */
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    @Override
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

    @Transient
    @XmlElement(name="nodeId", required=false)
    public Integer getNodeId() {
        if (m_node == null) return null;
        return m_node.getId();
    }

    @Transient
    @XmlElement(name="nodeLabel", required=false)
    public String getNodeLabel() {
        if (m_node == null) return null;
        return m_node.getLabel();
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
        return this.m_ipAddr;
    }

    /**
     * <p>setIpAddr</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     */
    public void setIpAddr(InetAddress ipaddr) {
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
    @Column(name="reductionKey", unique=true)
    @XmlElement(name="reductionKey")
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
    @XmlAttribute(name="type")
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
    @XmlAttribute(name="count")
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
     * <p>getSeverityLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    @XmlAttribute(name="severity")
    public String getSeverityLabel() {
        return this.m_severity.name();
    }

    /**
     * <p>setSeverityLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setSeverityLabel(final String label) {
        m_severity = OnmsSeverity.get(label);
    }
    
    /**
     * <p>getSeverity</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    @Override
    @Column(name="severity", nullable=false)
    // @Enumerated(EnumType.ORDINAL)
    @Type(type="org.opennms.netmgt.model.OnmsSeverityUserType")
    @XmlTransient
    public OnmsSeverity getSeverity() {
        return this.m_severity;
    }

    /**
     * <p>setSeverity</p>
     *
     * @param severity a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    public void setSeverity(final OnmsSeverity severity) {
        m_severity = severity;
    }
    
    /**
     * <p>getSeverityId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Transient
    @XmlTransient
    public Integer getSeverityId() {
        return this.m_severity.getId();
    }

    /**
     * <p>setSeverityId</p>
     *
     * @param severity a {@link java.lang.Integer} object.
     */
    public void setSeverityId(final Integer severity) {
        this.m_severity = OnmsSeverity.get(severity);
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
    @Column(name="logmsg", length=1024)
    @XmlElement(name="logMessage")
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
    @XmlElement(name="operatorInstructions")
    @Column(name="operinstruct")
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
    @XmlElement(name="troubleTicket")
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
    @XmlElement(name="troubleTicketState")
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
    @XmlElement(name="mouseOverText")
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
    @XmlElement(name="suppressedUntil")
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
    @XmlElement(name="suppressedBy")
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
    @XmlElement(name="suppressedTime")
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
    @XmlElement(name="ackUser")
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

    @Transient
    @XmlTransient
    public boolean isAcknowledged() {
        return getAlarmAckUser() != null;
    }

    /**
     * <p>getAlarmAckTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="alarmAckTime")
    @XmlElement(name="ackTime")
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
     * <p>getClearKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="clearKey")
    @Column(name="clearKey")
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
    @XmlElement(name="lastEvent")
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
        if (event!=null) {
            try {
                this.m_lastEventTime = event.getEventTime(); // alarm can be saved with no associated event
            } catch (final ObjectNotFoundException e) {
                // ignore errors getting this event from the DB
            }
        }
    }

    @Transient
    @XmlElementWrapper(name="parameters")
    @XmlElement(name="parameter")
    public List<OnmsEventParameter> getEventParameters() {
        return m_lastEvent != null ? m_lastEvent.getEventParameters() : null;
    }

    public Optional<OnmsEventParameter> findEventParameter(final String name) {
        return this.getEventParameters().stream().filter(p -> Objects.equals(name, p.getName())).findAny();
    }

    public String getEventParameter(final String name) {
        return this.getEventParameters().stream().filter(p -> Objects.equals(name, p.getName())).findAny().map(OnmsEventParameter::getValue).orElse(null);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("alarmid", getId())
            .add("distPoller", getDistPoller())
            .add("uei", getUei())
            .add("severity", getSeverity())
            .add("lastEventTime",getLastEventTime())
            .toString();
    }

    /**
     * <p>getLastEventTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastEventTime")
    @XmlElement(name="lastEventTime")
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
    @XmlElement(name="applicationDN")
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
    @XmlElement(name="firstAutomationTime")
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
    @XmlElement(name="lastAutomationTime")
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
    @XmlTransient
    @ElementCollection
    @JoinTable(name="alarm_attributes", joinColumns = @JoinColumn(name="alarmId"))
    @MapKeyColumn(name="attributename")
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

    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name="ifIndex")
    @XmlAttribute(name="ifIndex")
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
    
    @ManyToOne
    @JoinColumn(name="reductionKey", referencedColumnName="reductionkey", updatable=false, insertable=false)
    @XmlElement(name="reductionKeyMemo")
    public OnmsReductionKeyMemo getReductionKeyMemo() {
        return m_reductionKeyMemo;
    }

    public void setReductionKeyMemo(OnmsReductionKeyMemo reductionKeyMemo) {
        this.m_reductionKeyMemo = reductionKeyMemo;
    }

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="stickymemo")
    @XmlElement(name="stickyMemo")
    public OnmsMemo getStickyMemo() {
        return m_stickyMemo;
    }

    public void setStickyMemo(OnmsMemo stickyMemo) {
        this.m_stickyMemo = stickyMemo;
    }
    
    /** {@inheritDoc} */
    @Override
    public void acknowledge(String user) {
        if (m_alarmAckTime == null || m_alarmAckUser == null) {
            m_alarmAckTime = Calendar.getInstance().getTime();
            m_alarmAckUser = user;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unacknowledge(String ackUser) {
        m_alarmAckTime = null;
        m_alarmAckUser = null;
    }
    
    /** {@inheritDoc} */
    @Override
    public void clear(String ackUser) {
        m_severity = OnmsSeverity.CLEARED;
    }
    
    /** {@inheritDoc} */
    @Override
    public void escalate(String ackUser) {
        m_severity = OnmsSeverity.escalate(m_severity);
//        m_alarmAckUser = ackUser;
//        m_alarmAckTime = Calendar.getInstance().getTime();
    }

    /**
     * This marks an alarm as archived and prevents it from being used again in during reduction.
     */
    public void archive() {
        m_qosAlarmState = ARCHIVED;
        m_severity = OnmsSeverity.CLEARED;
        m_reductionKey = getReductionKey() + ":ID:"+ getId();
    }

    // Alarms that are archived
    @Transient
    @XmlTransient
    public boolean isArchived() {
        return ARCHIVED.equals(m_qosAlarmState);
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link org.opennms.netmgt.model.AckType} object.
     */
    @Transient
    @Override
    public AckType getType() {
        return AckType.ALARM;
    }
    
    /**
     * <p>getAckId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Transient
    @Override
    public Integer getAckId() {
        return m_id;
    }
    
    /**
     * <p>getAckUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    @Override
    public String getAckUser() {
        return m_alarmAckUser;
    }
    
    /**
     * <p>getAckTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Transient
    @Override
    public Date getAckTime() {
        return m_alarmAckTime;
    }

    /**
     * <p>getRelatedAlarms</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @Transient
    @XmlTransient
    public Set<OnmsAlarm> getRelatedAlarms() {
        return m_associatedAlarms.stream().map(AlarmAssociation::getRelatedAlarm).collect(Collectors.toSet());
    }

    @Transient
    @XmlTransient
    public Set<Integer> getRelatedAlarmIds() {
        return getRelatedAlarms().stream()
                .map(OnmsAlarm::getId)
                .collect(Collectors.toSet());
    }

    @XmlTransient
    @OneToMany(mappedBy = "situationAlarm", cascade = CascadeType.ALL)
    public Set<AlarmAssociation> getAssociatedAlarms() {
        return m_associatedAlarms;
    }

    public void setAssociatedAlarms(Set<AlarmAssociation> alarms) {
        m_associatedAlarms = alarms;
    }

    public void setRelatedAlarms(Set<OnmsAlarm> alarms) {
        m_associatedAlarms.clear();
        alarms.forEach(relatedAlarm -> m_associatedAlarms.add(new AlarmAssociation(this, relatedAlarm)));
    }

    public void setRelatedAlarms(Set<OnmsAlarm> alarms, Date associationEventTime) {
        m_associatedAlarms.clear();
        alarms.forEach(relatedAlarm -> m_associatedAlarms.add(new AlarmAssociation(this, relatedAlarm, associationEventTime)));
    }

    public void addRelatedAlarm(OnmsAlarm alarm) {
        m_associatedAlarms.add(new AlarmAssociation(this, alarm));
    }

    public void removeRelatedAlarm(OnmsAlarm alarm) {
        m_associatedAlarms.removeIf(associatedAlarm -> associatedAlarm.getRelatedAlarm().getId().equals(alarm.getId()));
    }


    // Any alarm with related alarms is a 'Situation'
    @Transient
    @XmlTransient
    public boolean isSituation() {
        return !m_associatedAlarms.isEmpty();
    }

    @XmlTransient
    @ElementCollection
    @JoinTable(name = "alarm_situations", joinColumns = @JoinColumn(name = "related_alarm_id"), inverseJoinColumns = @JoinColumn(name = "situation_id"))
    @Column(name="alarm_id", nullable=false)
    public Set<OnmsAlarm> getRelatedSituations() {
        return m_relatedSituations;
    }

    @Transient
    @XmlTransient
    public Set<Integer> getRelatedSituationIds() {
        return getRelatedSituations().stream()
                .map(OnmsAlarm::getId)
                .collect(Collectors.toSet());
    }

    @Transient
    @XmlTransient
    public boolean isPartOfSituation() {
        return !m_relatedSituations.isEmpty();
    }

    public void setRelatedSituations(Set<OnmsAlarm> alarms) {
        m_relatedSituations = alarms;
    }

    @Transient
    @XmlTransient
    public Integer getAffectedNodeCount() {
        if (m_associatedAlarms == null || m_associatedAlarms.isEmpty()) {
            return m_node == null ? 0 : 1;
        }
        Set<Integer> nodes = getRelatedAlarms().stream().map(OnmsAlarm::getNode).filter(Objects::nonNull).map(OnmsNode::getId).collect(Collectors.toSet());
        // count the Situtation's node if it is different
        if (m_node != null) {
            nodes.add(m_node.getId());
        }
        return nodes.size();
    }

    @Transient
    @XmlTransient
    public Date getLastUpdateTime() {
        if (getLastAutomationTime() != null && getLastAutomationTime().compareTo(getLastEventTime()) > 0) {
            return getLastAutomationTime();
        }
        return getLastEventTime();
    }

}
