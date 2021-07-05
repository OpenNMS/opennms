/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.session;

import java.util.Arrays;

/**
 * Tracks sequence numbers and verify completeness.
 *
 * Sequence numbers passed in to the tracker using the {@link #verify(long)} method are checked for completeness. I.e.
 * every sequence number has been passed in exactly once.
 *
 * While checking for completeness, the tracker allows for sequence numbers to be passed in out of order. Therefore it
 * keeps track of a short history. The {@code patience} parameter specifies the length of this history controlling how
 * much the expected sequence number can advance before the missing sequence number is reported as missing. If the
 * missing sequence number is passed in before the the expected sequence number advances to much, the element is
 * considered out-of-order but not missing and history is updated.
 *
 * To allow re-initialisation of sequence numbers, the tracker is lenient for huge sequence number jumps. If the passed
 * in sequence number differs from the expected sequence number by more than what {@code patience} parameters allows,
 * the tracker is reset and the element is considered valid.
 */
public class SequenceNumberTracker {

    /**
     * The highest seen sequence number.
     */
    private long current;

    /**
     * The history of seen sequence numbers relative to the current sequence number.
     *
     * The history is stored in a ring with the size of the expected {@code patience}. Therefore a sequence number
     * {@code q} and {@code q - patience} will share the same slot in the ring. While marking {@code q} as seen (or
     * missing) the history will return the status of {@code q - patience}. As the current sequence number is only
     * driven forwards, this concludes that {@code q - patience} has exceeded patience in just that moment.
     */
    private final Ring seen;

    public SequenceNumberTracker(final int patience) {
        if (patience < 0) {
            throw new IllegalArgumentException("patience must be positive");
        }

        this.seen = patience > 1
            ? new Ring(patience)
            : null;

        // Set to minimal value to trigger re-initialisation on first sequence number passed
        this.current = Integer.MIN_VALUE;
    }

    public synchronized boolean verify(final long sequenceNumber) {
        // Fast-path for disabled sequence tracking - everything is valid
        if (this.seen == null) {
            return true;
        }

        // Detect jumps and reinitialize
        if (Math.abs(this.current - sequenceNumber) > this.seen.size()) {
            this.current = sequenceNumber;

            // Start over with a history where everything is marked as seen
            this.seen.reset(true);
            return true;
        }

        // Check if input is out of order
        if (sequenceNumber < this.current) {
            // Update the history marking the input as seen
            this.seen.set(sequenceNumber, true);
            return true;
        }

        // Mark current sequence number as seen
        boolean valid = this.seen.set(sequenceNumber, true);

        // Mark everything between current sequence number and input as missing
        for (long x = this.current + 1; x < sequenceNumber; x++) {
            valid &= this.seen.set(x, false);
        }

        // Advance sequence number
        this.current = sequenceNumber;

        return valid;
    }

    /**
     * Number of elements that could be out of order.
     */
    public int getPatience() {
        return this.seen.size();
    }

    /**
     * A ring buffer where the elements are stored at {@code index % size}.
     */
    private static class Ring {
        private final boolean[] values;

        private Ring(final int size) {
            if (size < 0) {
                throw new IllegalArgumentException("ring size must be >= 1");
            }

            this.values = new boolean[size];
        }

        public void reset(final boolean value) {
            Arrays.fill(this.values, value);
        }

        public boolean set(final long index, final boolean value) {
            // Calculate the index in the ring
            // This cast is safe because long mod int is always int
            final int wrapped = (int) (index % this.values.length);

            final boolean prev = this.values[wrapped];
            this.values[wrapped] = value;

            return prev;
        }

        public int size() {
            return this.values.length;
        }
    }
}
