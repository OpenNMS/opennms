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
package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;

public class SequenceNumberTrackerTest {

    @Test
    public void testInitZero() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(32);
        assertTrue(tracker.verify(0));
    }

    @Test
    public void testInitSmallerThanPatience() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(32);
        assertTrue(tracker.verify(16));
    }

    @Test
    public void testInitWithExactPatience() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(32);
        assertTrue(tracker.verify(32));
    }

    @Test
    public void testInitLargerThanPatience() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(32);
        assertTrue(tracker.verify(128));
    }

    @Test
    public void testInOrder() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(32);
        for (int x = 0; x <= tracker.getPatience() * 2; x++) {
            assertTrue(tracker.verify(x));
        }
    }
    
    @Test
    public void testOutOfOrder() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(32);
        for (int x = 0; x <= tracker.getPatience() * 2; x += 2) {
            assertTrue("x=" + (x + 2), tracker.verify(x + 2));
            assertTrue("x=" + (x + 1), tracker.verify(x + 1));
        }
    }

    @Test
    public void testDuplicates() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(32);

        // Fill in 100 elements
        for (int x = 0; x <= 100; x++) {
            assertTrue(tracker.verify(x));
        }

        // Double call with current sequence number
        assertTrue(tracker.verify(100));

        // Double call with sequence number in history
        assertTrue(tracker.verify(90));
    }

    @Test
    public void testLate() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(32);

        // Start with first elements
        assertTrue(tracker.verify(95));
        assertTrue(tracker.verify(96));
        assertTrue(tracker.verify(97));
        assertTrue(tracker.verify(98));
        assertTrue(tracker.verify(99));

        // Skip the 100 and insert more elements to barely adhere to the patience
        for (int x = 1; x < tracker.getPatience(); x++) {
            assertTrue(tracker.verify(100 + x));
        }

        // 100 has not been seen and considered late
        assertFalse(tracker.verify(100 + tracker.getPatience()));

        // Followings are there, again
        for (int x = 1; x < tracker.getPatience(); x++) {
            assertTrue(tracker.verify(100 + tracker.getPatience() + x));
        }
    }

    @Test
    public void testReset() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(32);

        assertTrue(tracker.verify(8));
        assertTrue(tracker.verify(9));
        assertTrue(tracker.verify(10));

        // Skipping 32 - 1 -> no reset
        assertTrue(tracker.verify(42));
        assertFalse(tracker.verify(43));
        assertFalse(tracker.verify(44));

        // Skipping 32 -> reset
        assertTrue(tracker.verify(78));
        assertTrue(tracker.verify(79));
        assertTrue(tracker.verify(80));
    }

    @Test
    public void testSizeTwo() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(2);

        assertTrue(tracker.verify(0));
        assertTrue(tracker.verify(1));

        // skipping 1 -> no reset
        assertTrue(tracker.verify(3));
        assertFalse(tracker.verify(4));
        assertTrue(tracker.verify(5));

        // skipping 2 -> reset
        assertTrue(tracker.verify(8));
        assertTrue(tracker.verify(9));
        assertTrue(tracker.verify(10));
    }

    @Test
    public void testSizeOne() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(1);

        assertTrue(tracker.verify(0));
        assertTrue(tracker.verify(1));

        assertTrue(tracker.verify(3));
        assertTrue(tracker.verify(4));

        assertTrue(tracker.verify(6));
    }

    @Test
    public void testSizeZero() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker(0);

        assertTrue(tracker.verify(0));
        assertTrue(tracker.verify(1));

        assertTrue(tracker.verify(3));
        assertTrue(tracker.verify(4));

        assertTrue(tracker.verify(6));
    }
}
