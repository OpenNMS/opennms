/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.easymock.EasyMock;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.eventd.EventExpander;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.eventconf.EventOrdering;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Varbind;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

import com.codahale.metrics.MetricRegistry;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class EventExpanderTest {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    
    private EventConfDao m_eventConfDao = m_mocks.createMock(EventConfDao.class);
    private EventUtil m_eventUtil = m_mocks.createMock(EventUtil.class);

    @After
    public void tearDown() {
        m_mocks.verifyAll();
    }

    @Test
    public void testAfterPropertiesSetWithNoEventConfDao() {
        m_mocks.replayAll();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property eventConfDao must be set"));

        EventExpander expander = new EventExpander(new MetricRegistry());

        try {
            expander.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    @Test
    public void testAfterPropertiesSet() {
        m_mocks.replayAll();

        EventExpander expander = new EventExpander(new MetricRegistry());
        expander.setEventConfDao(m_eventConfDao);
        expander.setEventUtil(m_eventUtil);
        expander.afterPropertiesSet();
    }

    @Test
    public void testExpandEventWithNoDaoMatches() {

        String uei = "uei.opennms.org/internal/capsd/snmpConflictsWithDb";

        EventBuilder builder = new EventBuilder(uei, "something");

        EventExpander expander = new EventExpander(new MetricRegistry());
        expander.setEventConfDao(m_eventConfDao);
        expander.setEventUtil(m_eventUtil);
        expander.afterPropertiesSet();
        
        Event event = builder.getEvent();
        assertNull("event description should be null before expandEvent is called", event.getDescr());

        EasyMock.expect(m_eventConfDao.findByEvent(event)).andReturn(null);
        EasyMock.expect(m_eventConfDao.findByUei("uei.opennms.org/default/event")).andReturn(null);
        m_mocks.replayAll();

        expander.expandEvent(event);
        
        assertEquals("event UEI", uei, event.getUei());
        //assertNotNull("event description should not be null after expandEvent is called", event.getDescr());
        //
        //String matchText = "During a rescan";
        //assertTrue("event description should contain '" + matchText + "'", event.getDescr().contains(matchText));
    }

    @Test
    public void testOptionalParameters() {
        String uei = "uei.opennms.org/testEventWithOptionalParameters";
        EventBuilder builder = new EventBuilder(uei, "something");
        builder.addParam("worst-framework-ever", "Vaadin");
        Event event = builder.getEvent();

        EventExpander expander = new EventExpander(new MetricRegistry());
        expander.setEventConfDao(m_eventConfDao);
        expander.setEventUtil(m_eventUtil);
        expander.afterPropertiesSet();

        org.opennms.netmgt.xml.eventconf.Event eventConfig = new org.opennms.netmgt.xml.eventconf.Event();
        eventConfig.setUei(uei);
        org.opennms.netmgt.xml.eventconf.Parameter p1 = new org.opennms.netmgt.xml.eventconf.Parameter();
        p1.setName("username");
        p1.setValue("agalue");
        eventConfig.addParameter(p1);
        org.opennms.netmgt.xml.eventconf.Parameter p2 = new org.opennms.netmgt.xml.eventconf.Parameter();
        p2.setName("i-hate");
        p2.setValue("%parm[#1]%");
        p2.setExpand(true);
        eventConfig.addParameter(p2);

        EasyMock.expect(m_eventConfDao.findByEvent(event)).andReturn(eventConfig);
        EasyMock.expect(m_eventConfDao.isSecureTag(EasyMock.anyObject())).andReturn(true).anyTimes();
        EasyMock.expect(m_eventUtil.expandParms("%parm[#1]%", event, new HashMap<String,Map<String,String>>())).andReturn("Vaadin");
        m_mocks.replayAll();

        expander.expandEvent(event);

        assertEquals("event UEI", uei, event.getUei());
        assertEquals("parameters count", 3, event.getParmCollection().size());
        assertNotNull(event.getParm("username"));

        assertThat(event, hasParameter("username", "agalue"));
        assertThat(event, hasParameter("i-hate", "Vaadin"));
    }

    @Test
    public void canExpandWithMatchValues() {
        // build the event definition
        org.opennms.netmgt.xml.eventconf.Event eventConfig = new org.opennms.netmgt.xml.eventconf.Event();
        eventConfig.setUei("test");
        Mask mask = new Mask();
        eventConfig.setMask(mask);
        Maskelement me = new Maskelement();
        me.setMename("generic");
        me.setMevalues(Collections.singletonList("6"));
        mask.addMaskelement(me);
        Varbind vb = new Varbind();
        vb.setVbnumber(1);
        vb.setVbvalues(Arrays.asList("~Node /(?<poolName>.*?)/(?<poolMember>\\S+) address (?<poolAddr>\\S+) monitor status down. .*\\(slot(?<slotNum>[0-9]+)\\)"));
        mask.addVarbind(vb);

        // build an event that will match the definition above
        EventBuilder builder = new EventBuilder("test", "test");
        builder.addParam("vb1", "Node /Common/10.129.1.30 address 10.129.1.31 monitor status down. [ /Common/icmp_default: down ] [ was up for 0hr:1min:10sec ] (slot1)");
        builder.setGeneric(6);
        Event event = builder.getEvent();

        // initialize the matchers
        eventConfig.initialize(new EventOrdering().next());

        // expand
        expand(event, eventConfig);

        // verify
        assertThat(event, hasParameter("poolName", "Common"));
        assertThat(event, hasParameter("poolMember", "10.129.1.30"));
        assertThat(event, hasParameter("poolAddr", "10.129.1.31"));
        assertThat(event, hasParameter("slotNum", "1"));
    }

    private void expand(Event event, org.opennms.netmgt.xml.eventconf.Event eventConfig) {
        EventExpander expander = new EventExpander(new MetricRegistry());
        expander.setEventConfDao(m_eventConfDao);
        expander.setEventUtil(m_eventUtil);
        expander.afterPropertiesSet();

        EasyMock.expect(m_eventConfDao.findByEvent(event)).andReturn(eventConfig);
        EasyMock.expect(m_eventConfDao.isSecureTag(EasyMock.anyObject())).andReturn(true).anyTimes();
        m_mocks.replayAll();

        expander.expandEvent(event);
    }

    public static HasParameter hasParameter(String name, String value) {
        return new HasParameter(name, value);
    }

    private static class HasParameter extends TypeSafeMatcher<Event> {
        private final String name;
        private final String value;

        public HasParameter(String name, String value) {
            this.name = Objects.requireNonNull(name, "event parameter name cannot be null");
            this.value = value;
        }

        @Override
        protected boolean matchesSafely(Event event) {
            final Parm parm = event.getParm(name);
            if (parm == null) {
                return false;
            }
            if (parm.getValue() == null) {
                return value == null;
            }
            return Objects.equals(value, parm.getValue().getContent());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(String.format("has parameter %s=%s", name, value));
        }
    }


}
