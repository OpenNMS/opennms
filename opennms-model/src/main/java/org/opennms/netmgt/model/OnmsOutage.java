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

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="outages"
 *     
*/
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
    
    private Date m_suppressTime;
    
    private String m_suppressedBy;

    /** full constructor */
    public OnmsOutage(Date ifLostService, Date ifRegainedService, OnmsEvent eventBySvcRegainedEvent, OnmsEvent eventBySvcLostEvent, OnmsMonitoredService monitoredService) {
        m_ifLostService = ifLostService;
        m_ifRegainedService = ifRegainedService;
        m_eventBySvcRegainedEvent = eventBySvcRegainedEvent;
        m_eventBySvcLostEvent = eventBySvcLostEvent;
        m_monitoredService = monitoredService;
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

    /** 
     * @hibernate.id generator-class="assigned" type="java.lang.Integer" column="outageId"
     * @hibernate.generator-param name="sequence" value="outageNxtId"
     */
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer outageId) {
        m_id = outageId;
    }

    /** 
     *            @hibernate.property
     *             column="ifLostService"
     *             length="8"
     *             not-null="true"
     *         
     */
    public Date getIfLostService() {
        return m_ifLostService;
    }

    public void setIfLostService(Date ifLostService) {
        m_ifLostService = ifLostService;
    }

    /** 
     *            @hibernate.property
     *             column="ifRegainedService"
     *             length="8"
     *         
     */
    public Date getIfRegainedService() {
        return m_ifRegainedService;
    }

    public void setIfRegainedService(Date ifRegainedService) {
        m_ifRegainedService = ifRegainedService;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="svcregainedeventid"         
     *         
     */
    public OnmsEvent getEventBySvcRegainedEvent() {
        return m_eventBySvcRegainedEvent;
    }

    public void setEventBySvcRegainedEvent(OnmsEvent eventBySvcRegainedEvent) {
        m_eventBySvcRegainedEvent = eventBySvcRegainedEvent;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="svclosteventid"         
     *         
     */
    public OnmsEvent getEventBySvcLostEvent() {
        return m_eventBySvcLostEvent;
    }

    public void setEventBySvcLostEvent(OnmsEvent eventBySvcLostEvent) {
        m_eventBySvcLostEvent = eventBySvcLostEvent;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="monitoredServiceid"         
     *         
     */
    public OnmsMonitoredService getMonitoredService() {
        return m_monitoredService;
    }

    public void setMonitoredService(OnmsMonitoredService monitoredService) {
        m_monitoredService = monitoredService;
    }

    public Date getSuppressTime(){
    	return m_suppressTime;
    }
    
    public void setSuppressTime(Date timeToSuppress){
    	m_suppressTime = timeToSuppress;
    }
    
    public String getSuppressedBy(){
    	return m_suppressedBy;
    }
    
    public void setSuppressedBy(String suppressorMan){
    	m_suppressedBy = suppressorMan;
    }
    
    public String toString() {
        return new ToStringCreator(this)
            .append("outageId", getId())
            .append("ifLostService", getIfLostService())
            .append("ifRegainedService", getIfRegainedService())
            .toString();
    }

}
