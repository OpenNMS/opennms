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
package org.opennms.netmgt.discovery;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.config.EventConfTestUtil;
import org.opennms.netmgt.eventd.EventExpander;
import org.opennms.netmgt.eventd.EventUtilDaoImpl;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.FileSystemResource;

import com.codahale.metrics.MetricRegistry;

import java.util.List;

public class NewSuspectLocationTest {
    private final String NEW_SUSPECT_UEI = "uei.opennms.org/internal/discovery/newSuspect";
    private final String CUSTOM_LOCATION = "Ponyville";

    private EventExpander m_eventExpander;
    private DefaultEventConfDao m_eventConfDao;

    @Before
    public void setUp() throws Exception {
        m_eventConfDao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfTestUtil.parseResourcesAsEventConfEvents(new org.springframework.core.io.ClassPathResource("etc/eventconf.xml"));
        m_eventConfDao.loadEventsFromDB(eventConfEventList);

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
