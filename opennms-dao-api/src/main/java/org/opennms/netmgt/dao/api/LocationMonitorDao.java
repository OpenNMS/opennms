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

package org.opennms.netmgt.dao.api;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

/**
 * <p>LocationMonitorDao interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public interface LocationMonitorDao extends OnmsDao<OnmsLocationMonitor, String> {
    
    /**
     * <p>findByLocationDefinition</p>
     *
     * @param locationDefinition a {@link org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLocationMonitor> findByLocationDefinition(final OnmsMonitoringLocation locationDefinition);
    
    /**
     * <p>findByApplication</p>
     *
     * @param application a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLocationMonitor> findByApplication(final OnmsApplication application);

    /**
     * <p>saveStatusChange</p>
     *
     * @param status a {@link org.opennms.netmgt.model.OnmsLocationSpecificStatus} object.
     */
    void saveStatusChange(final OnmsLocationSpecificStatus status);
    
    /**
     * <p>getMostRecentStatusChange</p>
     *
     * @param locationMonitor a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     * @param monSvc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @return a {@link org.opennms.netmgt.model.OnmsLocationSpecificStatus} object.
     */
    OnmsLocationSpecificStatus getMostRecentStatusChange(final OnmsLocationMonitor locationMonitor, final OnmsMonitoredService monSvc);

    /**
     * <p>getAllMostRecentStatusChanges</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLocationSpecificStatus> getAllMostRecentStatusChanges();
    
    /**
     * <p>getAllStatusChangesAt</p>
     *
     * @param timestamp a {@link java.util.Date} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLocationSpecificStatus> getAllStatusChangesAt(final Date timestamp);
    
    /**
     * Returns all status changes since the date, <b>and</b> one previous
     * status change (so that status at the beginning of the period can be
     * determined).
     *
     * @param startDate a {@link java.util.Date} object.
     * @param endDate a {@link java.util.Date} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLocationSpecificStatus> getStatusChangesBetween(final Date startDate, final Date endDate);

    /**
     * <p>getStatusChangesForLocationBetween</p>
     *
     * @param startDate a {@link java.util.Date} object.
     * @param endDate a {@link java.util.Date} object.
     * @param locationDefinitionName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLocationSpecificStatus> getStatusChangesForLocationBetween(final Date startDate, final Date endDate, final String locationDefinitionName);
    
    /**
     * <p>getStatusChangesForApplicationBetween</p>
     *
     * @param startDate a {@link java.util.Date} object.
     * @param endDate a {@link java.util.Date} object.
     * @param applicationName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     * @since 1.8.1
     */
    Collection<OnmsLocationSpecificStatus> getStatusChangesForApplicationBetween(final Date startDate, final Date endDate, final String applicationName);
    
    /**
     * <p>getStatusChangesBetweenForApplications</p>
     * 
     * @param startDate a {@link java.util.Date} object.
     * @param endDate a {@link java.util.Date} object.
     * @param application a {@link java.util.Collection} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLocationSpecificStatus> getStatusChangesBetweenForApplications(final Date startDate, final Date endDate, final Collection<String> applicationNames);

    /**
     * <p>getMostRecentStatusChangesForLocation</p>
     *
     * @param locationName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLocationSpecificStatus> getMostRecentStatusChangesForLocation(String locationName);

    /**
     * <p>findStatusChangesForNodeForUniqueMonitorAndInterface</p>
     *
     * @param nodeId a int.
     * @return a {@link java.util.Collection} object.
     */
    Collection<LocationMonitorIpInterface> findStatusChangesForNodeForUniqueMonitorAndInterface(final int nodeId);

    /**
     * Mark all location monitors as paused except those that are already stopped
     */
    void pauseAll();

    /**
     * Mark all paused location monitors as started
     */
    void resumeAll();
}
