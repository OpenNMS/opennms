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
package org.opennms.netmgt.dao.mock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.alarm.SituationSummary;

public class MockAlarmDao extends AbstractMockDao<OnmsAlarm, Integer> implements AlarmDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public Integer save(final OnmsAlarm alarm) {
        Integer retval = super.save(alarm);
        updateSubObjects(alarm);
        return retval;
    }

    @Override
    public void update(final OnmsAlarm alarm) {
        super.update(alarm);
        updateSubObjects(alarm);
    }

    @Override
    public void delete(final OnmsAlarm alarm) {
        super.delete(alarm);
        updateSubObjects(alarm);
    }

    private void updateSubObjects(final OnmsAlarm alarm) {
        // Assume that the system ID is the ID of an OpenNMS system
        // instead of a Minion or Remote Poller
        getDistPollerDao().save((OnmsDistPoller)alarm.getDistPoller());
        getEventDao().save(alarm.getLastEvent());
        getNodeDao().save(alarm.getNode());
        getServiceTypeDao().save(alarm.getServiceType());
        alarm.getAssociatedAlarms().forEach(a -> getAlarmAssociationDao().saveOrUpdate(a));
    }

    @Override
    protected void generateId(final OnmsAlarm alarm) {
        alarm.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsAlarm alarm) {
        return alarm.getId();
    }

    @Override
    public long getNumSituations() {
        return 0;
    }

    @Override
    public int countNodesFromPast24Hours() {
        return 0;
    }

    @Override
    public OnmsAlarm findByReductionKey(final String reductionKey) {
        for (OnmsAlarm alarm : findAll()) {
            if (alarm.getReductionKey().equals(reductionKey)) {
                return alarm;
            }
        }
        return null;
    }

    @Override
    public List<AlarmSummary> getNodeAlarmSummaries() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<SituationSummary> getSituationSummaries() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<AlarmSummary> getNodeAlarmSummariesIncludeAcknowledgedOnes(List<Integer> nodeIds) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<HeatMapElement> getHeatMapItemsForEntity(String entityNameColumn, String entityIdColumn, boolean processAcknowledgedAlarms, String restrictionColumn, String restrictionValue, String... groupByColumns) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<OnmsAlarm> getAlarmsForEventParameters(Map<String, String> eventParameters) {
        Stream<OnmsAlarm> stream = findAll().stream();

        for (final Map.Entry<String, String> entry : eventParameters.entrySet()) {
            stream = stream.filter(e -> e.getEventParameters().stream().anyMatch(p ->
                       p.getName().matches(entry.getKey().replaceAll("%", ".*")) &&
                       p.getValue().matches(entry.getValue().replace("%", ".*"))));
        }

        return stream.distinct().collect(Collectors.toList());
    }
}
