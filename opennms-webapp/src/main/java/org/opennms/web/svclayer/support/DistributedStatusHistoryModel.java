/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.web.svclayer.model.RelativeTimePeriod;

/**
 * <p>DistributedStatusHistoryModel class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DistributedStatusHistoryModel {
    private List<OnmsMonitoringLocationDefinition> m_locations;
    private List<OnmsApplication> m_applications;
    private OnmsMonitoringLocationDefinition m_chosenLocation;
    private OnmsApplication m_chosenApplication;
    private Collection<OnmsMonitoredService> m_chosenApplicationMemberServices;
    private List<String> m_errors;
    private List<RelativeTimePeriod> m_periods;
    private RelativeTimePeriod m_chosenPeriod;
    private List<OnmsLocationMonitor> m_monitors;
    private OnmsLocationMonitor m_chosenMonitor;
    private SortedSet<ServiceGraph> m_serviceGraphs;
    
    /**
     * <p>Constructor for DistributedStatusHistoryModel.</p>
     *
     * @param locations a {@link java.util.List} object.
     * @param applications a {@link java.util.List} object.
     * @param monitors a {@link java.util.List} object.
     * @param periods a {@link java.util.List} object.
     * @param chosenLocation a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @param chosenApplication a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @param chosenApplicationMemberServices a {@link java.util.Collection} object.
     * @param chosenMonitor a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     * @param chosenPeriod a {@link org.opennms.web.svclayer.model.RelativeTimePeriod} object.
     * @param errors a {@link java.util.List} object.
     */
    public DistributedStatusHistoryModel(
            List<OnmsMonitoringLocationDefinition> locations,
            List<OnmsApplication> applications,
            List<OnmsLocationMonitor> monitors,
            List<RelativeTimePeriod> periods,
            OnmsMonitoringLocationDefinition chosenLocation,
            OnmsApplication chosenApplication,
            Collection<OnmsMonitoredService> chosenApplicationMemberServices,
            OnmsLocationMonitor chosenMonitor,
            RelativeTimePeriod chosenPeriod,
            List<String> errors) {
        m_locations = locations;
        m_applications = applications;
        m_monitors = monitors;
        m_periods = periods;
        m_chosenLocation = chosenLocation;
        m_chosenApplication = chosenApplication;
        m_chosenApplicationMemberServices = chosenApplicationMemberServices;
        m_chosenMonitor = chosenMonitor;
        m_chosenPeriod = chosenPeriod;
        m_errors = errors;
        
    }

    /**
     * <p>getApplications</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsApplication> getApplications() {
        return m_applications;
    }

    /**
     * <p>getLocations</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsMonitoringLocationDefinition> getLocations() {
        return m_locations;
    }

    /**
     * <p>getChosenApplication</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsApplication} object.
     */
    public OnmsApplication getChosenApplication() {
        return m_chosenApplication;
    }

    /**
     * <p>getChosenApplicationMemberServices</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsMonitoredService> getChosenApplicationMemberServices() {
        return m_chosenApplicationMemberServices;
    }

    /**
     * <p>getChosenLocation</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     */
    public OnmsMonitoringLocationDefinition getChosenLocation() {
        return m_chosenLocation;
    }
    
    /**
     * <p>getErrors</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getErrors() {
        return m_errors;
    }

    /**
     * <p>getChosenPeriod</p>
     *
     * @return a {@link org.opennms.web.svclayer.model.RelativeTimePeriod} object.
     */
    public RelativeTimePeriod getChosenPeriod() {
        return m_chosenPeriod;
    }

    /**
     * <p>getPeriods</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<RelativeTimePeriod> getPeriods() {
        return m_periods;
    }

    /**
     * <p>getChosenMonitor</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     */
    public OnmsLocationMonitor getChosenMonitor() {
        return m_chosenMonitor;
    }

    /**
     * <p>getMonitors</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsLocationMonitor> getMonitors() {
        return m_monitors;
    }
    
    /**
     * <p>getServiceGraphs</p>
     *
     * @return a {@link java.util.SortedSet} object.
     */
    public SortedSet<ServiceGraph >getServiceGraphs() {
        return m_serviceGraphs;
    }
    
    /**
     * <p>setServiceGraphs</p>
     *
     * @param serviceGraphs a {@link java.util.SortedSet} object.
     */
    public void setServiceGraphs(SortedSet<ServiceGraph> serviceGraphs) {
        m_serviceGraphs = serviceGraphs;
    }
    
    public static class ServiceGraph {
        private OnmsMonitoredService m_service;
        private String m_url;
        private String[] m_errors;
        
        public ServiceGraph(OnmsMonitoredService service, String url) {
            m_service = service;
            m_url = url;
            m_errors = new String[0];
        }
        
        public ServiceGraph(OnmsMonitoredService service, String[] errors) {
            m_service = service;
            m_url = null;
            m_errors = errors;
        }
        
        public String[] getErrors() {
            return m_errors;
        }
        
        public OnmsMonitoredService getService() {
            return m_service;
        }
        
        public String getUrl() {
            return m_url;
        }
    }
}
