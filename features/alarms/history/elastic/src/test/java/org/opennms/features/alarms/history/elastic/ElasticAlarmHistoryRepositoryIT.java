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

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.reindex.ReindexPlugin;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.core.time.PseudoClock;
import org.opennms.features.alarms.history.api.AlarmState;
import org.opennms.features.jest.client.JestClientWithCircuitBreaker;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.dao.mock.AbstractMockDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;

import com.codahale.metrics.MetricRegistry;
import org.awaitility.Awaitility;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public class ElasticAlarmHistoryRepositoryIT {

    private JestClientWithCircuitBreaker jestClient;
    private ElasticAlarmHistoryRepository repo;
    private ElasticAlarmIndexer indexer;
    private CircuitBreaker circuitBreaker;
    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withPlugins(PainlessPlugin.class, ReindexPlugin.class)
    );

    @Before
    public void setUp() throws IOException {
        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        final EventForwarder eventForwarder = new AbstractMockDao.NullEventForwarder();
        final JestClientWithCircuitBreaker jestClient = restClientFactory.createClientWithCircuitBreaker(CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom().waitDurationInOpenState(Duration.ofDays(1000)).build()
        ).circuitBreaker(ElasticAlarmHistoryRepositoryIT.class.getName()), eventForwarder);
        repo = new ElasticAlarmHistoryRepository(jestClient, IndexStrategy.MONTHLY, new IndexSettings());

        TemplateInitializerForAlarms templateInitializer = new TemplateInitializerForAlarms(jestClient);
        MetricRegistry metrics = new MetricRegistry();
        indexer = new ElasticAlarmIndexer(metrics, jestClient, templateInitializer);
        indexer.setUsePseudoClock(true);
        indexer.setIndexAllUpdates(true);
        indexer.init();

        // Increase the default timeout for these tests
        Awaitility.setDefaultTimeout(1, TimeUnit.MINUTES);
        Awaitility.setDefaultPollInterval(5, TimeUnit.SECONDS);

        PseudoClock.getInstance().reset();

        // Wait until ES is up and running - initially there should be no documents
        await().ignoreExceptions()
                .until(repo::getActiveAlarmsNow, hasSize(equalTo(0)));
    }

    @Test
    public void canGetAlarmAtTimestamp() {
        OnmsAlarm a1 = createAlarm(1, 1L);

        // An alarm that doesn't exist should return null
        assertThat(repo.getAlarmWithDbIdAt(a1.getId(), 0).orElse(null), nullValue());
        assertThat(repo.getAlarmWithReductionKeyIdAt(a1.getReductionKey(), 0).orElse(null), nullValue());

        // t=1
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Index a1
        indexer.handleNewOrUpdatedAlarm(a1);

        // The alarm does exist at this time
        await().until(() -> repo.getAlarmWithDbIdAt(a1.getId(), 1).orElse(null), notNullValue());
        await().until(() -> repo.getAlarmWithReductionKeyIdAt(a1.getReductionKey(), 1).orElse(null), notNullValue());

        // The alarm didn't exist and this time
        assertThat(repo.getAlarmWithDbIdAt(a1.getId(), 0).orElse(null), nullValue());
        assertThat(repo.getAlarmWithReductionKeyIdAt(a1.getReductionKey(), 0).orElse(null), nullValue());

        // t=2
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Update the alarm
        updateAlarmWithEvent(a1, 2L);
        indexer.handleNewOrUpdatedAlarm(a1);

        await().until(() -> repo.getAlarmWithDbIdAt(a1.getId(), 2).get().getCounter(), equalTo(2));
        assertThat(repo.getAlarmWithReductionKeyIdAt(a1.getReductionKey(), 2).get().getCounter(), equalTo(2));

        // t=3
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Delete the alarm
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);
        indexer.handleDeletedAlarm(a1.getId(), a1.getReductionKey());

        await().until(() -> repo.getAlarmWithDbIdAt(a1.getId(), 4).get().getDeletedTime(), notNullValue());
        assertThat(repo.getAlarmWithReductionKeyIdAt(a1.getReductionKey(), 4).get().getDeletedTime(), notNullValue());
    }

    @Test
    public void canGetStatesForAlarmWithDbId() {
        OnmsAlarm a1 = createAlarm(1, 1L);

        // An alarm that doesn't exist should return an empty list
        assertThat(repo.getStatesForAlarmWithDbId(a1.getId()), empty());
        assertThat(repo.getStatesForAlarmWithReductionKey(a1.getReductionKey()), empty());

        // t=1
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Index a1
        indexer.handleNewOrUpdatedAlarm(a1);

        // A single state change
        await().until(() -> repo.getStatesForAlarmWithDbId(a1.getId()), hasSize(equalTo(1)));
        assertThat(repo.getStatesForAlarmWithReductionKey(a1.getReductionKey()), hasSize(equalTo(1)));

        // t=2
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Update the alarm
        updateAlarmWithEvent(a1, 2L);
        indexer.handleNewOrUpdatedAlarm(a1);

        // Two state changes
        await().until(() -> repo.getStatesForAlarmWithDbId(a1.getId()), hasSize(equalTo(2)));
        assertThat(repo.getStatesForAlarmWithReductionKey(a1.getReductionKey()), hasSize(equalTo(2)));

        // t=3
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Delete the alarm
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);
        indexer.handleDeletedAlarm(a1.getId(), a1.getReductionKey());

        // Three state changes
        await().until(() -> repo.getStatesForAlarmWithDbId(a1.getId()), hasSize(equalTo(3)));
        assertThat(repo.getStatesForAlarmWithReductionKey(a1.getReductionKey()), hasSize(equalTo(3)));
    }

    @Test
    public void canGetActiveAlarmsAtTimestamp() {
        long queryTime = 10;

        // No alarms initially
        assertThat(repo.getActiveAlarmsAt(queryTime), hasSize(equalTo(0)));

        // t=1
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Index some alarm
        OnmsAlarm a1 = createAlarm(1, 1L);
        indexer.handleNewOrUpdatedAlarm(a1);

        // One alarm active
        await().until(() -> repo.getActiveAlarmsAt(queryTime), hasSize(equalTo(1)));
        assertThat(repo.getNumActiveAlarmsAt(queryTime), equalTo(1L));

        // t=2
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Index another alarm
        OnmsAlarm a2 = createAlarm(2, 2L);
        indexer.handleNewOrUpdatedAlarm(a2);

        // Two alarms active
        await().until(() -> repo.getActiveAlarmsAt(queryTime), hasSize(equalTo(2)));
        assertThat(repo.getNumActiveAlarmsAt(queryTime), equalTo(2L));

        // t=3
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Delete the second alarm
        indexer.handleDeletedAlarm(a2.getId(), a2.getReductionKey());

        // One alarm active
        await().until(() -> repo.getActiveAlarmsAt(queryTime), hasSize(equalTo(1)));
        assertThat(repo.getNumActiveAlarmsAt(queryTime), equalTo(1L));

        // t=4
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Delete the first alarm
        indexer.handleDeletedAlarm(a1.getId(), a1.getReductionKey());

        // No alarms
        await().until(() -> repo.getActiveAlarmsAt(queryTime), hasSize(equalTo(0)));
        assertThat(repo.getNumActiveAlarmsAt(queryTime), equalTo(0L));
    }

    @Test
    public void canGetLastStateOfAllAlarms() {
        // t=1
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Index a1

        OnmsAlarm a1 = createAlarm(1, 1L);
        indexer.handleNewOrUpdatedAlarm(a1);

        // t=2
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Delete a1
        indexer.handleDeletedAlarm(a1.getId(), a1.getReductionKey());

        // Index a2

        OnmsAlarm a2 = createAlarm(2, 2L);
        indexer.handleNewOrUpdatedAlarm(a2);

        // Wait until we have two results

        await().until(() -> repo.getLastStateOfAllAlarms(0, 10), hasSize(equalTo(2)));
        List<AlarmState> alarms = repo.getLastStateOfAllAlarms(0, 10);

        // a1 should be deleted
        AlarmState a1State = alarms.stream().filter(a -> a.getId() == 1).findFirst().orElse(null);
        assertThat(a1State.getDeletedTime(), notNullValue());

        // a2 should not be deleted
        AlarmState a2State = alarms.stream().filter(a -> a.getId() == 2).findFirst().orElse(null);
        assertThat(a2State.getDeletedTime(), nullValue());
    }

    private static OnmsAlarm createAlarm(int id, long firstEventTime) {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(id);
        alarm.setReductionKey("rkey-" + id);
        alarm.setFirstEventTime(new Date(firstEventTime));
        alarm.setCounter(1);

        OnmsEvent lastEvent = new OnmsEvent();
        lastEvent.setId(id);
        lastEvent.setEventTime(new Date(firstEventTime));
        alarm.setLastEvent(lastEvent);
        return alarm;
    }

    private static void updateAlarmWithEvent(OnmsAlarm a, long lastEventTime) {
        OnmsEvent lastEvent = new OnmsEvent();
        lastEvent.setId(a.getId());
        lastEvent.setEventTime(new Date(lastEventTime));
        a.setLastEvent(lastEvent);
        a.setCounter(a.getCounter() + 1);
    }
}
