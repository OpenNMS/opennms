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

package org.opennms.features.vaadin.events;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.xml.eventconf.Event;
import org.springframework.core.io.FileSystemResource;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import com.vaadin.data.fieldgroup.FieldGroup;

/**
 * The Test Class for EventForm.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class EventFormTest {

    private DefaultEventConfDao dao;

    @Before
    public void setUp() throws Exception {
        File config = new File(ConfigurationTestUtils.getDaemonEtcDirectory(), "events/MPLS.events.xml");
        Assert.assertTrue(config.exists());
        dao = new DefaultEventConfDao();
        dao.setConfigResource(new FileSystemResource(config));
        dao.afterPropertiesSet();
    }

    /**
     * Test the group field.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGroupField() throws Exception {
        EventForm form = new EventForm();
        FieldGroup group = form.eventEditor;

        Field<?> uei = group.getField("uei");
        Assert.assertTrue(uei instanceof TextField);
        Assert.assertEquals("uei.opennms.org/newEvent", uei.getValue());

        Field<?> logMsgDest = group.getField("logmsg.dest");
        Assert.assertNotNull(logMsgDest);
        Assert.assertTrue(logMsgDest instanceof ComboBox);
        Assert.assertEquals("logndisplay", logMsgDest.getValue());

        String eventUei = "uei.opennms.org/ietf/mplsTeStdMib/traps/mplsTunnelUp";
        Event event = dao.findByUei(eventUei);
        Assert.assertNotNull(event);

        form.setEvent(event);
        logMsgDest = group.getField("logmsg.dest");
        Assert.assertNotNull(logMsgDest);
        Assert.assertTrue(logMsgDest instanceof ComboBox);
        Assert.assertEquals(event.getLogmsg().getDest(), logMsgDest.getValue());
    }

}
