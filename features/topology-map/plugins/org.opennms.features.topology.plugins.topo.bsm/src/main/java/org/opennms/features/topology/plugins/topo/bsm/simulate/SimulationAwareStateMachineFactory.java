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
