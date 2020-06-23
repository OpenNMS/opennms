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

package org.opennms.netmgt.bsm.service.model.functions.reduce;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.StatusWithIndex;
import org.opennms.netmgt.bsm.service.model.StatusWithIndices;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Function;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Parameter;

@Function(name="HighestSeverityAbove", description = "Uses the highest severity greater than the given threshold severity")
public class HighestSeverityAbove implements ReductionFunction {

    @Parameter(key="threshold", description = "The status value to use as threshold")
    private Status threshold;

    @Override
    public Optional<StatusWithIndices> reduce(List<StatusWithIndex> statuses) {
        return reduceWithHighestSeverityAbove(statuses, threshold);
    }

    protected static Optional<StatusWithIndices> reduceWithHighestSeverityAbove(List<StatusWithIndex> statuses, Status threshold) {
        final Status highestSeverity = statuses.stream()
                .map(StatusWithIndex::getStatus)
                .filter(s -> s.isGreaterThan(threshold))
                .reduce((a, b) -> a.isGreaterThan(b) ? a : b)
                .orElse(null);
        if (highestSeverity == null) {
            return Optional.empty();
        } else {
            return Optional.of(new StatusWithIndices(highestSeverity,
                    StatusUtils.getIndicesWithStatusGe(statuses, highestSeverity)));
        }
    }

    public void setThreshold(Status threshold) {
        this.threshold = Objects.requireNonNull(threshold);
    }

    public Status getThreshold() {
        return threshold;
    }

    @Override
    public <T> T accept(ReduceFunctionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
