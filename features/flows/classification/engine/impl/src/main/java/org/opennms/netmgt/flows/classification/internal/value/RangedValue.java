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

package org.opennms.netmgt.flows.classification.internal.value;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangedValue {
    private static final Logger LOG = LoggerFactory.getLogger(RangedValue.class);

    private final int start;
    private final int end;

    public RangedValue(String input) {
        this(new StringValue(input));
    }

    public RangedValue(StringValue input) {
        Objects.requireNonNull(input);

        final int[] range = verifyInput(input);

        // We can finally set the values
        this.start = range[0];
        this.end = range[1];
    }

    public RangedValue(int start, int end) {
        verifyBounds(start, end);
        this.start = start;
        this.end = end;
    }

    public boolean isInRange(int value) {
        return value >= start && value <= end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    private void verifyBounds(int lowerBound, int higherBound) {
        if (lowerBound > higherBound) {
            throw new IllegalArgumentException("Range is not defined correctly");
        }
    }

    private int[] verifyInput(StringValue input) {
        // Ensure we actually have a value
        if (input.isNullOrEmpty()) throw new IllegalArgumentException("Range must not be null or empty");

        final List<StringValue> range = input.splitBy("-");
        if (range.size() > 2) {
            LOG.warn("Received multiple ranges {}. Will only use {}", range, range.subList(0, 2));
        }

        // Verify each value is a number value
        for (int i=0; i<Math.min(range.size(), 2); i++) {
            try {
                Integer.parseInt(range.get(i).getValue());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Value '" + range.get(i) + "' is not a valid number", ex);
            }
        }
        // Check bounds
        int lowerBound = Integer.parseInt(range.get(0).getValue());
        int higherBound = range.size() == 1 ? lowerBound : Integer.parseInt(range.get(1).getValue());
        verifyBounds(lowerBound, higherBound);
        return new int[]{lowerBound, higherBound};
    }
}
