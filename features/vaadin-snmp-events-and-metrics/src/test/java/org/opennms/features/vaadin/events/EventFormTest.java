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
import org.opennms.netmgt.xml.eventconf.LogDestType;
import org.springframework.core.io.FileSystemResource;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * The Test Class for EventForm.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class EventFormTest {

    /** The Event Configuration DAO. */
    private DefaultEventConfDao dao;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
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
        Assert.assertEquals(LogDestType.LOGNDISPLAY, logMsgDest.getValue());

        String eventUei = "uei.opennms.org/ietf/mplsTeStdMib/traps/mplsTunnelUp";
        Event event = dao.findByUei(eventUei);
        Assert.assertNotNull(event);

        form.setEvent(event);
        logMsgDest = group.getField("logmsg.dest");
        Assert.assertNotNull(logMsgDest);
        Assert.assertTrue(logMsgDest instanceof ComboBox);
        Assert.assertEquals(event.getLogmsg().getDest(), logMsgDest.getValue());
    }


    /**
     * Test event without alarm data.
     * <p>See NMS-9422 for more details</p>
     *
     * @throws Exception the exception
     */
    @Test
    public void testEventWithoutAlarmData() throws Exception {
        EventForm form = new EventForm();
        Event event = dao.findByUei("uei.opennms.org/ietf/mplsTeStdMib/traps/mplsTunnelReoptimized");
        Assert.assertNotNull(event);
        Assert.assertNull(event.getAlarmData());
        form.setEvent(event);

        form.setEnabled(true);
        form.hasAlarmData.setValue(true);
        form.alarmDataAlarmType.setValue(1);
        form.alarmDataAutoClean.setValue(true);
        form.alarmDataReductionKey.setValue("%uei%::%nodeid%");
        form.commit();

        Event updatedEvent = form.getEvent();
        Assert.assertNotNull(updatedEvent.getAlarmData());
        Assert.assertEquals(new Integer(1), updatedEvent.getAlarmData().getAlarmType());
    }

    /**
     * Test event with alarm data.
     * <p>See NMS-9422 for more details</p>
     *
     * @throws Exception the exception
     */
    @Test
    public void testEventWithAlarmData() throws Exception {
        EventForm form = new EventForm();
        Event event = dao.findByUei("uei.opennms.org/mpls/traps/mplsVrfIfDown");
        Assert.assertNotNull(event);
        Assert.assertNotNull(event.getAlarmData());
        form.setEvent(event);

        form.setEnabled(true);
        Assert.assertTrue(form.hasAlarmData.getValue());
        Assert.assertEquals(new Integer(1), form.alarmDataAlarmType.getValue());
        form.alarmDataReductionKey.setValue("this-is-a-new-reduction-key");
        form.commit();

        Event updatedEvent = form.getEvent();
        Assert.assertNotNull(updatedEvent.getAlarmData());
        Assert.assertEquals("this-is-a-new-reduction-key", updatedEvent.getAlarmData().getReductionKey());
    }

}
