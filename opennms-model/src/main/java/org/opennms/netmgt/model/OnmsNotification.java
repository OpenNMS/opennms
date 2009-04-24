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
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Filter;
import org.springframework.core.style.ToStringCreator;


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

	private String m_ipAddress;
    
    /**
     * persistent field representing the name of the configured notification from
     * notifications.xml
     */
    private String m_notifConfigName;

    /** full constructor */
    public OnmsNotification(Integer notifyId, String textMsg, String subject, String numericMsg, Date pageTime, Date respondTime, String answeredBy, String ipAddress, OnmsServiceType serviceType, String queueId, OnmsEvent event, OnmsNode node, Set<OnmsUserNotification> usersNotified, String notifConfigName) {
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

    /** default constructor */
    public OnmsNotification() {
    }

    /** minimal constructor */
    public OnmsNotification(Integer notifyId, String textMsg, OnmsEvent event, OnmsNode node, Set<OnmsUserNotification> usersNotified) {
        m_notifyId = notifyId;
        m_textMsg = textMsg;
        m_event = event;
        m_node = node;
        m_usersNotified = usersNotified;
    }

    
    @Id
    @SequenceGenerator(name="notifySequence", sequenceName="notifyNxtId")
    @GeneratedValue(generator="notifySequence")
    public Integer getNotifyId() {
        return m_notifyId;
    }

    public void setNotifyId(Integer notifyid) {
        m_notifyId = notifyid;
    }

    
    @Column(name="textMsg", length=4000, nullable=false)
    public String getTextMsg() {
        return m_textMsg;
    }

    public void setTextMsg(String textmsg) {
        m_textMsg = textmsg;
    }
    

    @Column(name="subject", length=256)
    public String getSubject() {
        return m_subject;
    }

    public void setSubject(String subject) {
        m_subject = subject;
    }
    

    @Column(name="numericMsg", length=256)
    public String getNumericMsg() {
        return m_numericMsg;
    }

    public void setNumericMsg(String numericmsg) {
        m_numericMsg = numericmsg;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="pageTime")
    public Date getPageTime() {
        return m_pageTime;
    }

    public void setPageTime(Date pagetime) {
        m_pageTime = pagetime;
    }

    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="respondTime")
    public Date getRespondTime() {
        return m_respondTime;
    }

    public void setRespondTime(Date respondtime) {
        m_respondTime = respondtime;
    }

    
    @Column(name="answeredBy", length=256)
    public String getAnsweredBy() {
        return m_answeredBy;
    }

    public void setAnsweredBy(String answeredby) {
        m_answeredBy = answeredby;
    }
    
    @Column(name="interfaceId", length=16)
    public String getIpAddress() {
    	return m_ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
    	m_ipAddress = ipAddress;
    }

    @XmlTransient
    @ManyToOne
    @JoinColumn(name="serviceId")
    public OnmsServiceType getServiceType() {
        return m_serviceType;
    }

    public void setServiceType(OnmsServiceType serviceType) {
        m_serviceType = serviceType;
    }


    @Column(name="queueId", length=256)
    public String getQueueId() {
        return m_queueId;
    }

    public void setQueueId(String queueid) {
        m_queueId = queueid;
    }


    @XmlTransient
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="eventId", nullable=false)
    public OnmsEvent getEvent() {
        return m_event;
    }

    public void setEvent(OnmsEvent event) {
        m_event = event;
    }
    
    /*
     * FIXME: HACK for some reason we put the eventUEI in the notificatinos table along with the eventId
     * so we have to HACK this so we can properly write the table
     */
    @Column(name="eventUEI")
    public String getEventUei() {
        return m_event.getEventUei();
    }
    
    public void setEventUei(String eventUei) {
        // do nothing as this is a HACK
    }

    @XmlTransient
    @ManyToOne
    @JoinColumn(name="nodeId")
    public OnmsNode getNode() {
        return m_node;
    }

    public void setNode(OnmsNode node) {
        m_node = node;
    }

    @XmlTransient
    @OneToMany(mappedBy="notification", fetch=FetchType.LAZY)
    public Set<OnmsUserNotification> getUsersNotified() {
        return m_usersNotified;
    }

    public void setUsersNotified(Set<OnmsUserNotification> usersnotifieds) {
        m_usersNotified = usersnotifieds;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("notifyid", getNotifyId())
            .toString();
    }

    public String getNotifConfigName() {
        return m_notifConfigName;
    }

    @Column(name="notifConfigName", length=63 )
    public void setNotifConfigName(String notifConfigName) {
        m_notifConfigName = notifConfigName;
    }

    public void acknowledge(String ackUser) {
        if(m_answeredBy == null || m_respondTime == null) {
            m_answeredBy = ackUser;
            m_respondTime = Calendar.getInstance().getTime();
        }
    }
    
    @Transient
    public AckType getType() {
        return AckType.NOTIFICATION;
    }
    
    @Transient
    public Integer getAckId() {
        return m_notifyId;
    }
    
    @Transient
    public String getAckUser() {
        return m_answeredBy;
    }
    
    @Transient
    public Date getAckTime() {
        return m_respondTime;
    }

    public void clear(String ackUser) {
        /* Note: this currently works based on the way Notifd currently processes queued notifications.
         * Outstanding notifications are not removed from the queue when a response is received, instead,
         * when the queued notification task is ran, the task checks to see if the notice is still outstanding.
         */
        m_respondTime = Calendar.getInstance().getTime();
        m_answeredBy = ackUser;
    }

    public void escalate(String ackUser) {
        //does nothing for there is no severity state in a notification object
        //escalation of a notification is handled in the notification path
    }

    public void unacknowledge(String ackUser) {
        m_respondTime = null;
        m_answeredBy = null;
    }

}
