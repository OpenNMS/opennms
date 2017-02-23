/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.server;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.StatusDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

/**
 * <p>LocationDataService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface LocationDataService {
    /**
     * <p>getLocationInfo</p>
     *
     * @param locationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     */
    LocationInfo getLocationInfo(final String locationName);
    /**
     * <p>getLocationInfo</p>
     *
     * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     */
    LocationInfo getLocationInfo(final OnmsMonitoringLocation def);
    /**
     * <p>getApplicationInfo</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    ApplicationInfo getApplicationInfo(final String applicationName);
    /**
     * <p>getApplicationInfo</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    ApplicationInfo getApplicationInfo(final OnmsApplication app);
    /**
     * <p>getApplicationInfo</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @param status a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    ApplicationInfo getApplicationInfo(final OnmsApplication app, final StatusDetails status);
    /**
     * <p>getLocationDetails</p>
     *
     * @param locationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationDetails} object.
     */
    LocationDetails getLocationDetails(final String locationName);
    /**
     * <p>getLocationDetails</p>
     *
     * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationDetails} object.
     */
    LocationDetails getLocationDetails(final OnmsMonitoringLocation def);
    /**
     * <p>getApplicationDetails</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
     */
    ApplicationDetails getApplicationDetails(final String applicationName);
    /**
     * <p>getApplicationDetails</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
     */
    ApplicationDetails getApplicationDetails(final OnmsApplication app);
    /**
     * <p>getUpdatedLocationsBetween</p>
     *
     * @param startDate a {@link java.util.Date} object.
     * @param endDate a {@link java.util.Date} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<LocationInfo> getUpdatedLocationsBetween(final Date startDate, final Date endDate);
    /**
     * <p>getLatLng</p>
     *
     * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @param geocode a boolean.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     */
    GWTLatLng getLatLng(final OnmsMonitoringLocation def, boolean geocode);
    /**
     * <p>handleAllMonitoringLocationDefinitions</p>
     *
     * @param handlers a {@link java.util.Collection} object.
     */
    void handleAllMonitoringLocationDefinitions(final Collection<LocationDefHandler> handlers);
    /**
     * <p>handleAllApplications</p>
     *
     * @param appHandlers a {@link java.util.Collection} object.
     */
    void handleAllApplications(final Collection<ApplicationHandler> appHandlers);
    /**
     * <p>getLocationInfoForMonitor</p>
     *
     * @param monitorId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     */
    LocationInfo getLocationInfoForMonitor(final Integer monitorId);
    /**
     * <p>getApplicationsForLocation</p>
     *
     * @param info a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<ApplicationInfo> getApplicationsForLocation(final LocationInfo info);
    
    /**
     * <p>getInfoForAllLocations</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<LocationInfo> getInfoForAllLocations();

    /**
     * <p>getStatusDetailsForAllLocations</p>
     *
     */
    Map<String, StatusDetails> getStatusDetailsForAllLocations();

    /**
     * <p>getInfoForAllApplications</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<ApplicationInfo> getInfoForAllApplications();
    /**
     * <p>getStatusDetailsForLocation</p>
     *
     * @param def a {@link OnmsMonitoringLocation} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    StatusDetails getStatusDetailsForLocation(OnmsMonitoringLocation def);
    /**
     * <p>getStatusDetailsForApplication</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    StatusDetails getStatusDetailsForApplication(OnmsApplication app);
    

}
