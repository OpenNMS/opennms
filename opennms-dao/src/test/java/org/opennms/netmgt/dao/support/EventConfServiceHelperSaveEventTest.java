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
package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.xml.eventconf.Event;

public class EventConfServiceHelperSaveEventTest {

    @Test
    public void saveEventStampsTheSourceLastModified() {
        final EventConfEventDao dao = mock(EventConfEventDao.class);
        when(dao.nextEventOrder(42L)).thenReturn(3);
        when(dao.save(any(EventConfEvent.class))).thenReturn(7L);

        final EventConfSource source = new EventConfSource();
        source.setId(42L);
        source.setLastModified(new Date(0));

        final Event event = new Event();
        event.setUei("uei.opennms.org/test/lastModified");
        event.setEventLabel("Last modified");
        event.setSeverity("Normal");

        final Date now = new Date(1_000_000L);
        assertEquals(Long.valueOf(7L), EventConfServiceHelper.saveEvent(dao, source, event, "admin", now));
        // every caller (REST add/upsert, the programmatic source) relies on this for change detection
        assertEquals(now, source.getLastModified());
    }
}
