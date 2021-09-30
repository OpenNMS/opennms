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
        event.setId(1);
        event.setEventUei("some-uei");
        event.setEventSeverity(OnmsSeverity.CRITICAL.getId());

        EventDTO eventDTO = eventMapper.eventToEventDTO(event);

        assertThat(eventDTO.getId(), equalTo(1));
        assertThat(eventDTO.getLabel(), equalTo("some-label"));
    }
}
