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
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

/**
 * <p>LocationMonitorDao interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public interface LocationMonitorDao extends OnmsDao<OnmsLocationMonitor, Integer> {
    
    /**
     * <p>findByLocationDefinition</p>
     *
     * @param locationDefinition a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLocationMonitor> findByLocationDefinition(OnmsMonitoringLocationDefinition locationDefinition);
    
    /**
     * <p>findAllMonitoringLocationDefinitions</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<OnmsMonitoringLocationDefinition> findAllMonitoringLocationDefinitions();
    
    /**
     * <p>findMonitoringLocationDefinition</p>
     *
     * @param monitoringLocationDefinitionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     */
    OnmsMonitoringLocationDefinition findMonitoringLocationDefinition(String monitoringLocationDefinitionName);
    
    /**
     * <p>saveMonitoringLocationDefinition</p>
     *
     * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     */
    void saveMonitoringLocationDefinition(OnmsMonitoringLocationDefinition def);
    
    /**
     * <p>saveMonitoringLocationDefinitions</p>
     *
     * @param defs a {@link java.util.Collection} object.
     */
    void saveMonitoringLocationDefinitions(Collection<OnmsMonitoringLocationDefinition> defs);

    /**
     * <p>saveStatusChange</p>
     *
     * @param status a {@link org.opennms.netmgt.model.OnmsLocationSpecificStatus} object.
     */
    void saveStatusChange(OnmsLocationSpecificStatus status);
    
    /**
     * <p>getMostRecentStatusChange</p>
     *
     * @param locationMonitor a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     * @param monSvc a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @return a {@link org.opennms.netmgt.model.OnmsLocationSpecificStatus} object.
     */
    OnmsLocationSpecificStatus getMostRecentStatusChange(OnmsLocationMonitor locationMonitor, OnmsMonitoredService monSvc);

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
    Collection<OnmsLocationSpecificStatus> getAllStatusChangesAt(Date timestamp);
    
    /**
     * Returns all status changes since the date, <b>and</b> one previous
     * status change (so that status at the beginning of the period can be
     * determined).
     *
     * @param startDate a {@link java.util.Date} object.
     * @param endDate a {@link java.util.Date} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLocationSpecificStatus> getStatusChangesBetween(Date startDate, Date endDate);

    /**
     * <p>findStatusChangesForNodeForUniqueMonitorAndInterface</p>
     *
     * @param nodeId a int.
     * @return a {@link java.util.Collection} object.
     */
    Collection<LocationMonitorIpInterface> findStatusChangesForNodeForUniqueMonitorAndInterface(int nodeId);
    
}
