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

public class AlarmStatusCalculator implements StatusCalculator {

    private final GenericPersistenceAccessor genericPersistenceAccessor;

    public AlarmStatusCalculator(GenericPersistenceAccessor genericPersistenceAccessor) {
        this.genericPersistenceAccessor = genericPersistenceAccessor;
    }

    @Override
    public Status calculateStatus(GeolocationQuery query, Set<Integer> nodeIds) {
        final List<String> parameterNames = Lists.newArrayList("nodeIds", "severity");
        final List<Object> parameterValues = Lists.newArrayList(nodeIds, Utils.getSeverity(query));

        final StringBuilder hql = new StringBuilder();
        hql.append("SELECT node.id, max(alarm.severity), count(alarm.id), count(alarm.alarmAckTime) ");
        hql.append("FROM OnmsAlarm AS alarm ");
        hql.append("LEFT JOIN alarm.node AS node ");
        hql.append("WHERE node.id IN (:nodeIds) ");
        hql.append("AND alarm.severity >= :severity ");
        if (!query.isIncludeAcknowledgedAlarms()) {
            hql.append("AND alarm.alarmAckTime is null ");
        }
        if(query.getLocation() != null) {
            hql.append("AND node.location.locationName = :nodeLocation ");
            parameterNames.add("nodeLocation");
            parameterValues.add(query.getLocation());
        }
        hql.append("GROUP BY node.id");

        final List<Object[]> rows = genericPersistenceAccessor.findUsingNamedParameters(
                hql.toString(),
                parameterNames.toArray(new String[parameterNames.size()]),
                parameterValues.toArray());
        final Status status = new Status();
        for(Object[] row : rows) {
            status.add((int) row[0], (OnmsSeverity) row[1], (long) row[2], (long) row[3]);
        }
        return status;
    }
}
