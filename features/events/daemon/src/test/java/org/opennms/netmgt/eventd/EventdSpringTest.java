/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test the startup and shutdown of eventd with the default wiring and
 * configuration files.  Don't override *any* beans so we can see if the
 * daemon will work as it does in production (as possible).
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventdSpringTest implements InitializingBean {
    @Autowired
    Eventd m_daemon;

    @Autowired
    EventConfDao m_eventConfDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /**
     * Test the startup and shutdown of this daemon.  This is the only test in
     * this file because having more seems to cause OutOfMemory errors within
     * Eclipse when there are multiple tests due to the large number of events
     * that are loaded by default.
     */
    @Test
    public void testDaemon() throws Exception {
        m_daemon.onStart();
        m_daemon.onStop();
    }

    @Test
    public void testEventConfSeverities() throws Exception {
        List<String> validSeverities = Arrays.asList(new String[] { "Critical", "Major", "Minor", "Warning", "Normal", "Cleared", "Indeterminate" });

        for (String uei : m_eventConfDao.getEventUEIs()) {
            for (Event event : m_eventConfDao.getEvents(uei)) {
                if (!validSeverities.contains(event.getSeverity())) {
                    fail(String.format("Invalid severity found on event: %s: %s", uei, event.getSeverity()));
                }
            }
        }
    }
}
