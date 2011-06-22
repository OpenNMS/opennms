/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import java.util.Comparator;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;

class LocationSpecificStatusComparator implements Comparator<GWTLocationSpecificStatus> {
    /**
     * <p>compare</p>
     *
     * @param a a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationSpecificStatus} object.
     * @param b a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationSpecificStatus} object.
     * @return a int.
     */
    public int compare(final GWTLocationSpecificStatus a, final GWTLocationSpecificStatus b) {
        return new CompareToBuilder()
            .append(a.getMonitoredService(), b.getMonitoredService())
            .append(
                 a.getLocationMonitor() == null? null : a.getLocationMonitor().getDefinitionName(),
                 b.getLocationMonitor() == null? null : b.getLocationMonitor().getDefinitionName()
            )
            .append(a.getPollTime(), b.getPollTime())
            .append(a.getLocationMonitor(), b.getLocationMonitor())
            .toComparison();
    }
}
