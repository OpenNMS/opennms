/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.svclayer.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.springframework.validation.Errors;

/**
 * <p>LocationMonitorListModel class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationMonitorListModel {

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
     * @param locationMonitor a {@link org.opennms.web.svclayer.model.LocationMonitorListModel.LocationMonitorModel} object.
     */
    public void addLocationMonitor(LocationMonitorModel locationMonitor) {
        if (m_locationMonitors == null) {
            m_locationMonitors = new LinkedList<>();
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
        private String m_id;
        private String m_name;
        private String m_hostName;
        private String m_ipAddress;
        private String m_connectionHostName;
        private String m_connectionIpAddress;
        private Date m_lastCheckInTime;
        private Map<String, String> m_additionalDetails;

        public LocationMonitorModel() {
        }
        
        /**
         * Create a LocationMonitorModel and populate it with data from a
         * OnmsLocationMonitor and OnmsMonitoringLocationDefinition (if any).
         * 
         * @param def the monitoring location definition for the location monitor (if any; can be null)
         */
        public LocationMonitorModel(OnmsMonitoringLocation def) {
            if (def == null) {
                throw new IllegalArgumentException("def argument cannot be null");
            }

            if (def != null && def.getMonitoringArea() != null) {
                setArea(def.getMonitoringArea());
            }
            
            setDefinitionName(def.getLocationName());
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

        public String getConnectionHostName() {
            return m_connectionHostName;
        }

        public void setConnectionHostName(String hostName) {
            m_connectionHostName = hostName;
        }

        public String getId() {
            return m_id;
        }

        public void setId(String id) {
            m_id = id;
        }

        public String getIpAddress() {
            return m_ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            m_ipAddress = ipAddress;
        }

        public String getConnectionIpAddress() {
            return m_connectionIpAddress;
        }

        public void setConnectionIpAddress(String ipAddress) {
            m_connectionIpAddress = ipAddress;
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
    }
}
