/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.alarms.history.elastic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMemo;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsReductionKeyMemo;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.plugins.elasticsearch.rest.template.TemplateInitializer;

import com.codahale.metrics.MetricRegistry;

import io.searchbox.client.JestClient;

public class ElasticAlarmIndexerTest {
    private final ElasticAlarmIndexer elasticAlarmIndexer = new ElasticAlarmIndexer(mock(MetricRegistry.class),
            mock(JestClient.class), mock(TemplateInitializer.class));

    @Test
    public void testGetDocumentIfNeedsIndexing() {
        OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setId(1);

        // Check to make sure updating each of the fields we expect to cause a re-index actually does
        updateAndTestPositive(onmsAlarm, () -> onmsAlarm.setReductionKey("test"));
        updateAndTestPositive(onmsAlarm, () -> onmsAlarm.setAlarmAckTime(new Date(System.currentTimeMillis())));
        updateAndTestPositive(onmsAlarm, () -> onmsAlarm.setSeverityId(2));
        updateAndTestPositive(onmsAlarm, () -> onmsAlarm.addRelatedAlarm(new OnmsAlarm()));
        updateAndTestPositive(onmsAlarm, () -> onmsAlarm.setStickyMemo(new OnmsMemo()));
        updateAndTestPositive(onmsAlarm, () -> onmsAlarm.setReductionKeyMemo(new OnmsReductionKeyMemo()));
        updateAndTestPositive(onmsAlarm, () -> onmsAlarm.setTTicketState(TroubleTicketState.OPEN));
        updateAndTestPositive(onmsAlarm, () -> onmsAlarm.setSituation(false));

        // Check to make sure a few fields we don't care about don't cause a re-index
        updateAndTestNegative(onmsAlarm, () -> onmsAlarm.setLastEvent(new OnmsEvent()));
        updateAndTestNegative(onmsAlarm, () -> onmsAlarm.setDistPoller(new OnmsMonitoringSystem()));
    }

    private void updateAndTestPositive(OnmsAlarm alarm, Runnable update) {
        update.run();
        // Since we updated, it should need to be indexed now
        assertThat(elasticAlarmIndexer.getDocumentIfNeedsIndexing(alarm).isPresent(), is(equalTo(true)));
        // Since it was indexed above it should no longer need to be
        assertThat(elasticAlarmIndexer.getDocumentIfNeedsIndexing(alarm).isPresent(), is(equalTo(false)));
    }

    private void updateAndTestNegative(OnmsAlarm alarm, Runnable update) {
        update.run();
        assertThat(elasticAlarmIndexer.getDocumentIfNeedsIndexing(alarm).isPresent(), is(equalTo(false)));
    }
}
