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

package org.opennms.netmgt.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.eventd.AbstractEventUtil;
import org.opennms.netmgt.eventd.EventExpander;
import org.opennms.netmgt.mock.EventUtilJdbcImpl;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml"
})
@JUnitConfigurationEnvironment
public class EventExpanderTest {

    @Autowired
    private EventConfDao m_eventConfDao;

    private EventExpander m_eventExpander;

    @Before
    public void setUp() {
        // Use the JDBC EventUtil so that it works with the mock datasource
        AbstractEventUtil.setInstance(new EventUtilJdbcImpl());

        m_eventExpander = new EventExpander();
        m_eventExpander.setEventConfDao(m_eventConfDao);
        m_eventExpander.afterPropertiesSet();
    }

    @Test
    public void testEventExpander() {
        final EventBuilder eb = new EventBuilder("uei.opennms.org/nodes/nodeDown", "EventExpanderTest");
        final Event event = eb.getEvent();
        m_eventExpander.process(null, event);
        assertNotNull(event.getDescr());
        assertTrue(event.getLogmsg().getContent().contains("is down"));
    }
}
