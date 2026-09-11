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
package org.opennms.netmgt.availability;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.opennms.netmgt.model.availability.NodeAvailability;
import org.opennms.netmgt.xml.event.Event;

public class CategoryAvailabilityThresholdNotifierTest {

    private static final long DAY = 24L * 60L * 60L * 1000L;

    private final CategoryDefinition m_web = new CategoryDefinition("Web Servers", "isHTTP", Arrays.asList("HTTP"), 99.99, 97.0);
    private final CategoryDefinition m_noThresholds = new CategoryDefinition("Loose", "isHTTP", Collections.emptyList(), null, null);

    private EventForwarder m_forwarder;
    private CategoryAvailabilityThresholdNotifier m_notifier;

    @Before
    public void setUp() {
        m_forwarder = mock(EventForwarder.class);
        m_notifier = new CategoryAvailabilityThresholdNotifier(m_forwarder);
    }

    @Test
    public void fallingBelowWarningRaisesEvent() {
        m_notifier.evaluate(m_web, snapshot(99.5), snapshot(95.0));
        final Event event = captured();
        assertEquals(CategoryAvailabilityThresholdNotifier.BELOW_WARNING_UEI, event.getUei());
        assertEquals("Web Servers", event.getParm("label").getValue().getContent());
        assertEquals("95.000", event.getParm("availability").getValue().getContent());
        assertEquals("99.500", event.getParm("previousAvailability").getValue().getContent());
        assertEquals("97.000", event.getParm("warningThreshold").getValue().getContent());
        assertEquals("99.990", event.getParm("normalThreshold").getValue().getContent());
    }

    @Test
    public void climbingBackRaisesResolvingEvent() {
        m_notifier.evaluate(m_web, snapshot(95.0), snapshot(97.0));
        final Event event = captured();
        assertEquals(CategoryAvailabilityThresholdNotifier.ABOVE_WARNING_UEI, event.getUei());
        assertEquals("Web Servers", event.getParm("label").getValue().getContent());
    }

    @Test
    public void stayingOnEitherSideIsSilent() {
        m_notifier.evaluate(m_web, snapshot(99.0), snapshot(98.0));
        m_notifier.evaluate(m_web, snapshot(90.0), snapshot(80.0));
        verify(m_forwarder, never()).sendNow(any(Event.class));
    }

    @Test
    public void firstComputationBelowWarningRaisesEvent() {
        m_notifier.evaluate(m_web, null, snapshot(50.0));
        assertEquals(CategoryAvailabilityThresholdNotifier.BELOW_WARNING_UEI, captured().getUei());
    }

    @Test
    public void firstComputationAboveWarningIsSilent() {
        m_notifier.evaluate(m_web, null, snapshot(100.0));
        verify(m_forwarder, never()).sendNow(any(Event.class));
    }

    @Test
    public void categoriesWithoutThresholdsAreIgnored() {
        m_notifier.evaluate(m_noThresholds, snapshot(100.0), snapshot(0.0));
        verify(m_forwarder, never()).sendNow(any(Event.class));
    }

    private Event captured() {
        final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(m_forwarder).sendNow(captor.capture());
        return captor.getValue();
    }

    /** A one-node, one-service snapshot with exactly the given availability. */
    private static CategoryAvailability snapshot(final double availability) {
        final Date end = new Date();
        final Date start = new Date(end.getTime() - DAY);
        final long downtime = Math.round(DAY * (1.0 - availability / 100.0));
        return new CategoryAvailability("Web Servers", start, end, end, Collections.singletonList(new NodeAvailability(1, 1, downtime > 0 ? 1 : 0, downtime, DAY)));
    }
}
