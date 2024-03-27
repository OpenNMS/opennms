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
package org.opennms.features.alarms.history.elastic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.junit.Test;
import org.opennms.features.jest.client.JestClientWithCircuitBreaker;
import org.opennms.features.jest.client.template.TemplateInitializer;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMemo;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsReductionKeyMemo;
import org.opennms.netmgt.model.TroubleTicketState;

import com.codahale.metrics.MetricRegistry;

public class ElasticAlarmIndexerTest {
    private final ElasticAlarmIndexer elasticAlarmIndexer = new ElasticAlarmIndexer(mock(MetricRegistry.class),
            mock(JestClientWithCircuitBreaker.class), mock(TemplateInitializer.class));

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
