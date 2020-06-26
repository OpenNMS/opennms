/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.model.LocationIpInterface;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public interface LocationSpecificStatusDao extends OnmsDao<OnmsLocationSpecificStatus, Integer> {
    Collection<OnmsMonitoringLocation> findByApplication(final OnmsApplication application);
    void saveStatusChange(final OnmsLocationSpecificStatus locationSpecificStatus);
    OnmsLocationSpecificStatus getMostRecentStatusChange(final OnmsMonitoringLocation location, final OnmsMonitoredService monSvc);
    Collection<OnmsLocationSpecificStatus> getAllMostRecentStatusChanges();
    Collection<OnmsLocationSpecificStatus> getAllStatusChangesAt(final Date timestamp);
    Collection<OnmsLocationSpecificStatus> getStatusChangesBetween(final Date startDate, final Date endDate);
    Collection<OnmsLocationSpecificStatus> getStatusChangesForLocationBetween(final Date startDate, final Date endDate, final String locationName);
    Collection<OnmsLocationSpecificStatus> getStatusChangesForApplicationNameBetween(final Date startDate, final Date endDate, final String applicationName);
    Collection<OnmsLocationSpecificStatus> getStatusChangesForApplicationIdBetween(final Date startDate, final Date endDate, final Integer applicationId);
    Collection<OnmsLocationSpecificStatus> getStatusChangesBetweenForApplications(final Date startDate, final Date endDate, final Collection<String> applicationNames);
    Collection<OnmsLocationSpecificStatus> getMostRecentStatusChangesForLocation(final String locationName);
    Collection<LocationIpInterface> findStatusChangesForNodeForUniqueMonitorAndInterface(final int nodeId);
}
