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
