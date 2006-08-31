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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.core.style.ToStringCreator;


/** 
 * @hibernate.class table="outages"
 *     
*/
@Entity
@Table(name="outages")
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
    private OnmsEvent m_eventBySvcRegainedEvent;

    /** persistent field */
    private OnmsEvent m_eventBySvcLostEvent;

    /** persistent field */
    private OnmsMonitoredService m_monitoredService;
    
    /** persistent field */
    private Date m_suppressTime;
    
    /** persistent field */
    private String m_suppressedBy;
    
    /** persistent field */
    private String m_ipaddr;
    
    /** persistent field */
    private Integer m_serviceId;
    
    /** persistent field */
    private Integer  m_nodeid;
    

    /** full constructor */
    public OnmsOutage(Date ifLostService, Date ifRegainedService, OnmsEvent eventBySvcRegainedEvent, OnmsEvent eventBySvcLostEvent, OnmsMonitoredService monitoredService, Date suppressTime, String suppressedBy, String ipaddr) {
        m_ifLostService = ifLostService;
        m_ifRegainedService = ifRegainedService;
        m_eventBySvcRegainedEvent = eventBySvcRegainedEvent;
        m_eventBySvcLostEvent = eventBySvcLostEvent;
        m_monitoredService = monitoredService;
        m_suppressTime = suppressTime;
        m_suppressedBy = suppressedBy;
        m_ipaddr = ipaddr;
        
    }

    /** default constructor */
    public OnmsOutage() {
    }

    /** minimal constructor */
    public OnmsOutage(Date ifLostService, OnmsEvent eventBySvcLostEvent, OnmsMonitoredService monitoredService) {
        m_ifLostService = ifLostService;
        m_eventBySvcLostEvent = eventBySvcLostEvent;
        m_monitoredService = monitoredService;
    }

    @Id
    @Column(name="outageId")
    @SequenceGenerator(name="outageSequence", sequenceName="outageNxtId")
    @GeneratedValue(generator="outageSequence")
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer outageId) {
        m_id = outageId;
    }


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ifLostService", nullable=false)
    public Date getIfLostService() {
        return m_ifLostService;
    }

    public void setIfLostService(Date ifLostService) {
        m_ifLostService = ifLostService;
    }


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ifRegainedService")
    public Date getIfRegainedService() {
        return m_ifRegainedService;
    }
    
    public void setIfRegainedService(Date ifRegainedService) {
        m_ifRegainedService = ifRegainedService;
    }

    
    @ManyToOne
    @JoinColumn(name="svcRegainedEventId")
    public OnmsEvent getEventBySvcRegainedEvent() {
        return m_eventBySvcRegainedEvent;
    }

    public void setEventBySvcRegainedEvent(OnmsEvent eventBySvcRegainedEvent) {
        m_eventBySvcRegainedEvent = eventBySvcRegainedEvent;
    }

    @ManyToOne
    @JoinColumn(name="svcLostEventId")
    public OnmsEvent getEventBySvcLostEvent() {
        return m_eventBySvcLostEvent;
    }

    public void setEventBySvcLostEvent(OnmsEvent eventBySvcLostEvent) {
        m_eventBySvcLostEvent = eventBySvcLostEvent;
    }


    @ManyToOne
    @JoinColumn(name="ifserviceId")
    public OnmsMonitoredService getMonitoredService() {
        return m_monitoredService;
    }

    public void setMonitoredService(OnmsMonitoredService monitoredService) {
        m_monitoredService = monitoredService;
    }

    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="suppressTime")
    public Date getSuppressTime(){
    	return m_suppressTime;
    }
    
    public void setSuppressTime(Date timeToSuppress){
    	m_suppressTime = timeToSuppress;
    }
    
    
    @Column(name="suppressedBy")
    public String getSuppressedBy(){
    	return m_suppressedBy;
    }
    
    public void setSuppressedBy(String suppressorMan){
    	m_suppressedBy = suppressorMan;
    }
    
    
    //TODO: This column should go away
    @Column(name="nodeid")
    public Integer getNodeId(){
        return m_nodeid;
    }
    
    public void setNodeId(Integer nodeid){
        m_nodeid = nodeid;
    }


    //TODO: This column should go away
    @Column(name="ipAddr", length=16)
    public String getIpAddress() {
    	 return m_ipaddr ;
    }
    
    public void setIpAddress(String ipAddr) {
        m_ipaddr = ipAddr;
        
    }

    
    //TODO: This column should go away
    @Column(name="serviceId")
    public Integer getServiceId() {
    	return m_serviceId ;
    }
    
    public void setServiceId(Integer serviceId) {
        m_serviceId = serviceId;
        
    }
    

    public String toString() {
        return new ToStringCreator(this)
            .append("outageId", getId())
            .append("ifLostService", getIfLostService())
            .append("ifRegainedService", getIfRegainedService())
            .append("suppressedBy", getSuppressedBy())
            .append("suppressTime", getSuppressTime())
            .append("ipAddr", getIpAddress())
            .append("serviceid", getServiceId())
            .append("nodeid",getNodeId())
            .toString();
    }

}
