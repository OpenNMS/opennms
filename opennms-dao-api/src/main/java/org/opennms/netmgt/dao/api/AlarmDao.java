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

import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.List;

public interface AlarmDao extends LegacyOnmsDao<OnmsAlarm, Integer> {

    OnmsAlarm findByReductionKey(String reductionKey);

    /**
     * <p>Get the list of current - not yet acknowledged - alarms per node with severity greater than normal,
     * reflecting the max severity, the minimum last event time and alarm count;
     * ordered by the oldest.</p>
     *
     * @return A list of alarm summaries.
     */
    List<AlarmSummary> getNodeAlarmSummaries();

    /**
     * Does the same as {@link #getNodeAlarmSummaries()} but allows to restrict the AlarmSummary calculation to
     * specific nodeIds. It also calculates the alarm count differently. The alarm count only considers
     * not yet acknowledged alarms, but the max severity is calculated overall (means also acknowledged) alarms.
     *
     * @param nodeIds The nodeIds you want to restrict the AlarmSummary calculation to. Must not be NULL!
     */
    List<AlarmSummary> getNodeAlarmSummariesIncludeAcknowledgedOnes(List<Integer> nodeIds);

    List<EdgeAlarmStatusSummary> getLldpEdgeAlarmSummaries(List<Integer> lldpLinkIds);

    List<HeatMapElement> getHeatMapItemsForEntity(String entityNameColumn, String entityIdColumn, boolean processAcknowledgedAlarms, String restrictionColumn, String restrictionValue, String... groupByColumns);
}
