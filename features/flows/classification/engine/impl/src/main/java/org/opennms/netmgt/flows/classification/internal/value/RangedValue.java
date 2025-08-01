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
