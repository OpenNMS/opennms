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
package org.opennms.netmgt.poller.pollables;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.model.events.EventBuilder;

public class PendingPollEventTest {
    @Test
    public void testPollEventTimeout() throws Exception {
        final Date d = new Date();
        final EventBuilder eb = new EventBuilder("foo", "bar", d);
        final PendingPollEvent ppe = new PendingPollEvent(ImmutableMapper.fromMutableEvent(eb.getEvent()));
        ppe.setExpirationTimeInMillis(Long.MAX_VALUE);
        assertFalse("timedOut should be false: " + ppe.isTimedOut(), ppe.isTimedOut());
        assertTrue("pending should be true: " + ppe.isPending(), ppe.isPending());
        ppe.setExpirationTimeInMillis(Long.MIN_VALUE);
        assertTrue("timedOut should be true: " + ppe.isTimedOut(), ppe.isTimedOut());
        assertFalse("pending should be false: " + ppe.isPending(), ppe.isPending());
    }
}
