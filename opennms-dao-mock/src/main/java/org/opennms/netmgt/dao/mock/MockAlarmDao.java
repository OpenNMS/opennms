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

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MockAlarmDao extends AbstractMockDao<OnmsAlarm, Integer> implements AlarmDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    public void save(final OnmsAlarm alarm) {
        super.save(alarm);
        updateSubObjects(alarm);
    }

    @Override
    public void update(final OnmsAlarm alarm) {
        super.update(alarm);
        updateSubObjects(alarm);
    }

    private void updateSubObjects(final OnmsAlarm alarm) {
        getDistPollerDao().save(alarm.getDistPoller());
        getEventDao().save(alarm.getLastEvent());
        getNodeDao().save(alarm.getNode());
        getServiceTypeDao().save(alarm.getServiceType());
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
    public OnmsAlarm findByReductionKey(final String reductionKey) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<AlarmSummary> getNodeAlarmSummaries() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<AlarmSummary> getNodeAlarmSummariesIncludeAcknowledgedOnes(List<Integer> nodeIds) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<EdgeAlarmStatusSummary> getLldpEdgeAlarmSummaries(List<Integer> lldpLinkIds) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<HeatMapElement> getHeatMapItemsForEntity(String entityNameColumn, String entityIdColumn, boolean processAcknowledgedAlarms, String restrictionColumn, String restrictionValue, String... groupByColumns) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
