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

package org.opennms.netmgt.flows.api;

import java.util.HashMap;
import java.util.Map;

public enum FlowSelectorAlgorithm {

    Unassigned(0),
    SystematicCountBasedSampling(1),
    SystematicTimeBasedSampling(2),
    RandomNoutOfNSampling(3),
    UniformProbabilisticSampling(4),
    PropertyMatchFiltering(5),
    HashBasedFilteringUsingBOB(6),
    HashBasedFilteringUsingIPSX(7),
    HashBasedFilteringUsingCRC(8),
    FlowStateDependentIntermediateFlowSelectionProcess(9);

    private final static Map<Integer, FlowSelectorAlgorithm> LOOKUP_MAP;

    static {
            FlowSelectorAlgorithm[] values = FlowSelectorAlgorithm.values();
            LOOKUP_MAP = new HashMap(values.length);

            for (FlowSelectorAlgorithm  flowSelectorAlgorithm: values) {
                LOOKUP_MAP.put(flowSelectorAlgorithm.getId(), flowSelectorAlgorithm);
            }
    }

    private final int id;

    FlowSelectorAlgorithm(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static FlowSelectorAlgorithm fromId(int id) {
        return LOOKUP_MAP.get(id);
    }
}
