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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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

public class EventConfMatcherTest {

    DefaultEventConfDao eventConfDao;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        eventConfDao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(new File("src/test/resources/matcher-test.events.xml")));
        eventConfDao.loadEventsFromDB(eventConfEventList);
        Assert.assertEquals(9, eventConfDao.getAllEvents().size());
    }

    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * NMS-9496: Verify that mask elements can be used to match
     * event definitions, even when an existing UEI is set in the event.
     */
    @Test
    public void canUseMaskElementsOnEventWithUei() throws Exception {
        EventBuilder eb = new EventBuilder("uei.opennms.org/threshold/highThresholdExceeded", "JUnit");
        Event event = eventConfDao.findByEvent(eb.getEvent());
        Assert.assertNull(event.getOperinstruct());

        eb.setNodeid(101); // Match first definition
        event = eventConfDao.findByEvent(eb.getEvent());
        Assert.assertEquals("Call Linux People", event.getOperinstruct());
        Assert.assertEquals("Critical", event.getSeverity());

        eb.setNodeid(201); // Match second definition
        event = eventConfDao.findByEvent(eb.getEvent());
        Assert.assertEquals("Call Windows People", event.getOperinstruct());
        Assert.assertEquals("Major", event.getSeverity());

        eb.setNodeid(121); // Match default
        event = eventConfDao.findByEvent(eb.getEvent());
        Assert.assertNull(event.getOperinstruct());
        Assert.assertEquals("Minor", event.getSeverity());
    }

    /**
     * NMS-9507: Verify that mask elements can be used to match
     * event definitions, even when multiple event definitions
     * with the same UEI are present.
     */
    @Test
    public void canUseMaskVarbindsOnManyEventsWithSameUei() throws Exception {
        EventBuilder eb = new EventBuilder("uei.opennms.org/mib2events/Enterprises/PaloAlto/Panorama/panGeneralGeneralTrap", "JUnit");
        eb.setGeneric(6);
        eb.setSpecific(600);
        eb.setEnterpriseId(".1.3.6.1.4.1.25461.2.1.3.2");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.1",  "p1");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.2",  "p2");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.3",  "p3");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.4",  "p4");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.5",  "p5");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.6",  "p6");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.7",  "p7");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.8",  "p8");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.9",  "p9");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.10", "p10");
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.11", 4);
        eb.addParam(".1.3.6.1.4.1.25461.2.1.3.2.1.12", "Management server shutting down");
        Event event = eventConfDao.findByEvent(eb.getEvent());
        Assert.assertEquals("Major", event.getSeverity());

        eb.setParam(".1.3.6.1.4.1.25461.2.1.3.2.1.11", 2);
        eb.setParam(".1.3.6.1.4.1.25461.2.1.3.2.1.12", "Management server started, everything is OK");
        event = eventConfDao.findByEvent(eb.getEvent());
        Assert.assertEquals("Cleared", event.getSeverity());
    }

    /**
     * NMS-10465: Verify that named event paramters can be used to match event
     * definitions, even when multiple event definitions with the same UEI are
     * present.
     */
    @Test
    public void canUseMaskParameterKeyValuePairs() throws Exception {
        EventBuilder eb = new EventBuilder("uei.opennms.org/threshold/highThresholdExceeded", "JUnit");
        eb.setGeneric(6);
        eb.setSpecific(600);
        eb.setEnterpriseId(".1.3.6.1.4.1.25461.2.1.3.2");
        eb.addParam("status", "down");
        eb.addParam("ifDescr", "eth0");

        Event event = eventConfDao.findByEvent(eb.getEvent());
        assertThat(event, is(not(nullValue())));
        assertThat(event.getSeverity(), is("Minor"));

        eb.setParam("status", "up");
        event = eventConfDao.findByEvent(eb.getEvent());
        assertThat(event, is(not(nullValue())));
        assertThat(event.getSeverity(), is("Cleared"));
    }

    /**
     * NMS-12755: Verify that event matching works as expected event when multiple event
     * definitions with the same UEI reside in different files.
     */
    @Test
    public void canDefineMultipleEventsWithSameUEIAcrossDifferentFiles() throws Exception {
        EventBuilder eb = new EventBuilder("uei.opennms.org/vendor/ipo/traps/ipoGenServiceErrorSvcEventCRITICAL", "JUnit");
        eb.setGeneric(6);
        eb.setSpecific(48);
        eb.setEnterpriseId(".1.3.6.1.4.1.6889.2.2.1.2");
        eb.addParam(".1.3.6.1.4.1.6889.2.2.1.2.1.1",  3);

        Event event = eventConfDao.findByEvent(eb.getEvent());
        assertThat(event.getAlarmData(), notNullValue());
    }
}
