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

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.painless.PainlessPlugin;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.core.time.PseudoClock;
import org.opennms.features.alarms.history.api.AlarmState;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.plugins.elasticsearch.rest.RestClientFactory;

import com.codahale.metrics.MetricRegistry;
import com.jayway.awaitility.Awaitility;

import io.searchbox.client.JestClient;
import java.util.Date;

public class ElasticAlarmHistoryRepositoryIT {

    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";

    private JestClient jestClient;
    private ElasticAlarmHistoryRepository repo;
    private ElasticAlarmIndexer indexer;

    @Rule
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withDefaults()
            .withSetting("http.enabled", true)
            .withSetting("http.port", HTTP_PORT)
            .withSetting("http.type", "netty4")
            .withSetting("transport.type", "netty4")
            .withSetting("transport.tcp.port", HTTP_TRANSPORT_PORT)
            .withPlugins(PainlessPlugin.class, ReindexPlugin.class)
    );

    @Before
    public void setUp() throws IOException {
        RestClientFactory restClientFactory = new RestClientFactory("http://localhost:" + HTTP_PORT);
        jestClient = restClientFactory.createClient();
        repo = new ElasticAlarmHistoryRepository(jestClient);

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
        assertThat(repo.getAlarmWithDbIdAt(a1.getId(), 0), nullValue());
        assertThat(repo.getAlarmWithReductionKeyIdAt(a1.getReductionKey(), 0), nullValue());

        // t=1
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Index a1
        indexer.handleNewOrUpdatedAlarm(a1);

        // The alarm does exist at this time
        await().until(() -> repo.getAlarmWithDbIdAt(a1.getId(), 1), notNullValue());
        await().until(() -> repo.getAlarmWithReductionKeyIdAt(a1.getReductionKey(), 1), notNullValue());

        // The alarm didn't exist and this time
        assertThat(repo.getAlarmWithDbIdAt(a1.getId(), 0), nullValue());
        assertThat(repo.getAlarmWithReductionKeyIdAt(a1.getReductionKey(), 0), nullValue());

        // t=2
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Update the alarm
        updateAlarmWithEvent(a1, 2L);
        indexer.handleNewOrUpdatedAlarm(a1);

        await().until(() -> repo.getAlarmWithDbIdAt(a1.getId(), 2).getCounter(), equalTo(2));
        assertThat(repo.getAlarmWithReductionKeyIdAt(a1.getReductionKey(), 2).getCounter(), equalTo(2));

        // t=3
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);

        // Delete the alarm
        PseudoClock.getInstance().advanceTime(1, TimeUnit.MILLISECONDS);
        indexer.handleDeletedAlarm(a1.getId(), a1.getReductionKey());

        await().until(() -> repo.getAlarmWithDbIdAt(a1.getId(), 4).getDeletedTime(), notNullValue());
        assertThat(repo.getAlarmWithReductionKeyIdAt(a1.getReductionKey(), 4).getDeletedTime(), notNullValue());
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

    @Test
    public void canGetActiveAlarmsAtTimestampExcluding() {

        // No alarms

        // One alarm

        // Two alarms

        // One alarm

        // No alarms

    }

    @Test
    public void doIt() {
        // can get all state changes for a given alarm
        // can get number of alarms at some given time

        // todo: ensure we can page through the results from the composite query
        // since the size in composite query - applies to deleted alarms too
    }

    @Test
    public void canHandleAlarmSnapshot() {



        // todo;validate sync - should delete an alarm, but not delete an excluded alarm
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
