/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import java.io.File;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
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
    public void canFindHigherpriorityInFile() throws Exception {
        eventConfDao.setConfigResource(new FileSystemResource(new File("src/test/resources/priority/eventconf.xml")));
        eventConfDao.afterPropertiesSet();
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
    public void canFindHigherpriorityInLaterFile() throws Exception {
        eventConfDao.setConfigResource(new FileSystemResource(new File("src/test/resources/priority/eventconf2.xml")));
        eventConfDao.afterPropertiesSet();
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
        eventConfDao.setConfigResource(new FileSystemResource(new File("src/test/resources/priority/eventconf3.xml")));
        eventConfDao.afterPropertiesSet();
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

}
