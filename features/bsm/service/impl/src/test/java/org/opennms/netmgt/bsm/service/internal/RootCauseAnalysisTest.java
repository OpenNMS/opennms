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

import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.bsm.mock.MockAlarmWrapper;
import org.opennms.netmgt.bsm.mock.MockBusinessServiceHierarchy;
import org.opennms.netmgt.bsm.mock.MockBusinessServiceHierarchy.HierarchyBuilder.BusinessServiceBuilder;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class RootCauseAnalysisTest {

    /**
     * Verifies that the RCA and IA algorithms can be performed
     * against business services that reference 100+ reduction keys. 
     *
     * See NMS-8527 for details.
     */
    @Test(timeout=10000)
    public void canEfficientlyPerformRootCauseAnalysis() {
        final int NUMBER_OF_REDUCTION_KEYS_PER_BS = 2500;
        HighestSeverity highestSeverity = new HighestSeverity();

        BusinessServiceBuilder builder = MockBusinessServiceHierarchy.builder()
                .withBusinessService(1)
                .withName("b1")
                .withReductionFunction(highestSeverity);
        
        for (int i = 0; i < NUMBER_OF_REDUCTION_KEYS_PER_BS; i++) {
            builder.withReductionKey(i, "a"+ i);
        }

        MockBusinessServiceHierarchy h = builder.commit().build();

        // Setup the state machine
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(h.getBusinessServices());

        // Bump b1 to MINOR, cause by a1
        stateMachine.handleNewOrUpdatedAlarm(new MockAlarmWrapper("a1", Status.MINOR));

        // Verify the state
        assertEquals(Status.MINOR, stateMachine.getOperationalStatus(h.getBusinessServiceById(1)));

        // Calculate and verify the root cause, b1 caused by a1
        List<GraphVertex> causedby = stateMachine.calculateRootCause(h.getBusinessServiceById(1));
        assertEquals(1, causedby.size());
        assertEquals("a1", causedby.get(0).getReductionKey());

        // Now calculate the impact, a1 impacts b1
        List<GraphVertex> impacts = stateMachine.calculateImpact("a1");
        assertEquals(1, impacts.size());
        assertEquals("b1", impacts.get(0).getBusinessService().getName());
    }

}
