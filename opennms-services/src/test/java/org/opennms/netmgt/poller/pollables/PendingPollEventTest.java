/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.pollables;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.model.events.EventBuilder;

public class PendingPollEventTest {
    @Test
    public void testPollEventTimeout() throws Exception {
        final Date d = new Date();
        final EventBuilder eb = new EventBuilder("foo", "bar", d);
        final PendingPollEvent ppe = new PendingPollEvent(eb.getEvent());
        ppe.setExpirationTimeInMillis(Long.MAX_VALUE);
        assertFalse("timedOut should be false: " + ppe.isTimedOut(), ppe.isTimedOut());
        assertTrue("pending should be true: " + ppe.isPending(), ppe.isPending());
        ppe.setExpirationTimeInMillis(Long.MIN_VALUE);
        assertTrue("timedOut should be true: " + ppe.isTimedOut(), ppe.isTimedOut());
        assertFalse("pending should be false: " + ppe.isPending(), ppe.isPending());
    }
}
