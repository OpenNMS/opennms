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

package org.opennms.netmgt.alarmd.itests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.opennms.core.test.alarms.AlarmMatchers.acknowledged;
import static org.opennms.core.test.alarms.AlarmMatchers.hasSeverity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.opennms.core.test.alarms.driver.Scenario;
import org.opennms.core.test.alarms.driver.ScenarioResults;
import org.opennms.core.test.alarms.driver.State;
import org.opennms.netmgt.model.OnmsSeverity;

/**
 * This test suite allows us to:
 *  A) Define and play out scenarios using timestamped events and actions.
 *  B) Playback the scenarios
 *  C) Analyze the state of alarms at various points in time.
 *  D) Analyze the state changes of a particular alarm over time.
 *
 * Using these tools we can validate the behavior of the alarms in various scenarios
 * without worrying about the underlying mechanics.
 *
 * @author jwhite
 */
public class AlarmdBlackboxIT {

    /**
     * Verifies the basic life-cycle of a trigger, followed by a clear.
     *
     * Indirectly verifies the cosmicClear and cleanUp automations.
     */
    @Test
    public void canTriggerAndClearAlarm() {
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                .withNodeDownEvent(1, 1)
                .withNodeUpEvent(2, 1)
                .build();
        ScenarioResults results = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(results.getAlarms(0), hasSize(0));
        // t=1, a single problem alarm
        assertThat(results.getAlarms(1), hasSize(1));
        assertThat(results.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));
        // t=2, a (cleared) problem and a resolution
        assertThat(results.getAlarms(2), hasSize(2));
        assertThat(results.getProblemAlarm(2), hasSeverity(OnmsSeverity.CLEARED));
        assertThat(results.getResolutionAlarm(2), hasSeverity(OnmsSeverity.NORMAL));
        // t=∞
        assertThat(results.getAlarmsAtLastKnownTime(), hasSize(0));

        // Now verify the state changes for the particular alarms

        // the problem
        List<State> problemStates = results.getStateChangesForAlarmWithId(results.getProblemAlarm(1).getId());
        assertThat(problemStates, hasSize(3)); // warning, cleared, deleted
        // state 0 at t=1
        assertThat(problemStates.get(0).getTime(), equalTo(1L));
        assertThat(problemStates.get(0).getAlarm(), hasSeverity(OnmsSeverity.MAJOR));
        // state 1 at t=2
        assertThat(problemStates.get(1).getTime(), equalTo(2L));
        assertThat(problemStates.get(1).getAlarm(), hasSeverity(OnmsSeverity.CLEARED));
        assertThat(problemStates.get(1).getAlarm().getCounter(), equalTo(1));
        // state 2 at t in [5m2ms, 10m]
        assertThat(problemStates.get(2).getTime(), greaterThanOrEqualTo(2L + TimeUnit.MINUTES.toMillis(5)));
        assertThat(problemStates.get(2).getTime(), lessThan(TimeUnit.MINUTES.toMillis(10)));
        assertThat(problemStates.get(2).getAlarm(), nullValue()); // DELETED

        // the resolution
        List<State> resolutionStates = results.getStateChangesForAlarmWithId(results.getResolutionAlarm(2).getId());
        assertThat(resolutionStates, hasSize(2)); // cleared, deleted
        // state 0 at t=2
        assertThat(resolutionStates.get(0).getTime(), equalTo(2L));
        assertThat(resolutionStates.get(0).getAlarm(), hasSeverity(OnmsSeverity.NORMAL));
        // state 1 at t in [5m2ms, 10m]
        assertThat(resolutionStates.get(1).getTime(), greaterThanOrEqualTo(2L + TimeUnit.MINUTES.toMillis(5)));
        assertThat(resolutionStates.get(1).getTime(), lessThan(TimeUnit.MINUTES.toMillis(10)));
        assertThat(resolutionStates.get(1).getAlarm(), nullValue()); // DELETED
    }


    /**
     * Indirectly verifies the cosmicClear, unclear and GC automations.
     */
    @Test
    public void canFlapAlarm() {
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                .withNodeDownEvent(1, 1)
                .withNodeUpEvent(2, 1)
                .withNodeDownEvent(3, 1)
                .withNodeUpEvent(4, 1)
                .withNodeDownEvent(5, 1)
                .build();
        ScenarioResults results = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(results.getAlarms(0), hasSize(0));
        // t=1, a single problem alarm
        assertThat(results.getAlarms(1), hasSize(1));
        assertThat(results.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(results.getProblemAlarm(1).getCounter(), equalTo(1));
        // t=2, a (cleared) problem and a resolution
        assertThat(results.getAlarms(2), hasSize(2));
        assertThat(results.getProblemAlarm(2), hasSeverity(OnmsSeverity.CLEARED));
        assertThat(results.getProblemAlarm(2).getCounter(), equalTo(1));
        assertThat(results.getResolutionAlarm(2), hasSeverity(OnmsSeverity.NORMAL));
        assertThat(results.getResolutionAlarm(2).getCounter(), equalTo(1));
        // t=3, a (re-armed) problem and a resolution
        assertThat(results.getAlarms(3), hasSize(2));
        assertThat(results.getProblemAlarm(3), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(results.getProblemAlarm(3).getCounter(), equalTo(2));
        assertThat(results.getResolutionAlarm(3), hasSeverity(OnmsSeverity.NORMAL));
        assertThat(results.getResolutionAlarm(3).getCounter(), equalTo(1));
        // t=4, a (cleared) problem and a resolution
        assertThat(results.getAlarms(4), hasSize(2));
        assertThat(results.getProblemAlarm(4), hasSeverity(OnmsSeverity.CLEARED));
        assertThat(results.getProblemAlarm(4).getCounter(), equalTo(2));
        assertThat(results.getResolutionAlarm(4), hasSeverity(OnmsSeverity.NORMAL));
        assertThat(results.getResolutionAlarm(4).getCounter(), equalTo(2));
        // t=5, a (re-armed) problem and a resolution
        assertThat(results.getAlarms(5), hasSize(2));
        assertThat(results.getProblemAlarm(5), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(results.getProblemAlarm(5).getCounter(), equalTo(3));
        assertThat(results.getResolutionAlarm(5), hasSeverity(OnmsSeverity.NORMAL));
        assertThat(results.getResolutionAlarm(5).getCounter(), equalTo(2));
        // t=∞
        assertThat(results.getAlarmsAtLastKnownTime(), hasSize(0));
    }

    /**
     * Verifies the basic life-cycle of a trigger, followed by a clear.
     *
     * Indirectly verifies the cosmicClear and fullCleanUp automations.
     */
    @Test
    public void canTriggerAcknowledgeAndClearAlarm() {
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                .withNodeDownEvent(1, 1)
                .withAcknowledgmentForNodeDownAlarm(2, 1)
                .withNodeUpEvent(3, 1)
                .build();
        ScenarioResults results = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(results.getAlarms(0), hasSize(0));
        // t=1, a single problem alarm that is not yet acknowledged
        assertThat(results.getAlarms(1), hasSize(1));
        assertThat(results.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(results.getProblemAlarm(1), not(acknowledged()));
        // t=2, a single problem alarm that is acknowledged
        assertThat(results.getAlarms(2), hasSize(1));
        assertThat(results.getProblemAlarm(2), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(results.getProblemAlarm(2), acknowledged());
        // t=3, a (acknowledged & cleared) problem and a resolution
        assertThat(results.getAlarms(3), hasSize(2));
        assertThat(results.getProblemAlarm(3), hasSeverity(OnmsSeverity.CLEARED));
        assertThat(results.getProblemAlarm(3), acknowledged());
        assertThat(results.getResolutionAlarm(3), hasSeverity(OnmsSeverity.NORMAL));
        // t=∞
        assertThat(results.getAlarmsAtLastKnownTime(), hasSize(0));

        // Now verify the state changes for the particular alarms

        // the problem
        List<State> problemStates = results.getStateChangesForAlarmWithId(results.getProblemAlarm(1).getId());
        assertThat(problemStates, hasSize(4)); // major, major+acked, cleared+acked, deleted
        // state 0 at t=1
        assertThat(problemStates.get(0).getTime(), equalTo(1L));
        assertThat(problemStates.get(0).getAlarm(), hasSeverity(OnmsSeverity.MAJOR));
        // state 1 at t=2
        assertThat(problemStates.get(1).getTime(), equalTo(2L));
        assertThat(problemStates.get(1).getAlarm(), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(problemStates.get(1).getAlarm(), acknowledged());
        // state 2 at t=3
        assertThat(problemStates.get(2).getTime(), equalTo(3L));
        assertThat(problemStates.get(2).getAlarm(), hasSeverity(OnmsSeverity.CLEARED));
        assertThat(problemStates.get(2).getAlarm(), acknowledged());
        // state 3 at t in [23h,25h]
        assertThat(problemStates.get(3).getTime(), greaterThanOrEqualTo(TimeUnit.HOURS.toMillis(23)));
        assertThat(problemStates.get(3).getTime(), lessThan(TimeUnit.HOURS.toMillis(25)));
        assertThat(problemStates.get(3).getAlarm(), nullValue()); // DELETED
    }

    /**
     * Verifies the basic life-cycle of a trigger, followed by a clear.
     *
     * Indirectly verifies the fullGC automation.
     */
    @Test
    public void canTriggerAndAcknowledgeAlarm() {
        Scenario scenario = Scenario.builder()
                .withNodeDownEvent(1, 1)
                .withAcknowledgmentForNodeDownAlarm(2, 1)
                .build();
        ScenarioResults results = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(results.getAlarms(0), hasSize(0));
        // t=1, a single problem alarm that is not yet acknowledged
        assertThat(results.getAlarms(1), hasSize(1));
        assertThat(results.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(results.getProblemAlarm(1), not(acknowledged()));
        // t=2, a single problem alarm that is acknowledged
        assertThat(results.getAlarms(2), hasSize(1));
        assertThat(results.getProblemAlarm(2), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(results.getProblemAlarm(2), acknowledged());
        // t=∞
        assertThat(results.getAlarmsAtLastKnownTime(), hasSize(0));

        // Now verify the state changes for the particular alarms

        // the problem
        List<State> problemStates = results.getStateChangesForAlarmWithId(results.getProblemAlarm(1).getId());
        assertThat(problemStates, hasSize(3)); // major, major+acked, deleted
        // state 0 at t=1
        assertThat(problemStates.get(0).getTime(), equalTo(1L));
        assertThat(problemStates.get(0).getAlarm(), hasSeverity(OnmsSeverity.MAJOR));
        // state 1 at t=2
        assertThat(problemStates.get(1).getTime(), equalTo(2L));
        assertThat(problemStates.get(1).getAlarm(), hasSeverity(OnmsSeverity.MAJOR));
        assertThat(problemStates.get(1).getAlarm(), acknowledged());
        // state 2 at t in [7d,9d]
        assertThat(problemStates.get(2).getTime(), greaterThanOrEqualTo(TimeUnit.DAYS.toMillis(2)));
        assertThat(problemStates.get(2).getTime(), lessThan(TimeUnit.DAYS.toMillis(9)));
        assertThat(problemStates.get(2).getAlarm(), nullValue()); // DELETED
    }


    /**
     * Verifies the basic lifecycle of a situation.
     */
    @Test
    public void canCreateSituation() {
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                // Create some node down alarms
                .withNodeDownEvent(1, 1)
                .withNodeDownEvent(2, 2)
                // Create a situation that contains the node down alarms
                .withSituationForNodeDownAlarms(3, "situation#1", 1, 2)
                // Now clear the node down alarms
                .withNodeUpEvent(4, 1)
                .withNodeUpEvent(4, 2)
                .build();
        ScenarioResults results = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(results.getAlarms(0), hasSize(0));
        // t=1, a single problem alarm
        assertThat(results.getAlarms(1), hasSize(1));
        assertThat(results.getProblemAlarm(1), hasSeverity(OnmsSeverity.MAJOR));
        // t=2, two problem alarms
        assertThat(results.getAlarms(2), hasSize(2));
        // t=3, two problem alarms + 1 situation
        assertThat(results.getAlarms(3), hasSize(3)); // the situation is also an alarm, so it is counted here
        assertThat(results.getSituations(3), hasSize(1));
        assertThat(results.getSituation(3), hasSeverity(OnmsSeverity.CRITICAL)); // the situation should be escalated in severity
        // t=4, everything should be cleared
        assertThat(results.getProblemAlarm(4), hasSeverity(OnmsSeverity.CLEARED));
        assertThat(results.getSituation(4), hasSeverity(OnmsSeverity.CLEARED));
        // t=∞
        assertThat(results.getAlarmsAtLastKnownTime(), hasSize(0));
    }

    /**
     * Verifies ACK'ing a situation will ACK all of the related alarms which are unacked.
     */
    @Test
    public void situationAcknowledgmentAcknowledgesAllAlarms() {
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                // Create some node down alarms
                .withNodeDownEvent(1, 1)
                .withNodeDownEvent(2, 2)
                // Create a situation that contains the node down alarms
                .withSituationForNodeDownAlarms(3, "situation#1", 1, 2)
                // Now ACK the situation
                .withAcknowledgmentForSituation(4, "situation#1")
                .build();
        ScenarioResults results = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(results.getAlarms(0), hasSize(0));
        // t=1, a single problem alarm
        assertThat(results.getAlarms(1), hasSize(1));
        // t=2, two problem alarms
        assertThat(results.getAlarms(2), hasSize(2));
        // t=3, two problem alarms + 1 situation, situation is not acknowledged
        assertThat(results.getAlarms(3), hasSize(3)); // the situation is also an alarm, so it is counted here
        assertThat(results.getSituation(3), not(acknowledged()));
        assertThat(results.getAlarms(3), everyItem(not(acknowledged())));
        // t=4, everything should be Ack'd
        assertThat(results.getSituation(4), acknowledged());
        assertThat(results.getAlarms(4), everyItem(acknowledged()));
        // t=∞
        assertThat(results.getAlarmsAtLastKnownTime(), hasSize(0));
    }

    /**
     * Verifies Unacking a situation should unack all previously acked related
     * alarms.
     */
    @Test
    public void situationUnAcknowledgmentUnAcknowledgesAllAlarms() {
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                // Create some node down alarms
                .withNodeDownEvent(1, 1)
                .withNodeDownEvent(2, 2)
                // Create a situation that contains the node down alarms
                .withSituationForNodeDownAlarms(3, "situation#1", 1, 2)
                // Now ACK the situation
                .withAcknowledgmentForSituation(4, "situation#1")
                .withUnAcknowledgmentForSituation(5, "situation#1")
                .build();
        ScenarioResults results = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(results.getAlarms(0), hasSize(0));
        // t=1, a single problem alarm
        assertThat(results.getAlarms(1), hasSize(1));
        // t=2, two problem alarms
        assertThat(results.getAlarms(2), hasSize(2));
        // t=3, two problem alarms + 1 situation, situation is not acknowledged
        assertThat(results.getAlarms(3), hasSize(3)); // the situation is also an alarm, so it is counted here
        assertThat(results.getSituation(3), not(acknowledged()));
        assertThat(results.getAlarms(3), everyItem(not(acknowledged())));
        // t=4, everything should be Ack'd
        assertThat(results.getSituation(4), acknowledged());
        assertThat(results.getAlarms(4), everyItem(acknowledged()));
        // t=5, all alarms and situation should be unacked
        assertThat(results.getAlarms(5), everyItem(not(acknowledged())));
        // t=∞
        assertThat(results.getAlarmsAtLastKnownTime(), hasSize(0));
    }

    @Test
    public void alarmsAckSituation() {
        // A situation is deemed "acked" if all the related alarms are acked
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                // Create some node down alarms
                .withNodeDownEvent(1, 1)
                .withNodeDownEvent(2, 2)
                // Create a situation that contains the node down alarms
                .withSituationForNodeDownAlarms(3, "situation#1", 1, 2)
                // Now ACK both alarms
                .withAcknowledgmentForNodeDownAlarm(4, 1)
                .withAcknowledgmentForNodeDownAlarm(4, 2)
                .build();
        ScenarioResults results = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(results.getAlarms(0), hasSize(0));
        // t=1, a single problem alarm
        assertThat(results.getAlarms(1), hasSize(1));
        // t=2, two problem alarms
        assertThat(results.getAlarms(2), hasSize(2));
        // t=3, two problem alarms + 1 situation, situation is not acknowledged
        assertThat(results.getAlarms(3), hasSize(3)); // the situation is also an alarm, so it is counted here
        assertThat(results.getSituation(3), not(acknowledged()));

        // t=4, everything should be Ack'd
        assertThat(results.getSituation(4), acknowledged());
        assertThat(results.getAlarms(4), everyItem(acknowledged()));
        // t=∞
        assertThat(results.getAlarmsAtLastKnownTime(), hasSize(0));
    }

    @Test
    public void alarmsUnAckSituation() {
        // If a new unacked alarm gets added to an acked situation, or an existing related alarm is unacknowledged,
        // then the situation itself should be unacked
        // (but all other related alarms which were acked should remain acked)
        Scenario scenario = Scenario.builder()
                .withLegacyAlarmBehavior()
                // Create some node down alarms
                .withNodeDownEvent(1, 1)
                .withNodeDownEvent(2, 2)
                // Create a situation that contains the node down alarms
                .withSituationForNodeDownAlarms(3, "situation#1", 1, 2)
                // Now ACK the situation
                .withAcknowledgmentForSituation(4, "situation#1")
                // now un-acknowledge one of the alarms
                .withUnAcknowledgmentForNodeDownAlarm(5, 1)
                .build();
        ScenarioResults results = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(results.getAlarms(0), hasSize(0));
        // t=1, a single problem alarm
        assertThat(results.getAlarms(1), hasSize(1));
        // t=2, two problem alarms
        assertThat(results.getAlarms(2), hasSize(2));
        // t=3, two problem alarms + 1 situation, situation is not acknowledged
        assertThat(results.getAlarms(3), hasSize(3)); // the situation is also an alarm, so it is counted here
        assertThat(results.getSituation(3), not(acknowledged()));

        // t=4, everything should be Ack'd
        assertThat(results.getSituation(4), acknowledged());
        assertThat(results.getAlarms(4), everyItem(acknowledged()));
        // t=5, alarm and situation should be unacked
        assertThat(results.getSituation(5), not(acknowledged()));
        // t=6, but other alarm should still be ACK'd
        assertThat(results.getAlarms(6), not(everyItem(not(acknowledged()))));
        // t=∞
        assertThat(results.getAlarmsAtLastKnownTime(), hasSize(0));
    }

    @Test
    public void oldAlarmsCanUnAckSituation() {
        // If an older unacked alarm gets added to an acked situation, or an
        // existing related alarm is unacknowledged,
        // then the situation itself should be unacked (but all other related
        // alarms which were acked should remain acked)
        Scenario scenario = Scenario.builder().withLegacyAlarmBehavior()
                // Create some node down alarms
                .withNodeDownEvent(1, 1).withNodeDownEvent(2, 2).withNodeDownEvent(3, 3)
                // Create a situation that contains the node down alarms
                .withSituationForNodeDownAlarms(4, "situation#1", 2, 3)
                // Now ACK the situation
                .withAcknowledgmentForSituation(5, "situation#1")
                // now add old un-acknowledged alarm to situation
                .withSituationForNodeDownAlarms(6, "situation#1", 2, 3, 1).build();
        // .withCorrelationAddsAlarm(6, "situation#1", 1).build();
        ScenarioResults results = scenario.play();

        // Verify the set of alarms at various points in time

        // t=0, no alarms
        assertThat(results.getAlarms(0), hasSize(0));
        // t=1, a single problem alarm
        assertThat(results.getAlarms(1), hasSize(1));
        // t=2, two problem alarms
        assertThat(results.getAlarms(2), hasSize(2));
        // t=3, three problem alarms
        assertThat(results.getAlarms(3), hasSize(3));
        // t=4, two problem alarms + 1 situation + 1 other alarm, situation is not acknowledged
        assertThat(results.getAlarms(4), hasSize(4)); // the situation is also
                                                      // an alarm, so it is
                                                      // counted here
        assertThat(results.getSituation(4), not(acknowledged()));

        // t=5, Situation and 2 Alarms should be Ack'd
        assertThat(results.getSituation(5), acknowledged());
        assertThat(results.getAcknowledgedAlarms(5), hasSize(3));
        assertThat(results.getUnAcknowledgedAlarms(5), hasSize(1));
        // t=6, alarm and situation should be unacked
        assertThat(results.getSituation(6), not(acknowledged()));
        assertThat(results.getAlarmAt(6, 1), not(acknowledged()));
        // but other alarm should still be ACK'd
        assertThat(results.getAcknowledgedAlarms(6), hasSize(2));
        // t=∞
        assertThat(results.getAlarmsAtLastKnownTime(), hasSize(0));
    }

}
