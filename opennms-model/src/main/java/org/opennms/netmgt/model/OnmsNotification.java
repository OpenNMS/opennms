/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.core.xml.bind.InetAddressXmlAdapter;
import org.springframework.core.style.ToStringCreator;


/**
 * <p>OnmsNotification class.</p>
 */
@XmlRootElement(name="notification")
@Entity
@Table(name="notifications")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public class OnmsNotification implements Acknowledgeable, Serializable {

    private static final long serialVersionUID = -1162549324168290004L;

    /** identifier field */
    private Integer m_notifyId;

    /** persistent field */
    private String m_textMsg;

    /** nullable persistent field */
    private String m_subject;

    /** nullable persistent field */
    private String m_numericMsg;

    /** nullable persistent field */
    private Date m_pageTime;

    /** nullable persistent field */
    private Date m_respondTime; 

    /** nullable persistent field */
    private String m_answeredBy;

    /** nullable persistent field */
    private OnmsServiceType m_serviceType;

    /** nullable persistent field */
    private String m_queueId;

    /** persistent field */
    private OnmsEvent m_event;

    /** persistent field */
    private OnmsNode m_node;

    /** persistent field */
    private Set<OnmsUserNotification> m_usersNotified = new HashSet<OnmsUserNotification>();

	private InetAddress m_ipAddress;
    
    /**
     * persistent field representing the name of the configured notification from
     * notifications.xml
     */
    private String m_notifConfigName;

    /**
     * full constructor
     *
     * @param notifyId a {@link java.lang.Integer} object.
     * @param textMsg a {@link java.lang.String} object.
     * @param subject a {@link java.lang.String} object.
     * @param numericMsg a {@link java.lang.String} object.
     * @param pageTime a {@link java.util.Date} object.
     * @param respondTime a {@link java.util.Date} object.
     * @param answeredBy a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceType a {@link org.opennms.netmgt.model.OnmsServiceType} object.
     * @param queueId a {@link java.lang.String} object.
     * @param event a {@link org.opennms.netmgt.model.OnmsEvent} object.
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @param usersNotified a {@link java.util.Set} object.
     * @param notifConfigName a {@link java.lang.String} object.
     */
    public OnmsNotification(Integer notifyId, String textMsg, String subject, String numericMsg, Date pageTime, Date respondTime, String answeredBy, InetAddress ipAddress, OnmsServiceType serviceType, String queueId, OnmsEvent event, OnmsNode node, Set<OnmsUserNotification> usersNotified, String notifConfigName) {
        m_notifyId = notifyId;
        m_textMsg = textMsg;
        m_subject = subject;
        m_numericMsg = numericMsg;
        m_pageTime = pageTime;
        m_respondTime = respondTime;
        m_answeredBy = answeredBy;
        m_ipAddress = ipAddress;
        m_serviceType = serviceType;
        m_queueId = queueId;
        m_event = event;
        m_node = node;
        m_usersNotified = usersNotified;
        m_notifConfigName = notifConfigName;
    }

    /**
     * default constructor
     */
    public OnmsNotification() {
    }

    /**
     * minimal constructor
     *
     * @param notifyId a {@link java.lang.Integer} object.
     * @param textMsg a {@link java.lang.String} object.
     * @param event a {@link org.opennms.netmgt.model.OnmsEvent} object.
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @param usersNotified a {@link java.util.Set} object.
     */
    public OnmsNotification(Integer notifyId, String textMsg, OnmsEvent event, OnmsNode node, Set<OnmsUserNotification> usersNotified) {
        m_notifyId = notifyId;
        m_textMsg = textMsg;
        m_event = event;
        m_node = node;
        m_usersNotified = usersNotified;
    }

    
    /**
     * <p>getNotifyId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable=false)
    @XmlAttribute(name="id")
    @SequenceGenerator(name="notifySequence", sequenceName="notifyNxtId")
    @GeneratedValue(generator="notifySequence")
    public Integer getNotifyId() {
        return m_notifyId;
    }

    /**
     * <p>setNotifyId</p>
     *
     * @param notifyid a {@link java.lang.Integer} object.
     */
    public void setNotifyId(Integer notifyid) {
        m_notifyId = notifyid;
    }


    /**
     * <p>getTextMsg</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="textMessage")
    @Column(name="textMsg", length=4000, nullable=false)
    public String getTextMsg() {
        return m_textMsg;
    }

    /**
     * <p>setTextMsg</p>
     *
     * @param textmsg a {@link java.lang.String} object.
     */
    public void setTextMsg(String textmsg) {
        m_textMsg = textmsg;
    }
    
    /**
     * <p>getSubject</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="subject")
    @Column(name="subject", length=256)
    public String getSubject() {
        return m_subject;
    }

    /**
     * <p>setSubject</p>
     *
     * @param subject a {@link java.lang.String} object.
     */
    public void setSubject(String subject) {
        m_subject = subject;
    }
    
    /**
     * <p>getNumericMsg</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="numericMessage")
    @Column(name="numericMsg", length=256)
    public String getNumericMsg() {
        return m_numericMsg;
    }

    /**
     * <p>setNumericMsg</p>
     *
     * @param numericmsg a {@link java.lang.String} object.
     */
    public void setNumericMsg(String numericmsg) {
        m_numericMsg = numericmsg;
    }

    /**
     * <p>getPageTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @XmlElement(name="pageTime")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="pageTime")
    public Date getPageTime() {
        return m_pageTime;
    }

    /**
     * <p>setPageTime</p>
     *
     * @param pagetime a {@link java.util.Date} object.
     */
    public void setPageTime(Date pagetime) {
        m_pageTime = pagetime;
    }

    /**
     * <p>getRespondTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @XmlElement(name="respondTime")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="respondTime")
    public Date getRespondTime() {
        return m_respondTime;
    }

    /**
     * <p>setRespondTime</p>
     *
     * @param respondtime a {@link java.util.Date} object.
     */
    public void setRespondTime(Date respondtime) {
        m_respondTime = respondtime;
    }

    /**
     * <p>getAnsweredBy</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="answeredBy")
    @Column(name="answeredBy", length=256)
    public String getAnsweredBy() {
        return m_answeredBy;
    }

    /**
     * <p>setAnsweredBy</p>
     *
     * @param answeredby a {@link java.lang.String} object.
     */
    public void setAnsweredBy(String answeredby) {
        m_answeredBy = answeredby;
    }
    
    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="interfaceId")
    @XmlElement(name="ipAddress")
    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getIpAddress() {
    	return m_ipAddress;
    }
    
    /**
     * <p>setIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public void setIpAddress(InetAddress ipAddress) {
    	m_ipAddress = ipAddress;
    }

    /**
     * <p>getServiceType</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsServiceType} object.
     */
    @ManyToOne
    @JoinColumn(name="serviceId")
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
     * <p>getQueueId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="queueId", length=256)
    public String getQueueId() {
        return m_queueId;
    }

    /**
     * <p>setQueueId</p>
     *
     * @param queueid a {@link java.lang.String} object.
     */
    public void setQueueId(String queueid) {
        m_queueId = queueid;
    }

    /**
     * <p>getEvent</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    @XmlTransient
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="eventId", nullable=false)
    public OnmsEvent getEvent() {
        return m_event;
    }

    /**
     * <p>setEvent</p>
     *
     * @param event a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    public void setEvent(OnmsEvent event) {
        m_event = event;
    }
    
    /*
     * FIXME: HACK for some reason we put the eventUEI in the notifications table along with the eventId
     * so we have to HACK this so we can properly write the table
     */
    /**
     * <p>getEventUei</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="uei")
    @Column(name="eventUEI")
    public String getEventUei() {
        return m_event.getEventUei();
    }
    
    /**
     * <p>setEventUei</p>
     *
     * @param eventUei a {@link java.lang.String} object.
     */
    public void setEventUei(String eventUei) {
        // do nothing as this is a HACK
    }

    /**
     * <p>getSeverityLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    @XmlAttribute(name="severity")
    public String getSeverityLabel() {
        return m_event.getSeverityLabel();
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @XmlTransient
    @ManyToOne
    @JoinColumn(name="nodeId")
    @Override
    public OnmsNode getNode() {
        return m_node;
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
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }

    /**
     * <p>getUsersNotified</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @XmlElement(name="destination")
    @XmlElementWrapper(name="destinations")
    @OneToMany(mappedBy="notification", fetch=FetchType.LAZY)
    public Set<OnmsUserNotification> getUsersNotified() {
        return m_usersNotified;
    }

    /**
     * <p>setUsersNotified</p>
     *
     * @param usersnotifieds a {@link java.util.Set} object.
     */
    public void setUsersNotified(Set<OnmsUserNotification> usersnotifieds) {
        m_usersNotified = usersnotifieds;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("notifyid", getNotifyId())
            .toString();
    }

    /**
     * <p>getNotifConfigName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNotifConfigName() {
        return m_notifConfigName;
    }

    /**
     * <p>setNotifConfigName</p>
     *
     * @param notifConfigName a {@link java.lang.String} object.
     */
    @XmlElement(name="notificationName")
    @Column(name="notifConfigName", length=63 )
    public void setNotifConfigName(String notifConfigName) {
        m_notifConfigName = notifConfigName;
    }

    /** {@inheritDoc} */
    @Override
    public void acknowledge(String ackUser) {
        if(m_answeredBy == null || m_respondTime == null) {
            m_answeredBy = ackUser;
            m_respondTime = Calendar.getInstance().getTime();
        }
    }
    
    /**
     * <p>getType</p>
     *
     * @return a {@link org.opennms.netmgt.model.AckType} object.
     */
    @Transient
    @Override
    public AckType getType() {
        return AckType.NOTIFICATION;
    }
    
    /**
     * <p>getAckId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Transient
    @Override
    public Integer getAckId() {
        return m_notifyId;
    }
    
    /**
     * <p>getAckUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    @Override
    public String getAckUser() {
        return m_answeredBy;
    }
    
    /**
     * <p>getAckTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Transient
    @Override
    public Date getAckTime() {
        return m_respondTime;
    }

    /** {@inheritDoc} */
    @Override
    public void clear(String ackUser) {
        /* Note: this currently works based on the way Notifd currently processes queued notifications.
         * Outstanding notifications are not removed from the queue when a response is received, instead,
         * when the queued notification task is ran, the task checks to see if the notice is still outstanding.
         */
        m_respondTime = Calendar.getInstance().getTime();
        m_answeredBy = ackUser;
    }

    /** {@inheritDoc} */
    @Override
    public void escalate(String ackUser) {
        //does nothing for there is no severity state in a notification object
        //escalation of a notification is handled in the notification path
    }

    /** {@inheritDoc} */
    @Override
    public void unacknowledge(String ackUser) {
        m_respondTime = null;
        m_answeredBy = null;
    }

}
