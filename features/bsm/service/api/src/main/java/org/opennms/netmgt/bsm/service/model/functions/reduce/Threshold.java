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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.StatusWithIndex;
import org.opennms.netmgt.bsm.service.model.StatusWithIndices;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Function;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Parameter;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

@Function(name="Threshold", description = "Uses the highest severity found more often than the given threshold.")
public class Threshold implements ReductionFunction {

    private static final Comparator<Status> HIGHEST_SEVERITY_FIRST = new Comparator<Status>() {
        @Override
        public int compare(Status s1, Status s2) {
            return s2.compareTo(s1);
        }
    };

    @Parameter(key="threshold", description = "The Threshold to use")
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
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("threshold", m_threshold)
                .toString();
    }

    @Override
    public Optional<StatusWithIndices> reduce(List<StatusWithIndex> statuses) {
        final Map<Status, Integer> hitsByStatus = getHitsByStatusWithIndex(statuses);

        // Determine the status with the highest severity where the number of relative hits
        // is greater than the configured threshold
        for (Map.Entry<Status, Integer> statusWithHits : hitsByStatus.entrySet()) {
            if (statusWithHits.getValue() / (double)statuses.size() >= m_threshold) {
                return Optional.of(new StatusWithIndices(statusWithHits.getKey(),
                        StatusUtils.getIndicesWithStatusGe(statuses, statusWithHits.getKey())));
            }
        }

        return Optional.empty();
    }

    @Override
    public <T> T accept(ReduceFunctionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Map<Status, Integer> getHitsByStatusWithIndex(List<StatusWithIndex> statuses) {
        return getHitsByStatus(statuses.stream()
                .map(StatusWithIndex::getStatus)
                .collect(Collectors.toList()));
    }

    // We increment the number of "hits" for a particular Status key
    // when one of the inputs is greater or equals to that given key
    // For example, reduce(Status.WARNING, Status.NORMAL) would build a map that looks like:
    //   { 'WARNING': 1, 'NORMAL': 2, 'INDETERMINATE': 2 }
    public Map<Status, Integer> getHitsByStatus(List<Status> statuses) {
        final Map<Status, Integer> hitsByStatus = new TreeMap<>(HIGHEST_SEVERITY_FIRST);
        for (Status s : statuses) {
            for (Status ss : Status.values()) {
                if (ss.isGreaterThan(s)) {
                    continue;
                }
                Integer count = hitsByStatus.get(ss);
                if (count == null) {
                    count = 1;
                } else {
                    count = count + 1;
                }
                hitsByStatus.put(ss, count);
            }
        }
        return hitsByStatus;
    }
}
