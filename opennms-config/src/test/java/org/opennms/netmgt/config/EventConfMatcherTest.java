/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.eventconf.Event;
import org.springframework.core.io.FileSystemResource;

public class EventConfMatcherTest {

    DefaultEventConfDao eventConfDao;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        eventConfDao = new DefaultEventConfDao();
        eventConfDao.setConfigResource(new FileSystemResource(new File("src/test/resources/NMS-9496.events.xml")));
        eventConfDao.afterPropertiesSet();
        Assert.assertEquals(3, eventConfDao.getAllEvents().size()); 
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

}
