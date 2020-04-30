/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.dao.api.LocationSpecificStatusDao;
import org.opennms.netmgt.model.LocationIpInterface;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class MockLocationSpecificStatusDao extends AbstractMockDao<OnmsLocationSpecificStatus, Integer> implements LocationSpecificStatusDao {
    @Override
    protected Integer getId(OnmsLocationSpecificStatus entity) {
        return null;
    }

    @Override
    protected void generateId(OnmsLocationSpecificStatus entity) {
    }

    @Override
    public Collection<OnmsMonitoringLocation> findByApplication(OnmsApplication application) {
        return null;
    }

    @Override
    public void saveStatusChange(OnmsLocationSpecificStatus locationSpecificStatus) {

    }

    @Override
    public OnmsLocationSpecificStatus getMostRecentStatusChange(OnmsMonitoringLocation location, OnmsMonitoredService monSvc) {
        return null;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getAllMostRecentStatusChanges() {
        return null;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getAllStatusChangesAt(Date timestamp) {
        return null;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesBetween(Date startDate, Date endDate) {
        return null;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesForLocationBetween(Date startDate, Date endDate, String locationName) {
        return null;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesForApplicationBetween(Date startDate, Date endDate, String applicationName) {
        return null;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesBetweenForApplications(Date startDate, Date endDate, Collection<String> applicationNames) {
        return null;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getMostRecentStatusChangesForLocation(String locationName) {
        return null;
    }

    @Override
    public Collection<LocationIpInterface> findStatusChangesForNodeForUniqueMonitorAndInterface(int nodeId) {
        return null;
    }
}
