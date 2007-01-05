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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.svclayer.support;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.web.Util;
import org.opennms.web.svclayer.DistributedPollerService;
import org.opennms.web.svclayer.SimpleWebTable;

public class DefaultDistributedPollerService implements
        DistributedPollerService {
    
    private LocationMonitorDao m_locationMonitorDao;
    
    private OnmsLocationMonitorAreaNameComparator m_comparator =
        new OnmsLocationMonitorAreaNameComparator();

    public SimpleWebTable createStatusTable() {
        List<OnmsLocationMonitor> monitors = m_locationMonitorDao.findAll();
        
        Collections.sort(monitors, m_comparator);
        
        SimpleWebTable table = new SimpleWebTable();
        table.setTitle("Distributed Poller Status");
        
        table.addColumn("Area", "");
        table.addColumn("Definition Name", "");
        table.addColumn("ID", "");
        table.addColumn("Status", "");
        table.addColumn("Last Check-in Time", "");
        
        for (OnmsLocationMonitor monitor : monitors) {
            String area = "";
            OnmsMonitoringLocationDefinition def = m_locationMonitorDao.findMonitoringLocationDefinition(monitor.getDefinitionName());
            if (def != null && def.getArea() != null) {
                area = def.getArea();
            }
                        
            String style = getStyleForStatus(monitor.getStatus());
          
            table.newRow();
            table.addCell(area, style);
            table.addCell(monitor.getDefinitionName(), "");
            table.addCell(monitor.getId(), "", "distributedStatusHistory.htm"
                          + "?location="
                          + Util.encode(monitor.getDefinitionName())
                          + "&monitorId="
                          + Util.encode(monitor.getId().toString()));
            table.addCell(monitor.getStatus(), "divider bright");
            table.addCell(new Date(monitor.getLastCheckInTime().getTime()),
                          "");
        }

        return table;
    }

    private String getStyleForStatus(MonitorStatus status) {
        switch (status) {
        case NEW:
            return "Warning";
            
        case REGISTERED:
            return "Warning";
                
        case STARTED:
            return "Normal";
            
        case STOPPED:
            return "Minor";
                
        case UNRESPONSIVE:
            return "Indeterminate";
                
        default:
            return "Indeterminate";
        }
    }

    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }
    
    /**
     * Sorts OnmsLocationMonitor by the area for the monitoring location
     * definition (if any), then monitoring location definition name, and
     * finally by location monitor ID.
     * 
     * @author djgregor
     */
    public class OnmsLocationMonitorAreaNameComparator
                implements Comparator<OnmsLocationMonitor> {
        public int compare(OnmsLocationMonitor o1, OnmsLocationMonitor o2) {
            OnmsMonitoringLocationDefinition def1 = null;
            OnmsMonitoringLocationDefinition def2 = null;
            
            if (o1.getDefinitionName() != null) {
                def1 = m_locationMonitorDao.findMonitoringLocationDefinition(o1.getDefinitionName());
            }
            
            if (o2.getDefinitionName() != null) {
                def2 = m_locationMonitorDao.findMonitoringLocationDefinition(o2.getDefinitionName());
            }
            
            int diff;
            
            if ((def1 == null || def1.getArea() == null) && (def2 != null && def2.getArea() != null)) {
                return 1;
            } else if ((def1 != null && def1.getArea() != null) && (def2 == null || def2.getArea() == null)) {
                return -1;
            } else if ((def1 != null && def1.getArea() != null) && (def2 != null && def2.getArea() != null)) {
                if ((diff = def1.getArea().compareToIgnoreCase(def1.getArea())) != 0) {
                    return diff;
                }
            }

            if ((diff = o1.getDefinitionName().compareToIgnoreCase(o2.getDefinitionName())) != 0) {
                return diff;
            }
            
            return o1.getId().compareTo(o2.getId());
        }
    }

}
