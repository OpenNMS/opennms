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

package org.opennms.netmgt.discovery;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.eventd.EventExpander;
import org.opennms.netmgt.eventd.EventUtilDaoImpl;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.FileSystemResource;

import com.codahale.metrics.MetricRegistry;

public class NewSuspectLocationTest {
    private final String NEW_SUSPECT_UEI = "uei.opennms.org/internal/discovery/newSuspect";
    private final String CUSTOM_LOCATION = "Ponyville";

    private EventExpander m_eventExpander;
    private DefaultEventConfDao m_eventConfDao;

    @Before
    public void setUp() throws Exception {
        m_eventConfDao = new DefaultEventConfDao();
        m_eventConfDao.setConfigResource(new FileSystemResource(ConfigurationTestUtils.getFileForConfigFile("eventconf.xml")));
        m_eventConfDao.afterPropertiesSet();

        m_eventExpander = new EventExpander(new MetricRegistry());
        m_eventExpander.setEventConfDao(m_eventConfDao);
        m_eventExpander.setEventUtil(new EventUtilDaoImpl());
        m_eventExpander.afterPropertiesSet();
    }

    @Test
    public void locationInDescriptionAndMessageTest() {
        EventBuilder builder = new EventBuilder(NEW_SUSPECT_UEI, "something");
        builder.addParam("location", CUSTOM_LOCATION);
        Event event = builder.getEvent();

        assertNull("event description must be null before expandEvent() call", event.getDescr());
        m_eventExpander.expandEvent(event);
        assertNotNull("event description must be non-null after expandEvent() call", event.getDescr());

        assertTrue("description must contain location", event.getDescr().contains("in location " + CUSTOM_LOCATION + " and"));
        assertTrue("logmsg must contain location", event.getLogmsg().getContent().contains("in location " + CUSTOM_LOCATION + " and"));
    }
}
