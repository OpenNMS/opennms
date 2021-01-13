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

package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;

public class SequenceNumberTrackerTest {

    @Test
    public void testInit() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker();
        assertTrue(tracker.verify(5));
    }

    @Test
    public void testExpected() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker();
        assertTrue(tracker.verify(5));
        assertTrue(tracker.verify(6));
        assertTrue(tracker.verify(7));
    }

    @Test
    public void testUnexpected() {
        final SequenceNumberTracker tracker = new SequenceNumberTracker();
        assertTrue(tracker.verify(5));
        assertFalse(tracker.verify(7));
        assertTrue(tracker.verify(8));
        assertFalse(tracker.verify(3));
        assertTrue(tracker.verify(4));
    }

}
