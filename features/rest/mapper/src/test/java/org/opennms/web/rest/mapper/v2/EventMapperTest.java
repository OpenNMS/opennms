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
package org.opennms.web.rest.mapper.v2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.model.v2.EventDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-rest-mappers.xml"
})
@JUnitConfigurationEnvironment
public class EventMapperTest {

    @Autowired
    private EventConfDao eventConfDao;

    @Autowired
    private EventMapper eventMapper;

    @Test
    public void canMapEvent() {
        Event eventConf = new Event();
        eventConf.setUei("some-uei");
        eventConf.setEventLabel("some-label");
        eventConfDao.addEvent(eventConf);

        OnmsEvent event = new OnmsEvent();
        event.setId(1L);
        event.setEventUei("some-uei");
        event.setEventSeverity(OnmsSeverity.CRITICAL.getId());

        EventDTO eventDTO = eventMapper.eventToEventDTO(event);

        assertThat(eventDTO.getId(), equalTo(1));
        assertThat(eventDTO.getLabel(), equalTo("some-label"));
    }
}
