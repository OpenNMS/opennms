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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.eventconf.Event;
import org.springframework.core.io.FileSystemResource;

public class EventConfPriorityTest {

    DefaultEventConfDao eventConfDao;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        eventConfDao = new DefaultEventConfDao();
    }

    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void canFindHigherPriorityInFile() throws Exception {
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(new File("src/test/resources/priority/eventconf.xml")));
        eventConfDao.loadEventsFromDB(eventConfEventList);

        Assert.assertEquals(3, eventConfDao.getAllEvents().size());

        EventBuilder eb = new EventBuilder("uei.opennms.org/vendor/3Com/traps/a3ComFddiMACNeighborChangeEvent", "JUnit");
        eb.setEnterpriseId(".1.3.6.1.4.1.43.29.10");
        eb.setGeneric(6);
        eb.setSpecific(6);

        Event event = eventConfDao.findByEvent(eb.getEvent());
        Assert.assertNotNull(event);
        assertThat(event.getPriority(), is(5));
        Assert.assertEquals("FILE1 CONFIG", event.getEventLabel());
    }

    @Test
    public void canFindHigherPriorityInLaterFile() throws Exception {
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(new File("src/test/resources/priority/eventconf2.xml")));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        Assert.assertEquals(4, eventConfDao.getAllEvents().size());

        EventBuilder eb = new EventBuilder("uei.opennms.org/vendor/3Com/traps/a3ComFddiMACNeighborChangeEvent", "JUnit");
        eb.setEnterpriseId(".1.3.6.1.4.1.43.29.10");
        eb.setGeneric(6);
        eb.setSpecific(6);

        Event event = eventConfDao.findByEvent(eb.getEvent());
        Assert.assertNotNull(event);
        assertThat(event.getPriority(), is(200));
        Assert.assertEquals("FILE2 CONFIG", event.getEventLabel());
    }

    @Test
    public void canUseHighestPriorityDefnWhenInRoot() throws Exception {
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(new File("src/test/resources/priority/eventconf3.xml")));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        Assert.assertEquals(4, eventConfDao.getAllEvents().size());

        EventBuilder eb = new EventBuilder("uei.opennms.org/vendor/3Com/traps/a3ComFddiMACNeighborChangeEvent", "JUnit");
        eb.setEnterpriseId(".1.3.6.1.4.1.43.29.10");
        eb.setGeneric(6);
        eb.setSpecific(6);

        Event event = eventConfDao.findByEvent(eb.getEvent());
        Assert.assertNotNull(event);
        assertThat(event.getPriority(), equalTo(2000));
        Assert.assertEquals("ROOT3 CONFIG", event.getEventLabel());
    }

    @Test
    public void doesNotDuplicateWhenGettingByUEI() throws Exception {
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(new File("src/test/resources/priority/eventconf3.xml")));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        Assert.assertEquals(4, eventConfDao.getAllEvents().size());

        EventBuilder eb = new EventBuilder("uei.opennms.org/vendor/3Com/traps/a3ComFddiMACNeighborChangeEvent", "JUnit");
        eb.setEnterpriseId(".1.3.6.1.4.1.43.29.10");
        eb.setGeneric(6);
        eb.setSpecific(6);

        List<Event> eventConfigList = eventConfDao.getEvents(eb.getEvent().getUei());
        Assert.assertNotNull(eventConfigList);
        assertThat(eventConfigList.size(), equalTo(3));
    }

}
