/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.geolocation.services.status;

import java.util.List;
import java.util.Set;

import org.opennms.features.geolocation.api.GeolocationQuery;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.collect.Lists;

public class OutageStatusCalculator implements StatusCalculator {

    private final GenericPersistenceAccessor genericPersistenceAccessor;

    public OutageStatusCalculator(GenericPersistenceAccessor genericPersistenceAccessor) {
        this.genericPersistenceAccessor = genericPersistenceAccessor;
    }

    @Override
    public Status calculateStatus(GeolocationQuery query, Set<Integer> nodeIds) {
        final List<String> parameterNames = Lists.newArrayList("nodeIds", "severity");
        final List<Object> parameterValues = Lists.newArrayList(nodeIds, Utils.getSeverity(query).getId());

        final StringBuilder hql = new StringBuilder();
        hql.append("SELECT node.id, max(event.eventSeverity) ");
        hql.append("FROM OnmsOutage as outage ");
        hql.append("LEFT JOIN outage.monitoredService as ifservice ");
        hql.append("LEFT JOIN ifservice.ipInterface as ipinterface ");
        hql.append("LEFT JOIN ipinterface.node as node ");
        hql.append("LEFT JOIN outage.serviceLostEvent as event ");
        hql.append("WHERE node.id in (:nodeIds) ");
        hql.append("AND outage.serviceRegainedEvent is null ");
        hql.append("AND event.eventSeverity >= :severity ");

        if (query.getLocation() != null) {
            hql.append("AND node.location.locationName = :locationName ");
            parameterNames.add("locationName");
            parameterValues.add(query.getLocation());
        }
        hql.append("GROUP BY node.id");

        final List<Object[]> rows = genericPersistenceAccessor.findUsingNamedParameters(
                hql.toString(),
                parameterNames.toArray(new String[parameterNames.size()]),
                parameterValues.toArray());
        final Status status = new Status();
        for (Object[] eachRow : rows) {
            status.add((int) eachRow[0], OnmsSeverity.get((int) eachRow[1]), 0, 0);
        }
        return status;
    }
}
