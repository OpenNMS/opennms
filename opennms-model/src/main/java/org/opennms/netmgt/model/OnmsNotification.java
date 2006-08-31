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
import java.util.Date;
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

import org.springframework.core.style.ToStringCreator;


@Entity
@Table(name="notifications")
public class OnmsNotification extends OnmsEntity implements Serializable {

    /**
     * 
     */
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
    private OnmsIpInterface m_interface;

    /** nullable persistent field */
    private OnmsServiceType m_serviceType;

    /** nullable persistent field */
    private String m_queueId;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsEvent m_event;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsNode m_node;

    /** persistent field */
    private Set m_usersNotified;

    /** full constructor */
    public OnmsNotification(Integer notifyId, String textMsg, String subject, String numericMsg, Date pageTime, Date respondTime, String answeredBy, OnmsIpInterface ipInterface, OnmsServiceType serviceType, String queueId, org.opennms.netmgt.model.OnmsEvent event, org.opennms.netmgt.model.OnmsNode node, Set usersNotified) {
        m_notifyId = notifyId;
        m_textMsg = textMsg;
        m_subject = subject;
        m_numericMsg = numericMsg;
        m_pageTime = pageTime;
        m_respondTime = respondTime;
        m_answeredBy = answeredBy;
        m_interface = ipInterface;
        m_serviceType = serviceType;
        m_queueId = queueId;
        m_event = event;
        m_node = node;
        m_usersNotified = usersNotified;
    }

    /** default constructor */
    public OnmsNotification() {
    }

    /** minimal constructor */
    public OnmsNotification(Integer notifyId, String textMsg, org.opennms.netmgt.model.OnmsEvent event, org.opennms.netmgt.model.OnmsNode node, Set usersNotified) {
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

    /** 
     *            @hibernate.property
     *             column="pagetime"
     *             length="8"
     *         
     */
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

    @ManyToOne
    @JoinColumn(name="ipInterfaceId")
    public OnmsIpInterface getInterface() {
        return m_interface;
    }

    public void setInterface(OnmsIpInterface interfaceId) {
        m_interface = interfaceId;
    }

    
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


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="eventId", nullable=false)
    public OnmsEvent getEvent() {
        return m_event;
    }

    public void setEvent(org.opennms.netmgt.model.OnmsEvent event) {
        m_event = event;
    }


    @ManyToOne
    @JoinColumn(name="nodeId", nullable=false)
    public org.opennms.netmgt.model.OnmsNode getNode() {
        return m_node;
    }

    public void setNode(org.opennms.netmgt.model.OnmsNode node) {
        m_node = node;
    }

    /** 
     *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.key
     *             column="notifyid"
     *            @hibernate.one-to-many
     *             class="org.opennms.netmgt.model.OnmsUserNotification"
     *             
     * old XDoclet1 Tags
     *            hibernate.collection-key
     *             column="notifyid"
     *            hibernate.collection-one-to-many
     *             class="org.opennms.netmgt.model.OnmsUserNotification"
     *         
     */
    @OneToMany(fetch=FetchType.LAZY)
    @JoinColumn(name="notifyId")
    public Set getUsersNotified() {
        return m_usersNotified;
    }

    public void setUsersNotified(Set usersnotifieds) {
        m_usersNotified = usersnotifieds;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("notifyid", getNotifyId())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		throw new RuntimeException("visitor method not implemented");
	}

}
