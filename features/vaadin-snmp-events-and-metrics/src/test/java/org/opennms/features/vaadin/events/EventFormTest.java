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
package org.opennms.features.vaadin.events;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.config.EventConfTestUtil;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.LogDestType;

import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.TextField;

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
        dao = new DefaultEventConfDao();
        List<EventConfEvent> eventConfEventList = EventConfTestUtil.parseResourcesAsEventConfEvents(
            new org.springframework.core.io.ClassPathResource("etc/events/MPLS.events.xml"));
        dao.loadEventsFromDB(eventConfEventList);
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
        Assert.assertEquals(LogDestType.LOGNDISPLAY, LogDestType.valueOf(logMsgDest.getValue().toString().toUpperCase()));

        String eventUei = "uei.opennms.org/ietf/mplsTeStdMib/traps/mplsTunnelUp";
        Event event = dao.findByUei(eventUei);
        Assert.assertNotNull(event);

        form.setEvent(event);
        logMsgDest = group.getField("logmsg.dest");
        Assert.assertNotNull(logMsgDest);
        Assert.assertTrue(logMsgDest instanceof ComboBox);
        Assert.assertEquals(event.getLogmsg().getDest(), LogDestType.valueOf(logMsgDest.getValue().toString().toUpperCase()));
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
