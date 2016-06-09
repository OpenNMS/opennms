/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.model.functions.reduce;

import com.google.common.base.Preconditions;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Function;
import org.opennms.netmgt.bsm.service.model.functions.annotations.Parameter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Function(name = "ExponentialPropagation", description = "")
public class ExponentialPropagation implements ReductionFunction {

    @Parameter(key = "base",
               defaultValue = "2.0",
               description = "The base used to calculate the required elements for propagation")
    private double base;

    @Override
    public Optional<Status> reduce(List<Status> statuses) {
        // Get the exponential sum of all child states
        final double sum = statuses.stream()
                                   .filter(status -> status.ordinal() >= Status.WARNING.ordinal())                          // Ignore normal and indeterminate
                                   .mapToDouble(status -> Math.pow(this.base, status.ordinal() - Status.WARNING.ordinal())) // Offset to warning = n^0
                                   .sum();

        // Get the log n of the sum
        final int res = (int) Math.floor(Math.log(sum) / Math.log(this.base)) + Status.WARNING.ordinal(); // Revert offset from above

        // Find the resulting status
        return Optional.of(Status.get(Math.max(res, Status.CRITICAL.ordinal())));
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
