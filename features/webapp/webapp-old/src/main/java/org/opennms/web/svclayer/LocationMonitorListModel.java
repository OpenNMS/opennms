/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 14, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.springframework.validation.Errors;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class LocationMonitorListModel {
    private static final String HOST_ADDRESS_KEY = "org.opennms.netmgt.poller.remote.hostAddress";
    private static final String HOST_NAME_KEY = "org.opennms.netmgt.poller.remote.hostName";

    private Errors m_errors;
    private List<LocationMonitorModel> m_locationMonitors;
    
    public LocationMonitorListModel() {
    }

    public List<LocationMonitorModel> getLocationMonitors() {
        return m_locationMonitors;
    }

    public void setLocationMonitors(List<LocationMonitorModel> locationMonitors) {
        m_locationMonitors = locationMonitors;
    }
    
    public void addLocationMonitor(LocationMonitorModel locationMonitor) {
        if (m_locationMonitors == null) {
            m_locationMonitors = new LinkedList<LocationMonitorModel>();
        }
        m_locationMonitors.add(locationMonitor);
    }

    public Errors getErrors() {
        return m_errors;
    }

    public void setErrors(Errors errors) {
        m_errors = errors;
    }

    public static class LocationMonitorModel {
        private String m_area;
        private String m_definitionName;
        private int m_id;
        private String m_name;
        private String m_hostName;
        private String m_ipAddress;
        private MonitorStatus m_status;
        private Date m_lastCheckInTime;
        private Map<String, String> m_additionalDetails;

        public LocationMonitorModel() {
        }
        
        /**
         * Create a LocationMonitorModel and populate it with data from a
         * OnmsLocationMonitor and OnmsMonitoringLocationDefinition (if any).
         * 
         * @param monitor the location monitor
         * @param def the monitoring location definition for the location monitor (if any; can be null)
         */
        public LocationMonitorModel(OnmsLocationMonitor monitor, OnmsMonitoringLocationDefinition def) {
            if (monitor == null) {
                throw new IllegalArgumentException("monitor argument cannot be null");
            }
            // def can be null

            if (def != null && def.getArea() != null) {
                setArea(def.getArea());
            }
            
            setDefinitionName(monitor.getDefinitionName());
            setId(monitor.getId());
            setHostName(monitor.getDetails().get(HOST_NAME_KEY));
            setIpAddress(monitor.getDetails().get(HOST_ADDRESS_KEY));
            setStatus(monitor.getStatus());
            setLastCheckInTime(monitor.getLastCheckInTime());
            
            List<Entry<String, String>> details = new ArrayList<Entry<String, String>>(monitor.getDetails().entrySet());
            Collections.sort(details, new Comparator<Entry<String, String>>() {
                public int compare(Entry<String, String> one, Entry<String, String> two) {
                    return one.getKey().compareToIgnoreCase(two.getKey());
                }
                
            });
            for (Entry<String, String> detail : details) {
                if (!detail.getKey().equals(HOST_NAME_KEY) && !detail.getKey().equals(HOST_ADDRESS_KEY)) {
                    addAdditionalDetail(detail.getKey(), detail.getValue());
                }
            }
        }
        
        public Map<String, String> getAdditionalDetails() {
            return m_additionalDetails;
        }

        public void setAdditionalDetails(Map<String, String> additionalDetails) {
            m_additionalDetails = additionalDetails;
        }
        
        public void addAdditionalDetail(String key, String value) {
            if (m_additionalDetails == null) {
                m_additionalDetails = new LinkedHashMap<String, String>();
            }
            m_additionalDetails.put(key, value);
        }

        public String getArea() {
            return m_area;
        }

        public void setArea(String area) {
            m_area = area;
        }

        public String getDefinitionName() {
            return m_definitionName;
        }

        public void setDefinitionName(String definitionName) {
            m_definitionName = definitionName;
        }

        public String getHostName() {
            return m_hostName;
        }

        public void setHostName(String hostName) {
            m_hostName = hostName;
        }

        public int getId() {
            return m_id;
        }

        public void setId(int id) {
            m_id = id;
        }

        public String getIpAddress() {
            return m_ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            m_ipAddress = ipAddress;
        }

        public Date getLastCheckInTime() {
            return m_lastCheckInTime;
        }

        public void setLastCheckInTime(Date lastcheckInTime) {
            m_lastCheckInTime = lastcheckInTime;
        }

        public String getName() {
            return m_name;
        }

        public void setName(String name) {
            m_name = name;
        }

        public MonitorStatus getStatus() {
            return m_status;
        }

        public void setStatus(MonitorStatus status) {
            m_status = status;
        }
        
    }
}
