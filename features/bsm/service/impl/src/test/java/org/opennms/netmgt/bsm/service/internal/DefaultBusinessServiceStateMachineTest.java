/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.bsm.mock.MockAlarmWrapper;
import org.opennms.netmgt.bsm.mock.MockBusinessServiceHierarchy;
import org.opennms.netmgt.bsm.service.AlarmProvider;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverityAbove;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.bsm.test.LoggingStateChangeHandler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class DefaultBusinessServiceStateMachineTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void canLookupNewAlarmsWhenReloading() {
        // Create a simple hierarchy
        MockBusinessServiceHierarchy h = MockBusinessServiceHierarchy.builder()
                .withBusinessService(1)
                    .withReductionKey(1, "a1")
                    .commit()
                .build();
        BusinessService b1 = h.getBusinessServiceById(1);
        Edge a1 = h.getEdgeByReductionKey("a1");

        // Setup the state machine
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        LoggingStateChangeHandler stateChangeHandler = new LoggingStateChangeHandler();
        stateMachine.addHandler(stateChangeHandler, Maps.newHashMap());
        stateMachine.setBusinessServices(h.getBusinessServices());

        stateMachine.setAlarmProvider(new AlarmProvider() {
            @Override
            public Map<String, AlarmWrapper> lookup(Set<String> reductionKeys) {

                if (reductionKeys.contains("a2")) {
                    return ImmutableMap.<String, AlarmWrapper>builder()
                            .put("a2", new MockAlarmWrapper("a2", Status.CRITICAL))
                            .build();
                }
                return new HashMap<>();
            }
        });

        // Verify the initial state
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(b1));
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(a1));
        assertEquals(0, stateChangeHandler.getStateChanges().size());

        // Send an alarm and verify the updated state
        stateMachine.handleNewOrUpdatedAlarm(new MockAlarmWrapper("a1", Status.MINOR));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(b1));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(a1));
        assertEquals(1, stateChangeHandler.getStateChanges().size());

        // Update the hierarchy and reload the state machine
        h = MockBusinessServiceHierarchy.builder()
                .withBusinessService(1)
                    .withReductionKey(1, "a1")
                    .withReductionKey(2, "a2")
                    .commit()
                .build();
        stateMachine.setBusinessServices(h.getBusinessServices());
        Edge a2 = h.getEdgeByReductionKey("a2");

        // The state should be upgraded
        assertEquals(Status.CRITICAL, stateMachine.getOperationalStatus(b1));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(a1));
        assertEquals(Status.CRITICAL, stateMachine.getOperationalStatus(a2));
        // One additional state change event should have been generated
        assertEquals(2, stateChangeHandler.getStateChanges().size());
    }

    @Test
    public void canReloadTheStateMachineWhilePreservingState() {
        // Create a simple hierarchy
        MockBusinessServiceHierarchy h = MockBusinessServiceHierarchy.builder()
                .withBusinessService(1)
                    .withReductionKey(1, "a1")
                    .commit()
                .build();
        BusinessService b1 = h.getBusinessServiceById(1);

        // Setup the state machine
        BusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        LoggingStateChangeHandler stateChangeHandler = new LoggingStateChangeHandler();
        stateMachine.addHandler(stateChangeHandler, Maps.newHashMap());
        stateMachine.setBusinessServices(h.getBusinessServices());

        // Verify the initial state
        assertEquals(Status.NORMAL, stateMachine.getOperationalStatus(b1));
        assertEquals(0, stateChangeHandler.getStateChanges().size());

        // Send an alarm and verify the updated state
        stateMachine.handleNewOrUpdatedAlarm(new MockAlarmWrapper("a1", Status.CRITICAL));
        assertEquals(Status.CRITICAL, stateMachine.getOperationalStatus(b1));
        assertEquals(1, stateChangeHandler.getStateChanges().size());

        // Now reload the state machine without making any changes to the hierarchies
        stateMachine.setBusinessServices(h.getBusinessServices());

        // The original status should remain
        assertEquals(Status.CRITICAL, stateMachine.getOperationalStatus(b1));
        // No additional state changes events should have been generated
        assertEquals(1, stateChangeHandler.getStateChanges().size());
    }

    @Test
    public void canPerformRootCauseAndImpactAnalysis() {
        // Create a hierarchy using all of the available reduction functions
        HighestSeverity highestSeverity = new HighestSeverity();
        Threshold threshold = new Threshold();
        threshold.setThreshold(0.5f);
        HighestSeverityAbove highestSeverityAbove = new HighestSeverityAbove();
        highestSeverityAbove.setThreshold(Status.MINOR);

        MockBusinessServiceHierarchy h = MockBusinessServiceHierarchy.builder()
                .withBusinessService(1)
                    .withName("b1")
                    .withReductionFunction(highestSeverityAbove)
                    .withBusinessService(2)
                        .withName("b2")
                        .withReductionFunction(highestSeverity)
                        .withReductionKey(21, "a1")
                        .withReductionKey(22, "a2")
                        .withReductionKey(23, "a3")
                    .commit()
                    .withBusinessService(3)
                        .withName("b3")
                        .withReductionFunction(threshold)
                        .withReductionKey(34, "a4")
                        .withReductionKey(35, "a5")
                        .withReductionKey(36, "a6")
                        .withReductionKey(37, "a7")
                    .commit()
                .commit()
                .build();

        // Setup the state machine
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(h.getBusinessServices());

        // Bump b2 to MINOR, caused by a1
        stateMachine.handleNewOrUpdatedAlarm(new MockAlarmWrapper("a1", Status.MINOR));
        // Bump b3 to MAJOR, caused by a4, a6 and a7
        stateMachine.handleNewOrUpdatedAlarm(new MockAlarmWrapper("a4", Status.MAJOR));
        stateMachine.handleNewOrUpdatedAlarm(new MockAlarmWrapper("a6", Status.CRITICAL));
        stateMachine.handleNewOrUpdatedAlarm(new MockAlarmWrapper("a7", Status.MAJOR));
        // Bumped b1 to MAJOR, caused by b3

        // Verify the state
        assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(h.getBusinessServiceById(1)));
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(h.getBusinessServiceById(2)));
        assertEquals(Status.MAJOR, stateMachine.getOperationalStatus(h.getBusinessServiceById(3)));

        // Calculate and verify the root causes, b2 caused by a1
        List<GraphVertex> causedby = stateMachine.calculateRootCause(h.getBusinessServiceById(2));
        assertEquals(1, causedby.size());
        assertEquals("a1", causedby.get(0).getReductionKey());

        // b3 caused by a4, a6 and a7
        causedby = stateMachine.calculateRootCause(h.getBusinessServiceById(3));
        assertEquals(3, causedby.size());
        assertEquals("a4", causedby.get(0).getReductionKey());
        assertEquals("a6", causedby.get(1).getReductionKey());
        assertEquals("a7", causedby.get(2).getReductionKey());

        // b1 caused by b3, which was in turn caused by a4, a6 and a7
        causedby = stateMachine.calculateRootCause(h.getBusinessServiceById(1));
        assertEquals(4, causedby.size());
        assertEquals(Long.valueOf(3), causedby.get(0).getBusinessService().getId());
        assertEquals("a4", causedby.get(1).getReductionKey());
        assertEquals("a6", causedby.get(2).getReductionKey());
        assertEquals("a7", causedby.get(3).getReductionKey());

        // Now calculate the impact, a1 impacts b2
        List<GraphVertex> impacts = stateMachine.calculateImpact("a1");
        assertEquals(1, impacts.size());
        assertEquals("b2", impacts.get(0).getBusinessService().getName());

        // a4 impacts b3 which impacts b1
        impacts = stateMachine.calculateImpact("a4");
        assertEquals(2, impacts.size());
        assertEquals("b3", impacts.get(0).getBusinessService().getName());
        assertEquals("b1", impacts.get(1).getBusinessService().getName());

        // b3 impacts b1
        impacts = stateMachine.calculateImpact(h.getBusinessServiceById(3));
        assertEquals(1, impacts.size());
        assertEquals("b1", impacts.get(0).getBusinessService().getName());
    }

    @Test
    public void canRenderGraphToPng() {
        // Create a simple hierarchy
        MockBusinessServiceHierarchy h = MockBusinessServiceHierarchy.builder()
                .withBusinessService(1)
                    .withReductionKey(1, "a1")
                    .commit()
                .build();

        // Setup the state machine
        BusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(h.getBusinessServices());

        // Render the state machine graph
        File pngFile = new File(tempFolder.getRoot(), "test.png");
        assertFalse(pngFile.getAbsolutePath() + " should not exist.", pngFile.exists());
        stateMachine.renderGraphToPng(pngFile);
        assertTrue(pngFile.getAbsolutePath() + " should exist.", pngFile.exists());
    }
}
