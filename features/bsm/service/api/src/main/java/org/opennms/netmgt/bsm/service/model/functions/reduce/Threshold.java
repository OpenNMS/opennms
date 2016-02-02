/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.model.functions.reduce;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.mapreduce.ReductionFunction;

import com.google.common.base.Preconditions;

public class Threshold implements ReductionFunction {

    private float m_threshold;

    public void setThreshold(float threshold) {
        Preconditions.checkArgument(threshold > 0, "threshold must be strictly positive");
        Preconditions.checkArgument(threshold <= 1, "threshold must be less or equal to 1");
        m_threshold = threshold;
    }

    public float getThreshold() {
        return m_threshold;
    }

    @Override
    public Optional<Status> reduce(Map<Edge, Status> edgeStatusMap) {
        // define weight factor
        final int weightSum = edgeStatusMap.keySet().stream().mapToInt(e -> e.getWeight()).sum();
        final Map<Edge, Double> weightMap = new HashMap<>();
        edgeStatusMap.keySet().forEach(e -> {
            double weightFactor = (double) e.getWeight() / (double) weightSum;
            weightMap.put(e, weightFactor);
        });
        // define status weight
        Map<Status, Double> statusWeightMap = new HashMap<>();
        for (Status eachStatus : Status.values()) {
            double statusTotal = edgeStatusMap.entrySet().stream().filter(e -> e.getValue().isGreaterThanOrEqual(eachStatus)).mapToDouble(e -> weightMap.get(e.getKey())).sum();
            statusWeightMap.put(eachStatus, statusTotal);
        }
        // get maximum severity
        Optional<Status> reducedStatus = statusWeightMap.keySet().stream().sorted((o1, o2) -> -1 * o1.compareTo(o2)).filter(status -> statusWeightMap.get(status).doubleValue() >= m_threshold).findFirst();
        return reducedStatus;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("threshold", m_threshold)
                .toString();
    }
}
