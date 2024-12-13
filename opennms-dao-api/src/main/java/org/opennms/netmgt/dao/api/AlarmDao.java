/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.api;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.alarm.SituationSummary;

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
     * <p>Get the list of current - not yet acknowledged - situations with severity greater than normal;
     *
     * @return A list of situation summaries.
     */
    List<SituationSummary> getSituationSummaries();

    /**
     * Get the list of current alarms per node with severity not equal to cleared, reflecting the max severity,
     * the minimum last event time and alarm count.
     * The alarm count only considers not yet acknowledged alarms, but the max severity is calculated overall
     * (including acknowledged) alarms.
     *
     * @param nodeIds The nodeIds you want to restrict the AlarmSummary calculation to. Must not be NULL!
     */
    List<AlarmSummary> getNodeAlarmSummariesIncludeAcknowledgedOnes(List<Integer> nodeIds);

    List<HeatMapElement> getHeatMapItemsForEntity(String entityNameColumn, String entityIdColumn, boolean processAcknowledgedAlarms, String restrictionColumn, String restrictionValue, String... groupByColumns);

    List<OnmsAlarm> getAlarmsForEventParameters(final Map<String, String> eventParameters);

    /**
     * Returns the number of situations currently present in the database.
     */
    long getNumSituations();

    int getNumAlarmsPastHours(int hours );

}
