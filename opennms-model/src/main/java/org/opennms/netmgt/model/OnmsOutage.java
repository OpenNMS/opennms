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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.core.xml.bind.InetAddressXmlAdapter;
import org.springframework.core.style.ToStringCreator;


/**
 * <p>OnmsOutage class.</p>
 *
 * @hibernate.class table="outages"
 */
@XmlRootElement(name="outage")
@Entity
@Table(name="outages")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public class OnmsOutage implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3846398168228820151L;

    /** identifier field */
    private Integer m_id;

    /** persistent field */
    private Date m_ifLostService;

    /** nullable persistent field */
    private Date m_ifRegainedService;

    /** persistent field */
    private OnmsEvent m_serviceRegainedEvent;

    /** persistent field */
    private OnmsEvent m_serviceLostEvent;

    /** persistent field */
    private OnmsMonitoredService m_monitoredService;
    
    /** persistent field */
    private Date m_suppressTime;
    
    /** persistent field */
    private String m_suppressedBy;
    
    /**
     * full constructor
     *
     * @param ifLostService a {@link java.util.Date} object.
     * @param ifRegainedService a {@link java.util.Date} object.
     * @param eventBySvcRegainedEvent a {@link org.opennms.netmgt.model.OnmsEvent} object.
     * @param eventBySvcLostEvent a {@link org.opennms.netmgt.model.OnmsEvent} object.
     * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @param suppressTime a {@link java.util.Date} object.
     * @param suppressedBy a {@link java.lang.String} object.
     */
    public OnmsOutage(Date ifLostService, Date ifRegainedService, OnmsEvent eventBySvcRegainedEvent, OnmsEvent eventBySvcLostEvent, OnmsMonitoredService monitoredService, Date suppressTime, String suppressedBy) {
        m_ifLostService = ifLostService;
        m_ifRegainedService = ifRegainedService;
        m_serviceRegainedEvent = eventBySvcRegainedEvent;
        m_serviceLostEvent = eventBySvcLostEvent;
        m_monitoredService = monitoredService;
        m_suppressTime = suppressTime;
        m_suppressedBy = suppressedBy;
        
    }

    /**
     * default constructor
     */
    public OnmsOutage() {
    }

    /**
     * minimal constructor
     *
     * @param ifLostService a {@link java.util.Date} object.
     * @param eventBySvcLostEvent a {@link org.opennms.netmgt.model.OnmsEvent} object.
     * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    public OnmsOutage(Date ifLostService, OnmsEvent eventBySvcLostEvent, OnmsMonitoredService monitoredService) {
        m_ifLostService = ifLostService;
        m_serviceLostEvent = eventBySvcLostEvent;
        m_monitoredService = monitoredService;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @XmlAttribute(name="id")
    @Column(name="outageId", nullable=false)
    @SequenceGenerator(name="outageSequence", sequenceName="outageNxtId")
    @GeneratedValue(generator="outageSequence")
    public Integer getId() {
        return m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param outageId a {@link java.lang.Integer} object.
     */
    public void setId(Integer outageId) {
        m_id = outageId;
    }

    // @XmlTransient
    /**
     * <p>getMonitoredService</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    @ManyToOne
    @JoinColumn(name="ifserviceId")
    public OnmsMonitoredService getMonitoredService() {
        return m_monitoredService;
    }

    /**
     * <p>setMonitoredService</p>
     *
     * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    public void setMonitoredService(OnmsMonitoredService monitoredService) {
        m_monitoredService = monitoredService;
    }

    
    /**
     * <p>getIfLostService</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ifLostService", nullable=false)
    public Date getIfLostService() {
        return m_ifLostService;
    }

    /**
     * <p>setIfLostService</p>
     *
     * @param ifLostService a {@link java.util.Date} object.
     */
    public void setIfLostService(Date ifLostService) {
        m_ifLostService = ifLostService;
    }

    // @XmlTransient
    /**
     * <p>getServiceLostEvent</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="svcLostEventId")
    public OnmsEvent getServiceLostEvent() {
        return m_serviceLostEvent;
    }

    /**
     * <p>setServiceLostEvent</p>
     *
     * @param svcLostEvent a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    public void setServiceLostEvent(OnmsEvent svcLostEvent) {
        m_serviceLostEvent = svcLostEvent;
    }


    /**
     * <p>getIfRegainedService</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ifRegainedService")
    public Date getIfRegainedService() {
        return m_ifRegainedService;
    }
    
    /**
     * <p>setIfRegainedService</p>
     *
     * @param ifRegainedService a {@link java.util.Date} object.
     */
    public void setIfRegainedService(Date ifRegainedService) {
        m_ifRegainedService = ifRegainedService;
    }

    // @XmlTransient
    /**
     * <p>getServiceRegainedEvent</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="svcRegainedEventId")
    public OnmsEvent getServiceRegainedEvent() {
        return m_serviceRegainedEvent;
    }

    /**
     * <p>setServiceRegainedEvent</p>
     *
     * @param svcRegainedEvent a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    public void setServiceRegainedEvent(OnmsEvent svcRegainedEvent) {
        m_serviceRegainedEvent = svcRegainedEvent;
    }

    /**
     * <p>getSuppressTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="suppressTime")
    public Date getSuppressTime(){
    	return m_suppressTime;
    }
    
    /**
     * <p>setSuppressTime</p>
     *
     * @param timeToSuppress a {@link java.util.Date} object.
     */
    public void setSuppressTime(Date timeToSuppress){
    	m_suppressTime = timeToSuppress;
    }
    
    
    /**
     * <p>getSuppressedBy</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="suppressedBy")
    public String getSuppressedBy(){
    	return m_suppressedBy;
    }
    
    /**
     * <p>setSuppressedBy</p>
     *
     * @param suppressorMan a {@link java.lang.String} object.
     */
    public void setSuppressedBy(String suppressorMan){
    	m_suppressedBy = suppressorMan;
    }
    
    
    /**
     * <p>getNodeId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Transient
    public Integer getNodeId(){
    	return getMonitoredService().getNodeId();
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    @XmlElement(name="ipAddress")
    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getIpAddress() {
        return getMonitoredService().getIpAddress();
    }

    /**
     * <p>getIpAddressAsString</p>
     *
     * @return a {@link java.lang.String} object.
     * @deprecated use getIpAddress
     */
    @Transient
    @XmlTransient
    public String getIpAddressAsString() {
        return getMonitoredService().getIpAddressAsString();
    }

    /**
     * <p>getServiceId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Transient
    public Integer getServiceId() {
    	return getMonitoredService().getServiceId();
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("outageId", m_id)
            .append("ifLostService", m_ifLostService)
            .append("ifRegainedService", m_ifRegainedService)
            .append("service", m_monitoredService)
            .append("suppressedBy", m_suppressedBy)
            .append("suppressTime", m_suppressTime)
            .toString();
    }

}
