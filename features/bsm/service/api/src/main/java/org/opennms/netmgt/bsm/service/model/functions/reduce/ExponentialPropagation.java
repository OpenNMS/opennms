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
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.StatusWithIndex;
import org.opennms.netmgt.bsm.service.model.StatusWithIndices;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Function;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Parameter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

@Function(name = "ExponentialPropagation", description = "Propagate severities using a given base number")
public class ExponentialPropagation implements ReductionFunction {

    @Parameter(key = "base",
               defaultValue = "2.0",
               description = "The base used to calculate the required elements for propagation")
    private double base;

    @Override
    public Optional<StatusWithIndices> reduce(List<StatusWithIndex> statuses) {
        // Exit early for no incoming statuses
        if (statuses.isEmpty()) {
            return Optional.empty();
        }

        // Unfortunately, our computation will result in a normal severity when all input statuses are
        // indeterminate. So, we have to handle this case explicitly here...
        if (Iterables.all(statuses, si -> si.getStatus() == Status.INDETERMINATE)) {
            return Optional.empty();
        }

        // Get the exponential sum of all child states
        final double sum = statuses.stream()
                                   .filter(si -> si.getStatus().ordinal() >= Status.WARNING.ordinal())                          // Ignore normal and indeterminate
                                   .mapToDouble(si -> Math.pow(this.base, (double)(si.getStatus().ordinal() - Status.WARNING.ordinal()))) // Offset to warning = n^0
                                   .sum();

        // Grab the indices from all the statuses that contributed to the sum
        // since these contribute to the cause
        final List<Integer> contributingIndices = statuses.stream()
            .filter(si -> si.getStatus().ordinal() >= Status.WARNING.ordinal())
            .map(StatusWithIndex::getIndex)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        // Get the log n of the sum
        final int res = (int) Math.floor(Math.log(sum) / Math.log(this.base)) + Status.WARNING.ordinal(); // Revert offset from above

        // Find the resulting status and treat values lower than NORMAL.ordinal() as NORMAL.ordinal() and
        // all values higher than CRITICAL.ordinal() as CRITICAL.ordinal()
        final Status effectiveStatus = Status.get(Math.max(Math.min(res, Status.CRITICAL.ordinal()), Status.NORMAL.ordinal()));
        return Optional.of(new StatusWithIndices(effectiveStatus, contributingIndices));
    }

    public void setBase(final double base) {
        Preconditions.checkArgument(base > 1.0);

        this.base = base;
    }

    public double getBase() {
        return this.base;
    }

    @Override
    public <T> T accept(ReduceFunctionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
