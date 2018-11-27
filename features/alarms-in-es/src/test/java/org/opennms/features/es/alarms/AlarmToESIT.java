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

package org.opennms.features.es.alarms;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.opennms.core.test.alarms.AlarmMatchers.acknowledged;
import static org.opennms.core.test.alarms.AlarmMatchers.hasSeverity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.painless.PainlessPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.alarms.driver.Scenario;
import org.opennms.core.test.alarms.driver.ScenarioResults;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.core.time.PseudoClock;
import org.opennms.features.es.alarms.dto.AlarmDocumentDTO;
import org.opennms.netmgt.alarmd.AlarmLifecycleListenerManager;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.plugins.elasticsearch.rest.RestClientFactory;

import io.searchbox.client.JestClient;

public class AlarmToESIT {
    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";

    private JestClient jestClient;
    private AlarmsFromES alarmsFromES;

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
    public static void setUpClass() {
        // Required, since deletes can miss right after indexing
        System.setProperty(AlarmLifecycleListenerManager.ALARM_SNAPSHOT_INTERVAL_MS_SYS_PROP, "5000");
    }

    @Before
    public void setUp() throws IOException {
        // The pseudo clock is driven by the alarm test driver, and used
        // by the ES indexer so that the timestamps line up
        // Reset the clock to 0 before every test
        PseudoClock.getInstance().reset();

        RestClientFactory restClientFactory = new RestClientFactory("http://localhost:" + HTTP_PORT);
        jestClient = restClientFactory.createClient();
        alarmsFromES = new AlarmsFromES(jestClient);

        // Wait until ES is up and running - initially there should be no documents
        await().atMost(1, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(alarmsFromES::getAllAlarms, hasSize(equalTo(0)));
    }

    @After
    public void tearDown() throws IOException {
        if (jestClient != null) {
            jestClient.close();
            jestClient = null;
        }
    }

    @Test
    public void canIndexAlarmsInElasticsearch() {
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                .withNodeDownEvent(1, 1)
                .withAcknowledgmentForNodeDownAlarm(2, 1)
                .withNodeUpEvent(3, 1)
                .awaitUntil(waitForNAlarmsInES(2))
                .build();
        // Execute the scenario
        ScenarioResults scenarioResults = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(scenarioResults.getAlarms(0), hasSize(0));

        assertThat(alarmsFromES.getAlarmsAt(0), hasSize(0));

        // t=1, a single problem alarm that is not yet acknowledged
        assertThat(scenarioResults.getAlarms(1), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(1), not(acknowledged()));

        assertThat(alarmsFromES.getAlarmsAt(1), hasSize(1));
        assertThat(alarmsFromES.getProblemAlarmAt(1), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(alarmsFromES.getProblemAlarmAt(1), not(ExtAlarmsMatchers.acknowledged()));

        // t=2, a single problem alarm that is acknowledged
        assertThat(scenarioResults.getAlarms(2), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(2), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(2), acknowledged());

        assertThat(alarmsFromES.getAlarmsAt(2), hasSize(1));
        assertThat(alarmsFromES.getProblemAlarmAt(2), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(alarmsFromES.getProblemAlarmAt(2), ExtAlarmsMatchers.acknowledged());

        // t=3, a (acknowledged & cleared) problem and a resolution
        assertThat(scenarioResults.getAlarms(3), hasSize(2));
        assertThat(scenarioResults.getProblemAlarm(3), hasSeverity(OnmsSeverity.CLEARED));
        assertThat(scenarioResults.getProblemAlarm(3), acknowledged());
        assertThat(scenarioResults.getResolutionAlarm(3), hasSeverity(OnmsSeverity.NORMAL));

        assertThat(alarmsFromES.getAlarmsAt(4), hasSize(2));
        assertThat(alarmsFromES.getProblemAlarmAt(4), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.CLEARED));
        assertThat(alarmsFromES.getProblemAlarmAt(4), ExtAlarmsMatchers.acknowledged());
        assertThat(alarmsFromES.getResolutionAlarmAt(4), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.NORMAL));

        // t=∞
        Long lastKnownTime = scenarioResults.getLastKnownTime();
        assertThat(scenarioResults.getAlarms(lastKnownTime), hasSize(0));

        assertThat(alarmsFromES.getAlarmsAt(lastKnownTime), hasSize(0));
    }

    @Test
    public void canIndexSituationsInElasticsearch() throws InterruptedException {
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                // Create some node down alarms
                .withNodeDownEvent(1, 1)
                .withNodeDownEvent(2, 2)
                // Create a situation that contains the node down alarms
                .withSituationForNodeDownAlarms(3, "situation#1", 1, 2)
                // Create another another node down alarm
                .withNodeDownEvent(4, 3)
                // Add the new nodeDown alarm to the situation
                .withSituationForNodeDownAlarms(5, "situation#1", 3)
                // Clear the nodeDown alarms
                .withNodeUpEvent(6, 1)
                .withNodeUpEvent(6, 2)
                .withNodeUpEvent(6, 3)
                .awaitUntil(waitForNAlarmsInES(7))
                .build();
        ScenarioResults scenarioResults = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(scenarioResults.getAlarms(0), hasSize(0));

        assertThat(alarmsFromES.getAlarmsAt(0), hasSize(0));

        // t=1, a single problem alarm
        assertThat(scenarioResults.getAlarms(1), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));

        assertThat(alarmsFromES.getAlarmsAt(1), hasSize(1));
        assertThat(alarmsFromES.getProblemAlarmAt(1), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));

        // t=2, two problem alarms
        assertThat(scenarioResults.getAlarms(2), hasSize(2));

        assertThat(alarmsFromES.getAlarmsAt(2), hasSize(2));

        // t=3, two problem alarms + 1 situation
        assertThat(scenarioResults.getAlarms(3), hasSize(3)); // the situation is also an alarm, so it is counted here
        assertThat(scenarioResults.getSituations(3), hasSize(1));
        assertThat(scenarioResults.getSituation(3), hasSeverity(OnmsSeverity.CRITICAL)); // situations have max(severity) + 1

        assertThat(alarmsFromES.getAlarmsAt(3), hasSize(3));
        assertThat(alarmsFromES.getSituationsAt(3), hasSize(1));

        final AlarmDocumentDTO situation = alarmsFromES.getSituationAt(3);
        assertThat(situation.getRelatedAlarms(), hasSize(2));

        // t=4, three problem alarms + 1 situation - only 2 of the problems are in the situation
        assertThat(scenarioResults.getAlarms(4), hasSize(4)); // the situation is also an alarm, so it is counted here
        assertThat(alarmsFromES.getSituationAt(3).getRelatedAlarms(), hasSize(2));

        // t=5, three problem alarms + 1 situation - all 3 alarms are in the situation
        assertThat(scenarioResults.getAlarms(5), hasSize(4)); // the situation is also an alarm, so it is counted here
        assertThat(alarmsFromES.getSituationAt(5).getRelatedAlarms(), hasSize(3));

        // t=∞
        Long lastKnownTime = scenarioResults.getLastKnownTime();
        assertThat(scenarioResults.getAlarms(lastKnownTime), hasSize(0));
        assertThat(alarmsFromES.getAlarmsAt(lastKnownTime), hasSize(0));
    }

    @Test
    public void canMaintainMultipleStateChanges() {
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                .withNodeDownEvent(1, 1)
                .withAcknowledgmentForNodeDownAlarm(2, 1)
                .withUnAcknowledgmentForNodeDownAlarm(3, 1)
                .withAcknowledgmentForNodeDownAlarm(4, 1)
                .withUnAcknowledgmentForNodeDownAlarm(5, 1)
                .awaitUntil(waitForNAlarmsInES(1))
                .build();
        // Execute the scenario
        ScenarioResults scenarioResults = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(scenarioResults.getAlarms(0), hasSize(0));

        assertThat(alarmsFromES.getAlarmsAt(0), hasSize(0));

        // t=1, a single problem alarm that is not yet acknowledged
        assertThat(scenarioResults.getAlarms(1), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(1), not(acknowledged()));

        assertThat(alarmsFromES.getAlarmsAt(1), hasSize(1));
        assertThat(alarmsFromES.getProblemAlarmAt(1), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(alarmsFromES.getProblemAlarmAt(1), not(ExtAlarmsMatchers.acknowledged()));

        // t=2, a single problem alarm that is acknowledged
        assertThat(scenarioResults.getAlarms(2), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(2), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(2), acknowledged());

        assertThat(alarmsFromES.getAlarmsAt(2), hasSize(1));
        assertThat(alarmsFromES.getProblemAlarmAt(2), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(alarmsFromES.getProblemAlarmAt(2), ExtAlarmsMatchers.acknowledged());

        // t=3, a single problem alarm that is no longer acknowledged
        assertThat(scenarioResults.getAlarms(3), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(3), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(3), not(acknowledged()));

        assertThat(alarmsFromES.getAlarmsAt(3), hasSize(1));
        assertThat(alarmsFromES.getProblemAlarmAt(3), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(alarmsFromES.getProblemAlarmAt(3), not(ExtAlarmsMatchers.acknowledged()));

        // t=4, a single problem alarm that is acknowledged
        assertThat(scenarioResults.getAlarms(4), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(4), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(4), acknowledged());

        assertThat(alarmsFromES.getAlarmsAt(4), hasSize(1));
        assertThat(alarmsFromES.getProblemAlarmAt(4), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(alarmsFromES.getProblemAlarmAt(4), ExtAlarmsMatchers.acknowledged());

        // t=5, a single problem alarm that is no longer acknowledged
        assertThat(scenarioResults.getAlarms(5), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(5), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(5), not(acknowledged()));

        assertThat(alarmsFromES.getAlarmsAt(5), hasSize(1));
        assertThat(alarmsFromES.getProblemAlarmAt(5), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(alarmsFromES.getProblemAlarmAt(5), not(ExtAlarmsMatchers.acknowledged()));

        // t=∞
        Long lastKnownTime = scenarioResults.getLastKnownTime();
        assertThat(scenarioResults.getAlarms(lastKnownTime), hasSize(0));

        assertThat(alarmsFromES.getAlarmsAt(lastKnownTime), hasSize(0));
    }

    private Runnable waitForNAlarmsInES(int numAlarms) {
        return () -> {
            // Don't tear down until we have the expected number of alarms in ES and they are all marked as deleted.
            await().atMost(1, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                    .until(alarmsFromES::getAllAlarms, hasSize(equalTo(numAlarms)));
            // All of the alarms are in ES now, wait until they are deleted
            await().atMost(1, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                    .until(alarmsFromES::getAllAlarms, everyItem(ExtAlarmsMatchers.wasDeleted()));
        };
    }

}
