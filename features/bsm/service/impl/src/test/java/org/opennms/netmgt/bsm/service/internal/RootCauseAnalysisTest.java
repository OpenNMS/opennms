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
