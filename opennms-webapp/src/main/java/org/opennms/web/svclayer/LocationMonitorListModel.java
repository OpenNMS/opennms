/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
 * <p>LocationMonitorListModel class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationMonitorListModel {
    private static final String HOST_ADDRESS_KEY = "org.opennms.netmgt.poller.remote.hostAddress";
    private static final String HOST_NAME_KEY = "org.opennms.netmgt.poller.remote.hostName";

    private Errors m_errors;
    private List<LocationMonitorModel> m_locationMonitors;
    
    /**
     * <p>Constructor for LocationMonitorListModel.</p>
     */
    public LocationMonitorListModel() {
    }

    /**
     * <p>getLocationMonitors</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<LocationMonitorModel> getLocationMonitors() {
        return m_locationMonitors;
    }

    /**
     * <p>setLocationMonitors</p>
     *
     * @param locationMonitors a {@link java.util.List} object.
     */
    public void setLocationMonitors(List<LocationMonitorModel> locationMonitors) {
        m_locationMonitors = locationMonitors;
    }
    
    /**
     * <p>addLocationMonitor</p>
     *
     * @param locationMonitor a {@link org.opennms.web.svclayer.LocationMonitorListModel.LocationMonitorModel} object.
     */
    public void addLocationMonitor(LocationMonitorModel locationMonitor) {
        if (m_locationMonitors == null) {
            m_locationMonitors = new LinkedList<LocationMonitorModel>();
        }
        m_locationMonitors.add(locationMonitor);
    }

    /**
     * <p>getErrors</p>
     *
     * @return a {@link org.springframework.validation.Errors} object.
     */
    public Errors getErrors() {
        return m_errors;
    }

    /**
     * <p>setErrors</p>
     *
     * @param errors a {@link org.springframework.validation.Errors} object.
     */
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
                @Override
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
