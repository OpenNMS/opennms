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
package org.opennms.features.topology.plugins.topo.bsm.simulate;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.Status;

public class SimulationAwareStateMachineFactory {

    private SimulationAwareStateMachineFactory() { }

    public static boolean isInSimulationMode(Criteria[] criteria) {
        return Arrays.stream(criteria).anyMatch(c -> c instanceof SimulationEnabledCriteria);
    }

    public static BusinessServiceStateMachine createStateMachine(BusinessServiceManager manager, Criteria[] criteria) {
        if (isInSimulationMode(criteria)) {
            return createSimulatedStateMachine(manager, criteria);
        } else {
            return manager.getStateMachine();
        }
    }

    public static BusinessServiceStateMachine createSimulatedStateMachine(BusinessServiceManager manager, Criteria[] criteria) {
        // Gather the statuses and group them by reduction key
        final Map<String, Status> statusByReductionKey = Arrays.stream(criteria)
                .filter(c -> c instanceof SetStatusToCriteria)
                .map(c -> (SetStatusToCriteria)c)
                .filter(c -> c.getStatus() != null)
                .collect(Collectors.toMap(SetStatusToCriteria::getReductionKey,
                        SetStatusToCriteria::getStatus));

        // Determine whether or not we should inherit the existing state
        final boolean shouldInheritState = Arrays.stream(criteria).anyMatch(c -> c instanceof InheritStateCriteria);

        // Grab a copy of the state machine, and update push alarms
        // that reflect the simulated state of the reduction keys
        final BusinessServiceStateMachine stateMachine = manager.getStateMachine().clone(shouldInheritState);
        for (Entry<String, Status> entry : statusByReductionKey.entrySet()) {
            stateMachine.handleNewOrUpdatedAlarm(new AlarmWrapper() {
                @Override
                public String getReductionKey() {
                    return entry.getKey();
                }

                @Override
                public Status getStatus() {
                    return entry.getValue();
                }
            });
        }

        return stateMachine;
    }
}
