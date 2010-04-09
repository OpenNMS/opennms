
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/


package org.opennms.netmgt.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
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

@Entity
@Table(name="location_specific_status_changes")
public class OnmsLocationSpecificStatus {

    private Integer m_id;
    private OnmsLocationMonitor m_locationMonitor;
    private OnmsMonitoredService m_monitoredService;
    private PollStatus m_pollResult;
	private Date m_statusTime;

    public OnmsLocationSpecificStatus() {
        // this is used by hibernate to construct an object from the db
    }

    public OnmsLocationSpecificStatus(final OnmsLocationMonitor locationMonitor, final OnmsMonitoredService monitoredService, final PollStatus pollResult) {
        m_locationMonitor = locationMonitor;
        m_monitoredService = monitoredService;
        m_pollResult = pollResult;
    }

    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    public void setId(final Integer id) {
        m_id = id;
    }

    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="locationMonitorId")
    public OnmsLocationMonitor getLocationMonitor() {
        return m_locationMonitor;
    }

    public void setLocationMonitor(final OnmsLocationMonitor locationMonitor) {
        m_locationMonitor = locationMonitor;
    }

    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="ifServiceId")
    public OnmsMonitoredService getMonitoredService() {
        return m_monitoredService;
    }

    public void setMonitoredService(final OnmsMonitoredService monitoredService) {
        m_monitoredService = monitoredService;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="statustime")
    public Date getStatusTime() {
    	return m_statusTime;
    }

    public void setStatusTime(final Date date) {
    	m_statusTime = date;
    }
    
    @Embedded
    public PollStatus getPollResult() {
        return m_pollResult;
    }

    public void setPollResult(final PollStatus newStatus) {
        m_pollResult = newStatus;
    }

    @Transient
    public int getStatusCode() {
        return m_pollResult.getStatusCode();
    }
}
