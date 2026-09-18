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
package org.opennms.netmgt.wsman.eventlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EventLogStatusStoreTest {

    @Test
    public void keepsOneEntryPerPackageAndLogAndListsAllNodes() {
        final EventLogStatusStore store = new EventLogStatusStore(new InMemoryJsonStore());
        final EventLogTarget a = new EventLogTarget(1, "a", EventLogEventMapperTest.addr("10.0.0.1"), "Default");
        final EventLogTarget b = new EventLogTarget(2, "b", EventLogEventMapperTest.addr("10.0.0.2"), "Remote");
        store.update(a, "p", "System", st -> { st.lastSuccess = 10L; st.recordsRead = 3; });
        store.update(a, "p", "System", st -> st.recordsRead += 2);
        store.update(a, "p", "Application", st -> { st.lastFailure = 11L; st.lastError = "boom"; st.consecutiveFailures = 1; });
        store.update(b, "p", "System", st -> st.backingOff = true);

        final EventLogReadStatus statusA = store.get(1).orElseThrow();
        assertEquals("a", statusA.nodeLabel);
        assertEquals("10.0.0.1", statusA.address);
        assertEquals(2, statusA.logs.size());
        assertEquals(5, statusA.forLog("p", "System").recordsRead);
        assertEquals(Long.valueOf(10L), statusA.forLog("p", "System").lastSuccess);
        assertEquals("boom", statusA.forLog("p", "Application").lastError);
        assertNull(statusA.forLog("p", "System").lastError);

        assertEquals(2, store.getAll().size());
        assertEquals("Remote", store.getAll().get(1).location);
        assertTrue(store.getAll().get(1).logs.get(0).backingOff);
        store.clear();
        assertFalse(store.get(1).isPresent());
    }
}
