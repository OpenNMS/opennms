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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public interface LocationMonitorDao extends OnmsDao<OnmsLocationMonitor, Integer> {
    
    Collection<OnmsLocationMonitor> findByLocationDefinition(final OnmsMonitoringLocationDefinition locationDefinition);
    
    Collection<OnmsLocationMonitor> findByApplication(final OnmsApplication application);

    List<OnmsMonitoringLocationDefinition> findAllMonitoringLocationDefinitions();
    
    OnmsMonitoringLocationDefinition findMonitoringLocationDefinition(final String monitoringLocationDefinitionName);
    
    void saveMonitoringLocationDefinition(final OnmsMonitoringLocationDefinition def);
    
    void saveMonitoringLocationDefinitions(final Collection<OnmsMonitoringLocationDefinition> defs);

    void saveStatusChange(final OnmsLocationSpecificStatus status);
    
    OnmsLocationSpecificStatus getMostRecentStatusChange(final OnmsLocationMonitor locationMonitor, final OnmsMonitoredService monSvc);

    Collection<OnmsLocationSpecificStatus> getAllMostRecentStatusChanges();
    
    Collection<OnmsLocationSpecificStatus> getAllStatusChangesAt(final Date timestamp);
    
    /**
     * Returns all status changes since the date, <b>and</b> one previous
     * status change (so that status at the beginning of the period can be
     * determined).
     */
    Collection<OnmsLocationSpecificStatus> getStatusChangesBetween(final Date startDate, final Date endDate);

    Collection<OnmsLocationSpecificStatus> getStatusChangesForLocationBetween(final Date startDate, final Date endDate, final String locationDefinitionName);

    Collection<OnmsLocationSpecificStatus> getMostRecentStatusChangesForLocation(String locationName);

    Collection<LocationMonitorIpInterface> findStatusChangesForNodeForUniqueMonitorAndInterface(final int nodeId);

}
