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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.web.Util;
import org.opennms.web.command.LocationMonitorDetailsCommand;
import org.opennms.web.svclayer.DistributedPollerService;
import org.opennms.web.svclayer.SimpleWebTable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;

public class DefaultDistributedPollerService implements
        DistributedPollerService {
    
    private static final String HOST_ADDRESS_KEY = "org.opennms.netmgt.poller.remote.hostAddress";

    private static final String HOST_NAME_KEY = "org.opennms.netmgt.poller.remote.hostName";

    private LocationMonitorDao m_locationMonitorDao;
    
    private OnmsLocationMonitorAreaNameComparator m_comparator =
        new OnmsLocationMonitorAreaNameComparator();

    public SimpleWebTable createStatusTable() {
        List<OnmsLocationMonitor> monitors = m_locationMonitorDao.findAll();
        
        Collections.sort(monitors, m_comparator);
        
        SimpleWebTable table = new SimpleWebTable();
        table.setTitle("distributed.pollerStatus.title");
        
        table.addColumn("distributed.area");
        table.addColumn("distributed.definitionName");
        table.addColumn("distributed.id");
        table.addColumn("distributed.hostName");
        table.addColumn("distributed.ipAddress");
        table.addColumn("distributed.status");
        table.addColumn("distributed.lastCheckInTime");
        
        for (OnmsLocationMonitor monitor : monitors) {
            String area = "";
            OnmsMonitoringLocationDefinition def = m_locationMonitorDao.findMonitoringLocationDefinition(monitor.getDefinitionName());
            if (def != null && def.getArea() != null) {
                area = def.getArea();
            }
                        
            String style = getStyleForStatus(monitor.getStatus());
          
            String hostName = monitor.getDetails().get(HOST_NAME_KEY);
            String hostAddress = monitor.getDetails().get(HOST_ADDRESS_KEY);
            
            if (hostName == null) {
                hostName = "";
            }
            if (hostAddress == null) {
                hostAddress = "";
            }
            
            /*
             * We check for null here because the DB column could be null if
             * the location monitor registered but has never started (or checked in?).
             * 
             * Also, we wrap the Date that we get from getLastCheckInTime() in
             * a java.util.Date because the class that we get from getLastCheckInTime()
             * has a different format when we call toString().
             *  
             * TODO: Come up with a better way to format dates in all of the webapp
             */
            String date = (monitor.getLastCheckInTime() != null)
                ? new Date(monitor.getLastCheckInTime().getTime()).toString()
                : "Never";
            	
                        
            table.newRow();
            table.addCell(area, style);
            table.addCell(monitor.getDefinitionName());
            table.addCell(monitor.getId(), "", "distributed/locationMonitorDetails.htm"
                          + "?monitorId="
                          + Util.encode(monitor.getId().toString()));
            table.addCell(hostName);
            table.addCell(hostAddress);
            table.addCell(monitor.getStatus(), "divider bright");
            table.addCell(date);
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

    public LocationMonitorDetailsModel getLocationMonitorDetails(LocationMonitorDetailsCommand cmd, BindException errors) {
        LocationMonitorDetailsModel model = new LocationMonitorDetailsModel();
        model.setErrors(errors);
        
        if (errors.getErrorCount() > 0) {
            return model;
        }
        
        OnmsLocationMonitor monitor = m_locationMonitorDao.get(cmd.getMonitorId());
        
        model.setTitle(new DefaultMessageSourceResolvable("distributed.locationMonitorDetails.title"));
        
        String area = "";
        OnmsMonitoringLocationDefinition def = m_locationMonitorDao.findMonitoringLocationDefinition(monitor.getDefinitionName());
        if (def != null && def.getArea() != null) {
            area = def.getArea();
        }
                    
        String hostName = monitor.getDetails().get(HOST_NAME_KEY);
        String hostAddress = monitor.getDetails().get(HOST_ADDRESS_KEY);
        
        if (hostName == null) {
            hostName = "";
        }
        if (hostAddress == null) {
            hostAddress = "";
        }
        
        model.addMainDetail(new DefaultMessageSourceResolvable("distributed.area"), new DefaultMessageSourceResolvable(null, area));
        model.addMainDetail(new DefaultMessageSourceResolvable("distributed.definitionName"), new DefaultMessageSourceResolvable(null, monitor.getDefinitionName()));
        model.addMainDetail(new DefaultMessageSourceResolvable("distributed.id"), new DefaultMessageSourceResolvable(null, monitor.getId().toString()));
        model.addMainDetail(new DefaultMessageSourceResolvable("distributed.hostName"), new DefaultMessageSourceResolvable(null, hostName));
        model.addMainDetail(new DefaultMessageSourceResolvable("distributed.ipAddress"), new DefaultMessageSourceResolvable(null, hostAddress));
        // Localize the status
        model.addMainDetail(new DefaultMessageSourceResolvable("distributed.status"), new DefaultMessageSourceResolvable("distributed.status." + monitor.getStatus().toString()));
        model.addMainDetail(new DefaultMessageSourceResolvable("distributed.lastCheckInTime"), new DefaultMessageSourceResolvable(null, new Date(monitor.getLastCheckInTime().getTime()).toString()));
        
        model.setAdditionalDetailsTitle(new DefaultMessageSourceResolvable("distributed.locationMonitorDetails.additionalTitle"));
        List<Entry<String, String>> details = new ArrayList<Entry<String, String>>(monitor.getDetails().entrySet());
        Collections.sort(details, new Comparator<Entry<String, String>>() {
            public int compare(Entry<String, String> one, Entry<String, String> two) {
                return one.getKey().compareToIgnoreCase(two.getKey());
            }
            
        });
        for (Entry<String, String> detail : details) {
            if (!detail.getKey().equals(HOST_NAME_KEY) && !detail.getKey().equals(HOST_ADDRESS_KEY)) {
                // Localize the key, and default to the key name
                model.addAdditionalDetail(new DefaultMessageSourceResolvable(new String[] { "distributed.detail." + detail.getKey() }, detail.getKey()), new DefaultMessageSourceResolvable(null, detail.getValue()));
            }
        }

        return model;
    }

}
