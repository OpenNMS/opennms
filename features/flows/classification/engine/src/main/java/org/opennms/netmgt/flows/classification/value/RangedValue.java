/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.value;

import java.util.List;
import java.util.Objects;

public class RangedValue {
    private final int start;
    private final int end;

    public RangedValue(StringValue input) {
        Objects.requireNonNull(input);
        if (!input.isRanged()) throw new IllegalArgumentException("input value must be ranged");

        final List<StringValue> range = input.splitBy("-");
        if (range.size() != 2) {
            throw new IllegalArgumentException("Range must contain 2 elements.");
        }
        int lowerBound = Integer.parseInt(range.get(0).getValue());
        int higherBound = Integer.parseInt(range.get(1).getValue());
        if (lowerBound > higherBound) {
            throw new IllegalArgumentException("Range is not defined correctly");
        }

        this.start = lowerBound;
        this.end = higherBound;
    }

    public boolean isInRange(int value) {
        return value >= start && value <= end;
    }
}
