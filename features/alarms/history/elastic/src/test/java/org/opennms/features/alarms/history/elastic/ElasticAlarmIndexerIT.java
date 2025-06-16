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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.reindex.ReindexPlugin;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.core.time.PseudoClock;
import org.opennms.features.alarms.history.api.AlarmHistoryRepository;
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

public class ElasticAlarmIndexerIT {
    private JestClientWithCircuitBreaker jestClient;
    private AlarmHistoryRepository alarmHistoryRepo;
    private ElasticAlarmIndexer elasticAlarmIndexer;
    private CircuitBreaker circuitBreaker;

    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withPlugins(PainlessPlugin.class, ReindexPlugin.class)
    );

    @BeforeClass
    public static void classSetUp() {
        MockLogAppender.setupLogging(true, "DEBUG");
        Awaitility.setDefaultPollDelay(1, TimeUnit.SECONDS);
        Awaitility.setDefaultPollInterval(5, TimeUnit.SECONDS);
    }

    @Before
    public void setUp() throws IOException {
        final RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        final EventForwarder eventForwarder = new AbstractMockDao.NullEventForwarder();
        final JestClientWithCircuitBreaker jestClient = restClientFactory.createClientWithCircuitBreaker(CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom().build()).circuitBreaker(ElasticAlarmIndexerIT.class.getName()), eventForwarder);
        alarmHistoryRepo = new ElasticAlarmHistoryRepository(jestClient, IndexStrategy.MONTHLY, new IndexSettings());

        MetricRegistry metrics = new MetricRegistry();
        TemplateInitializerForAlarms templateInitializerForAlarms = new TemplateInitializerForAlarms(jestClient);
        elasticAlarmIndexer = new ElasticAlarmIndexer(metrics, jestClient, templateInitializerForAlarms);
        elasticAlarmIndexer.setUsePseudoClock(true);
        elasticAlarmIndexer.init();

        // Wait until ES is up and running - initially there should be no documents
        await().atMost(1, TimeUnit.MINUTES).ignoreExceptions()
                .until(alarmHistoryRepo::getActiveAlarmsNow, hasSize(equalTo(0)));
    }

    @Test
    public void canHandleAlarmSnapshots() {
        // Use some value N that is greater than the the size used for the composite aggregations in the query
        // in order to verify that the pagination is working properly
        final long N = QueryProvider.MAX_BUCKETS * 2;
        final long now = PseudoClock.getInstance().getTime();
        final List<OnmsAlarm> alarms = LongStream.range(0, N)
                .mapToObj(i -> createAlarm((int)i, now))
                .collect(Collectors.toList());

        // Trigger the snapshot
        issueSnapshotWithPreAndPostCalls(alarms);

        // Wait for the alarms to be indexed
        await().atMost(1, TimeUnit.MINUTES).until(() -> alarmHistoryRepo.getNumActiveAlarmsAt(now), equalTo(N));


        // Advance the clock
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MINUTES);
        final long then = PseudoClock.getInstance().getTime();

        // Now trigger another snapshot with an empty list of alarms
        issueSnapshotWithPreAndPostCalls(Collections.emptyList());

        // Wait for the deletes to be indexed
        await().atMost(1, TimeUnit.MINUTES).until(() -> alarmHistoryRepo.getNumActiveAlarmsAt(then), equalTo(0L));
    }

    private void issueSnapshotWithPreAndPostCalls(List<OnmsAlarm> alarms) {
        elasticAlarmIndexer.preHandleAlarmSnapshot();
        elasticAlarmIndexer.handleAlarmSnapshot(alarms);
        elasticAlarmIndexer.postHandleAlarmSnapshot();
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
}
