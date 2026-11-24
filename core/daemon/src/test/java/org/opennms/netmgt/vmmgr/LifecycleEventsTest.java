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
package org.opennms.netmgt.vmmgr;

import static org.junit.Assert.assertEquals;
import static org.opennms.netmgt.events.api.EventConstants.OPENNMS_STARTED_UEI;

import org.junit.Test;
import org.opennms.netmgt.xml.event.Event;

public class LifecycleEventsTest {

    @Test
    public void startedEventCarriesVersionAndStartupTime() {
        final Event event = LifecycleEvents.startedEvent("37.0.0", 61_400);
        assertEquals(OPENNMS_STARTED_UEI, event.getUei());
        assertEquals(LifecycleEvents.SOURCE, event.getSource());
        assertEquals("37.0.0", parm(event, LifecycleEvents.PARM_VERSION));
        assertEquals("61", parm(event, LifecycleEvents.PARM_STARTUP_SECONDS));
        assertEquals(2, event.getParmCollection().size());
    }

    private static String parm(final Event event, final String name) {
        return event.getParm(name).getValue().getContent();
    }
}
