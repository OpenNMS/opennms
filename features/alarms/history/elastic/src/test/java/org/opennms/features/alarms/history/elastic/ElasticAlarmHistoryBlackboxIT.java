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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.opennms.core.test.alarms.AlarmMatchers.acknowledged;
import static org.opennms.core.test.alarms.AlarmMatchers.hasSeverity;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.reindex.ReindexPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.alarms.driver.Scenario;
import org.opennms.core.test.alarms.driver.ScenarioResults;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.core.test.elastic.ElasticSearchServerConfig;
import org.opennms.core.time.PseudoClock;
import org.opennms.features.alarms.history.api.AlarmHistoryRepository;
import org.opennms.features.alarms.history.api.AlarmState;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

import io.searchbox.client.JestClient;

/**
 * These test cases leverage the Alarm Test Driver to
 * issue callbacks which are used to drive the indexing
 * in Elasticsearch.
 *
 * Given that we are capturing the changes over time, we
 * then go back and look at the alarm states at particular
 * points in time, and make sure that they match expectations.
 */
public class ElasticAlarmHistoryBlackboxIT {

    private JestClient jestClient;
    private AlarmHistoryRepository alarmHistoryRepo;

    @Rule
    @SuppressWarnings("unchecked")
    public ElasticSearchRule elasticSearchRule = new ElasticSearchRule(new ElasticSearchServerConfig()
            .withPlugins(PainlessPlugin.class, ReindexPlugin.class)
    );

    @Before
    public void setUp() throws IOException {
        // The pseudo clock is driven by the alarm test driver, and used
        // by the ES indexer so that the timestamps line up
        // Reset the clock to 0 before every test
        PseudoClock.getInstance().reset();

        RestClientFactory restClientFactory = new RestClientFactory(elasticSearchRule.getUrl());
        jestClient = restClientFactory.createClient();
        alarmHistoryRepo = new ElasticAlarmHistoryRepository(jestClient, IndexStrategy.MONTHLY, new IndexSettings());

        // Wait until ES is up and running - initially there should be no documents
        await().atMost(1, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(alarmHistoryRepo::getActiveAlarmsNow, hasSize(equalTo(0)));
    }

    @After
    public void tearDown() throws IOException {
        if (jestClient != null) {
            jestClient.close();
            jestClient = null;
        }
    }

    private AlarmState getFirstAlarmWithType(long time, int type, String typeDescr) {
        return alarmHistoryRepo.getActiveAlarmsAt(time).stream()
                .filter(a -> a.getType() == type)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No " + typeDescr + " alarms at time: " + time));
    }

    private AlarmState getProblemAlarmAt(long t) {
        return getFirstAlarmWithType(t, OnmsAlarm.PROBLEM_TYPE, "problem");
    }

    private AlarmState getResolutionAlarmAt(long t) {
        return getFirstAlarmWithType(t, OnmsAlarm.RESOLUTION_TYPE, "resolution");
    }

    private List<AlarmState> getSituationsAt(long time) {
        return alarmHistoryRepo.getActiveAlarmsAt(time).stream()
                .filter(AlarmState::isSituation)
                .collect(Collectors.toList());
    }

    private AlarmState getSituationAt(long time) {
        return getSituationsAt(time).stream()
                .findFirst()
                .orElse(null);
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

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(0), hasSize(0));

        // t=1, a single problem alarm that is not yet acknowledged
        assertThat(scenarioResults.getAlarms(1), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(1), not(acknowledged()));

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(1), hasSize(1));
        assertThat(getProblemAlarmAt(1), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(getProblemAlarmAt(1), not(ExtAlarmsMatchers.acknowledged()));

        // t=2, a single problem alarm that is acknowledged
        assertThat(scenarioResults.getAlarms(2), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(2), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(2), acknowledged());

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(2), hasSize(1));
        assertThat(getProblemAlarmAt(2), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(getProblemAlarmAt(2), ExtAlarmsMatchers.acknowledged());

        // t=3, a (acknowledged & cleared) problem and a resolution
        assertThat(scenarioResults.getAlarms(3), hasSize(2));
        assertThat(scenarioResults.getProblemAlarm(3), hasSeverity(OnmsSeverity.CLEARED));
        assertThat(scenarioResults.getProblemAlarm(3), acknowledged());
        assertThat(scenarioResults.getResolutionAlarm(3), hasSeverity(OnmsSeverity.NORMAL));

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(4), hasSize(2));
        assertThat(getProblemAlarmAt(4), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.CLEARED));
        assertThat(getProblemAlarmAt(4), ExtAlarmsMatchers.acknowledged());
        assertThat(getResolutionAlarmAt(4), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.NORMAL));

        // t=∞
        Long lastKnownTime = scenarioResults.getLastKnownTime();
        assertThat(scenarioResults.getAlarms(lastKnownTime), hasSize(0));

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(lastKnownTime), hasSize(0));
    }

    @Test
    public void canIndexSituationsInElasticsearch() {
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
                .withSituationForNodeDownAlarms(5, "situation#1", 1, 2, 3)
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

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(0), hasSize(0));

        // t=1, a single problem alarm
        assertThat(scenarioResults.getAlarms(1), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(1), hasSize(1));
        assertThat(getProblemAlarmAt(1), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));

        // t=2, two problem alarms
        assertThat(scenarioResults.getAlarms(2), hasSize(2));

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(2), hasSize(2));

        // t=3, two problem alarms + 1 situation
        assertThat(scenarioResults.getAlarms(3), hasSize(3)); // the situation is also an alarm, so it is counted here
        assertThat(scenarioResults.getSituations(3), hasSize(1));
        assertThat(scenarioResults.getSituation(3), hasSeverity(OnmsSeverity.CRITICAL)); // situations have max(severity) + 1

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(3), hasSize(3));
        assertThat(getSituationsAt(3), hasSize(1));

        final AlarmState situation = getSituationAt(3);
        assertThat(situation.getRelatedAlarms(), hasSize(2));

        // t=4, three problem alarms + 1 situation - only 2 of the problems are in the situation
        assertThat(scenarioResults.getAlarms(4), hasSize(4)); // the situation is also an alarm, so it is counted here
        assertThat(getSituationAt(3).getRelatedAlarms(), hasSize(2));

        // t=5, three problem alarms + 1 situation - all 3 alarms are in the situation
        assertThat(scenarioResults.getAlarms(5), hasSize(4)); // the situation is also an alarm, so it is counted here
        assertThat(getSituationAt(5).getRelatedAlarms(), hasSize(3));

        // t=∞
        Long lastKnownTime = scenarioResults.getLastKnownTime();
        assertThat(scenarioResults.getAlarms(lastKnownTime), hasSize(0));
        assertThat(alarmHistoryRepo.getActiveAlarmsAt(lastKnownTime), hasSize(0));
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

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(0), hasSize(0));

        // t=1, a single problem alarm that is not yet acknowledged
        assertThat(scenarioResults.getAlarms(1), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(1), not(acknowledged()));

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(1), hasSize(1));
        assertThat(getProblemAlarmAt(1), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(getProblemAlarmAt(1), not(ExtAlarmsMatchers.acknowledged()));

        // t=2, a single problem alarm that is acknowledged
        assertThat(scenarioResults.getAlarms(2), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(2), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(2), acknowledged());

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(2), hasSize(1));
        assertThat(getProblemAlarmAt(2), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(getProblemAlarmAt(2), ExtAlarmsMatchers.acknowledged());

        // t=3, a single problem alarm that is no longer acknowledged
        assertThat(scenarioResults.getAlarms(3), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(3), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(3), not(acknowledged()));

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(3), hasSize(1));
        assertThat(getProblemAlarmAt(3), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(getProblemAlarmAt(3), not(ExtAlarmsMatchers.acknowledged()));

        // t=4, a single problem alarm that is acknowledged
        assertThat(scenarioResults.getAlarms(4), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(4), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(4), acknowledged());

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(4), hasSize(1));
        assertThat(getProblemAlarmAt(4), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(getProblemAlarmAt(4), ExtAlarmsMatchers.acknowledged());

        // t=5, a single problem alarm that is no longer acknowledged
        assertThat(scenarioResults.getAlarms(5), hasSize(1));
        assertThat(scenarioResults.getProblemAlarm(5), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(scenarioResults.getProblemAlarm(5), not(acknowledged()));

        assertThat(alarmHistoryRepo.getActiveAlarmsAt(5), hasSize(1));
        assertThat(getProblemAlarmAt(5), ExtAlarmsMatchers.hasSeverity(OnmsSeverity.MAJOR));
        assertThat(getProblemAlarmAt(5), not(ExtAlarmsMatchers.acknowledged()));

        // t=∞
        Long lastKnownTime = scenarioResults.getLastKnownTime();
        assertThat(scenarioResults.getAlarms(lastKnownTime), hasSize(0));
        assertThat(alarmHistoryRepo.getActiveAlarmsAt(lastKnownTime), hasSize(0));
    }

    private Runnable waitForNAlarmsInES(int numAlarms) {
        return () -> {
            // Don't tear down until we have the expected number of alarms in ES and they are all marked as deleted.
            await().atMost(2, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                    .until(() -> alarmHistoryRepo.getLastStateOfAllAlarms(0, System.currentTimeMillis()),
                            hasSize(equalTo(numAlarms)));
            // All of the alarms are in ES now, wait until they are deleted
            await().atMost(2, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                    .until(() -> alarmHistoryRepo.getLastStateOfAllAlarms(0, System.currentTimeMillis()),
                            everyItem(ExtAlarmsMatchers.wasDeleted()));
        };
    }

}
