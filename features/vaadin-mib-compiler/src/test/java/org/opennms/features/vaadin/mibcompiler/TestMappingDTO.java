/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.mibcompiler;

import java.util.List;

import junit.framework.Assert;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

import org.opennms.features.vaadin.mibcompiler.model.EventDTO;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.xml.eventconf.Event;
import org.springframework.core.io.FileSystemResource;

/**
 * The Class TestMappingDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class TestMappingDTO {

    /**
     * Test mapping.
     */
    @Test
    public void testMapping() {
        MapperFacade mapper = new DefaultMapperFactory.Builder().build().getMapperFacade();

        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        eventConfDao.setConfigResource(new FileSystemResource("src/test/resources/Cisco2.events.xml"));
        eventConfDao.afterPropertiesSet();
        List<Event> events = eventConfDao.getEventsByLabel();

        for (Event event : events) {
            EventDTO dto = mapper.map(event, EventDTO.class);
            Assert.assertEquals(event.getUei(), dto.getUei());
            Assert.assertEquals(event.getLogmsg().getContent(), dto.getLogmsg().getContent());
            Assert.assertEquals(event.getMask().getMaskelementCollection().get(0).getMevalueCollection().get(0), dto.getMask().getMaskelementCollection().get(0).getMevalueCollection().get(0));

            Event e = mapper.map(dto, Event.class);
            Assert.assertEquals(dto.getUei(), e.getUei());
            Assert.assertEquals(dto.getLogmsg().getContent(), e.getLogmsg().getContent());
            Assert.assertEquals(dto.getMask().getMaskelementCollection().get(0).getMevalueCollection().get(0), e.getMask().getMaskelementCollection().get(0).getMevalueCollection().get(0));
        }
    }

}
