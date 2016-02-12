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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.bsm.mock.MockAlarmWrapper;
import org.opennms.netmgt.bsm.mock.MockBusinessServiceHierarchy;
import org.opennms.netmgt.bsm.service.AlarmProvider;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.ReadOnlyBusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyEdge;
import org.opennms.netmgt.bsm.test.LoggingStateChangeHandler;

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
        ReadOnlyBusinessService b1 = h.getBusinessServiceById(1);
        ReadOnlyEdge a1 = h.getEdgeByReductionKey("a1");

        // Setup the state machine
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        LoggingStateChangeHandler stateChangeHandler = new LoggingStateChangeHandler();
        stateMachine.addHandler(stateChangeHandler, Maps.newHashMap());
        stateMachine.setBusinessServices(h.getBusinessServices());

        stateMachine.setAlarmProvider(new AlarmProvider() {
            @Override
            public AlarmWrapper lookup(String reductionKey) {
                switch (reductionKey) {
                case "a2":
                    return new MockAlarmWrapper(reductionKey, Status.CRITICAL);
                }
                return null;
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
        ReadOnlyEdge a2 = h.getEdgeByReductionKey("a2");

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
        ReadOnlyBusinessService b1 = h.getBusinessServiceById(1);

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
