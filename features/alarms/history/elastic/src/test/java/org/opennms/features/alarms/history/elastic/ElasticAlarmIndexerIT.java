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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.painless.PainlessPlugin;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.core.time.PseudoClock;
import org.opennms.features.alarms.history.api.AlarmHistoryRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.plugins.elasticsearch.rest.RestClientFactory;

import com.codahale.metrics.MetricRegistry;
import com.jayway.awaitility.Awaitility;

import io.searchbox.client.JestClient;

public class ElasticAlarmIndexerIT {
    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";

    private JestClient jestClient;
    private AlarmHistoryRepository alarmHistoryRepo;
    private ElasticAlarmIndexer elasticAlarmIndexer;

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

    @BeforeClass
    public static void classSetUp() {
        MockLogAppender.setupLogging(true, "DEBUG");
        Awaitility.setDefaultPollDelay(1, TimeUnit.SECONDS);
        Awaitility.setDefaultPollInterval(5, TimeUnit.SECONDS);
    }

    @Before
    public void setUp() throws IOException {
        PseudoClock.getInstance().reset();

        RestClientFactory restClientFactory = new RestClientFactory("http://localhost:" + HTTP_PORT);
        jestClient = restClientFactory.createClient();
        alarmHistoryRepo = new ElasticAlarmHistoryRepository(jestClient);

        MetricRegistry metrics = new MetricRegistry();
        TemplateInitializerForAlarms templateInitializerForAlarms = new TemplateInitializerForAlarms(jestClient);
        elasticAlarmIndexer = new ElasticAlarmIndexer(metrics, jestClient, templateInitializerForAlarms);
        elasticAlarmIndexer.setUsePseudoClock(true);
        elasticAlarmIndexer.init();

        // Wait until ES is up and running - initially there should be no documents
        await().atMost(1, TimeUnit.MINUTES).ignoreExceptions()
                .until(alarmHistoryRepo::getActiveAlarmsNow, hasSize(equalTo(0)));
    }

    @Ignore
    @Test
    public void canHandleAlarmSnapshots() {
        final int N = 100;
        final long now = PseudoClock.getInstance().getTime();
        final List<OnmsAlarm> alarms = IntStream.range(0, N)
                .mapToObj(i -> createAlarm(i, now))
                .collect(Collectors.toList());

        // Trigger the snapshot
        issueSnapshotWithPreAndPostCalls(alarms);

        // Wait for the alarms to be indexed
        await().atMost(1, TimeUnit.MINUTES).until(() ->
                alarmHistoryRepo.getNumActiveAlarmsNow(), equalTo(N));

        // Now trigger another snapshot with an empty list of alarms
        issueSnapshotWithPreAndPostCalls(Collections.emptyList());

        // Wait for the deletes to be indexed
        await().atMost(1, TimeUnit.MINUTES).until(() ->
                alarmHistoryRepo.getNumActiveAlarmsNow(), equalTo(0));
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
