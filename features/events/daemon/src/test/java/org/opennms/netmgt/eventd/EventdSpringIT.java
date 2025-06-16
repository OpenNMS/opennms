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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/mockSinkConsumerManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventdSpringIT implements InitializingBean {
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
