//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@Entity
@Table(name = "location_monitors")
public class OnmsLocationMonitor {
    
    public static enum MonitorStatus {
        NEW,
        REGISTERED,
        STARTED,
        STOPPED,
        UNRESPONSIVE,
        DELETED
    }

    private Integer m_id;

    private String m_name;
    
    private MonitorStatus m_status = MonitorStatus.NEW;
    
    private Date m_lastCheckInTime;

    // needed for locating XML configured location definition and
    // creating m_locationDefintion.
    private String m_definitionName;

    private OnmsMonitoringLocationDefinition m_locationDefinition;

    @Id
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer id) {
        m_id = id;
    }

//    @Column(name = "name", length = 63, nullable = false)
//    public String getName() {
//        return m_name;
//    }
//
//    public void setName(String label) {
//        m_name = label;
//    }

//    @Transient
//    public OnmsMonitoringLocationDefinition getLocationDefinition() {
//        return m_locationDefinition;
//    }

//    public void setLocationDefinition(
//            OnmsMonitoringLocationDefinition locationDefinition) {
//        m_locationDefinition = locationDefinition;
//    }

    @Column(name = "definitionName", length = 31, nullable = false)
    public String getDefinitionName() {
        return m_definitionName;
    }

    public void setDefinitionName(String definitionName) {
        m_definitionName = definitionName;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length=13, nullable=false)
    public MonitorStatus getStatus() {
        return m_status;
    }

    public void setStatus(MonitorStatus status) {
        m_status = status;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastCheckInTime")
    public Date getLastCheckInTime() {
        return m_lastCheckInTime;
    }
    
    public void setLastCheckInTime(Date lastCheckInTime) {
        m_lastCheckInTime = lastCheckInTime;
    }
    
}
