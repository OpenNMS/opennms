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
package org.opennms.netmgt.eventd.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.eventd.EventExpander;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.eventconf.EventOrdering;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Varbind;
import org.opennms.test.ThrowableAnticipator;

import com.codahale.metrics.MetricRegistry;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class EventExpanderTest {
    private EventConfDao m_eventConfDao = mock(EventConfDao.class);
    private EventUtil m_eventUtil = mock(EventUtil.class);

    @After
    public void tearDown() {
        verifyNoMoreInteractions(m_eventConfDao);
        verifyNoMoreInteractions(m_eventUtil);
    }

    @Test
    public void testAfterPropertiesSetWithNoEventConfDao() {
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

        when(m_eventConfDao.findByEvent(event)).thenReturn(null);
        when(m_eventConfDao.findByUei("uei.opennms.org/default/event")).thenReturn(null);

        expander.expandEvent(event);
        
        assertEquals("event UEI", uei, event.getUei());

        verify(m_eventConfDao, times(1)).findByEvent(any(Event.class));
        verify(m_eventConfDao, times(1)).findByUei(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
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

        when(m_eventConfDao.findByEvent(event)).thenReturn(eventConfig);
        when(m_eventConfDao.isSecureTag(anyString())).thenReturn(true);
        when(m_eventUtil.expandParms("%parm[#1]%", event, new HashMap<String,Map<String,String>>())).thenReturn("Vaadin");

        expander.expandEvent(event);

        assertEquals("event UEI", uei, event.getUei());
        assertEquals("parameters count", 3, event.getParmCollection().size());
        assertNotNull(event.getParm("username"));

        assertThat(event, hasParameter("username", "agalue"));
        assertThat(event, hasParameter("i-hate", "Vaadin"));

        verify(m_eventConfDao, times(1)).findByEvent(any(Event.class));
        verify(m_eventConfDao, times(14)).isSecureTag(anyString());
        verify(m_eventUtil, times(1)).expandParms(anyString(), any(Event.class), any(Map.class));
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

        verify(m_eventConfDao, times(1)).findByEvent(any(Event.class));
        verify(m_eventConfDao, times(14)).isSecureTag(anyString());
    }

    private void expand(Event event, org.opennms.netmgt.xml.eventconf.Event eventConfig) {
        EventExpander expander = new EventExpander(new MetricRegistry());
        expander.setEventConfDao(m_eventConfDao);
        expander.setEventUtil(m_eventUtil);
        expander.afterPropertiesSet();

        when(m_eventConfDao.findByEvent(event)).thenReturn(eventConfig);
        when(m_eventConfDao.isSecureTag(anyString())).thenReturn(true);

        expander.expandEvent(event);

        verify(m_eventConfDao, atLeastOnce()).isSecureTag(anyString());
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
