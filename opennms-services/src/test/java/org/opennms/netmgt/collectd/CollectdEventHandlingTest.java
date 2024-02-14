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
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class CollectdEventHandlingTest {

    private Collectd collectd = new Collectd();

    private CollectableService svc1;
    private CollectableService svc2;

    @Before
    public void setUp() {
        // Setup the transaction template
        MockTransactionTemplate transactionTemplate = new MockTransactionTemplate();
        transactionTemplate.afterPropertiesSet();
        collectd.setTransactionTemplate(transactionTemplate);

        // Create two collectable services
        CollectorUpdates svc1_udpates = new CollectorUpdates();
        svc1 = mock(CollectableService.class);
        when(svc1.getNodeId()).thenReturn(42);
        when(svc1.getAddress()).thenReturn(InetAddressUtils.ONE_TWENTY_SEVEN);
        when(svc1.getServiceName()).thenReturn("JMX");
        when(svc1.getCollectorUpdates()).thenReturn(svc1_udpates);
        collectd.getCollectableServices().add(svc1);

        CollectorUpdates svc2_udpates = new CollectorUpdates();
        svc2 = mock(CollectableService.class);
        when(svc2.getNodeId()).thenReturn(43);
        when(svc2.getAddress()).thenReturn(InetAddressUtils.UNPINGABLE_ADDRESS);
        when(svc2.getServiceName()).thenReturn("WS-Man");
        when(svc2.getCollectorUpdates()).thenReturn(svc2_udpates);
        collectd.getCollectableServices().add(svc2);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(svc1);
        verifyNoMoreInteractions(svc2);
    }

    @Test
    public void canHandleNodeDeletedEvents() {
        // Handle a interfaceDeleted event targeting svc1
        Event e = new EventBuilder(EventConstants.NODE_DELETED_EVENT_UEI, "test")
                .setNodeid(svc1.getNodeId())
                .getEvent();
        collectd.onEvent(ImmutableMapper.fromMutableEvent(e));

        // The delete flag should be set (and only set) on svc1
        assertTrue("deletion flag was not set on svc1!", svc1.getCollectorUpdates().isDeletionFlagSet());
        assertFalse("deletion flag was set on svc2!", svc2.getCollectorUpdates().isDeletionFlagSet());

        verify(svc1, times(2)).getCollectorUpdates();
        verify(svc1, times(3)).getNodeId();
        verify(svc2, times(1)).getCollectorUpdates();
        verify(svc2, times(1)).getNodeId();
    }

    @Test
    public void canHandleInterfaceDeletedEvents() {
        // Handle a interfaceDeleted event targeting svc1
        OnmsNode node = new OnmsNode();
        node.setId(svc1.getNodeId());

        OnmsIpInterface iface = new OnmsIpInterface();
        iface.setId(99);
        iface.setNode(node);
        iface.setIpAddress(svc1.getAddress());

        Event e = new EventBuilder(EventConstants.INTERFACE_DELETED_EVENT_UEI, "test")
                .setIpInterface(iface)
                .getEvent();
        collectd.onEvent(ImmutableMapper.fromMutableEvent(e));

        // The delete flag should be set (and only set) on svc1
        assertTrue("deletion flag was not set on svc1!", svc1.getCollectorUpdates().isDeletionFlagSet());
        assertFalse("deletion flag was set on svc2!", svc2.getCollectorUpdates().isDeletionFlagSet());

        verify(svc1, times(2)).getCollectorUpdates();
        verify(svc1, times(2)).getAddress();
        verify(svc1, times(3)).getNodeId();
        verify(svc2, times(1)).getAddress();
        verify(svc2, times(1)).getCollectorUpdates();
        verify(svc2, times(1)).getNodeId();
    }

    @Test
    public void canHandleServiceDeletedEvents() {
        // Handle a serviceDeleted event targeting svc2
        OnmsNode node = new OnmsNode();
        node.setId(svc2.getNodeId());

        OnmsIpInterface iface = new OnmsIpInterface();
        iface.setId(101);
        iface.setNode(node);
        iface.setIpAddress(svc2.getAddress());

        Event e = new EventBuilder(EventConstants.SERVICE_DELETED_EVENT_UEI, "test")
                .setIpInterface(iface)
                .setService(svc2.getServiceName())
                .getEvent();
        collectd.onEvent(ImmutableMapper.fromMutableEvent(e));

        // The delete flag should be set (and only set) on svc2
        assertFalse("deletion flag was set on svc1!", svc1.getCollectorUpdates().isDeletionFlagSet());
        assertTrue("deletion flag was not set on svc2!", svc2.getCollectorUpdates().isDeletionFlagSet());

        verify(svc1, times(1)).getAddress();
        verify(svc1, times(1)).getCollectorUpdates();
        verify(svc1, times(1)).getNodeId();
        verify(svc2, times(2)).getAddress();
        verify(svc2, times(2)).getCollectorUpdates();
        verify(svc2, times(3)).getNodeId();
        verify(svc2, times(3)).getServiceName();
    }
}
