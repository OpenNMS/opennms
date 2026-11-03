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
package org.opennms.netmgt.snmpinterfacepoller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.config.SnmpInterfacePollerConfig;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

/**
 * Verifies the SNMP interface poller's handling of reloadDaemonConfig and the
 * application of the configured thread count to its scheduler.
 */
public class SnmpPollerReloadTest {

    private final AtomicInteger m_reloads = new AtomicInteger();
    private SnmpPoller m_poller;
    private SnmpInterfacePollerConfig m_config;
    private EventIpcManager m_eventIpcManager;
    private LegacyScheduler m_scheduler;

    @Before
    public void setUp() {
        // The full reload touches the pollable network and the database; count it instead
        m_poller = new SnmpPoller() {
            @Override
            protected void reloadConfiguration() {
                m_reloads.incrementAndGet();
                resizeScheduler();
            }
        };
        m_config = mock(SnmpInterfacePollerConfig.class);
        m_poller.setPollerConfig(m_config);
        // DaemonTools reports the outcome through the static IPC manager
        m_eventIpcManager = mock(EventIpcManager.class);
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        m_scheduler = new LegacyScheduler("Snmpinterfacepoller", 2);
        m_poller.setScheduler(m_scheduler);
    }

    @After
    public void tearDown() {
        m_scheduler.getRunner().shutdownNow();
        EventIpcManagerFactory.reset();
    }

    @Test
    public void reloadDaemonConfigForThisDaemonReloadsResizesAndReportsSuccess() {
        when(m_config.getThreads()).thenReturn(6);

        m_poller.handleReloadDaemonConfig(reloadEventFor("SnmpPoller"));

        assertThat(m_reloads.get(), equalTo(1));
        assertThat(m_scheduler.getThreads(), equalTo(6));
        final Event sent = sentEvent();
        assertThat(sent.getUei(), equalTo(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI));
        assertThat(parm(sent, EventConstants.PARM_DAEMON_NAME), equalTo("SnmpPoller"));
    }

    @Test
    public void daemonNameMatchIsCaseInsensitive() {
        when(m_config.getThreads()).thenReturn(2);

        m_poller.handleReloadDaemonConfig(reloadEventFor("snmppoller"));

        assertThat(m_reloads.get(), equalTo(1));
    }

    @Test
    public void reloadDaemonConfigForAnotherDaemonIsIgnored() {
        m_poller.handleReloadDaemonConfig(reloadEventFor("Pollerd"));

        assertThat(m_reloads.get(), equalTo(0));
        assertThat(m_scheduler.getThreads(), equalTo(2));
        verify(m_eventIpcManager, never()).sendNow(any(Event.class));
    }

    @Test
    public void reloadFailureIsReportedWithAReason() {
        final SnmpPoller failing = new SnmpPoller() {
            @Override
            protected void reloadConfiguration() {
                throw new IllegalStateException("boom");
            }
        };

        failing.handleReloadDaemonConfig(reloadEventFor("SnmpPoller"));

        final Event sent = sentEvent();
        assertThat(sent.getUei(), equalTo(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI));
        assertThat(parm(sent, EventConstants.PARM_REASON), equalTo("boom"));
    }

    @Test
    public void reloadDaemonConfigWithoutADaemonNameIsIgnored() {
        final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test");

        m_poller.handleReloadDaemonConfig(ImmutableMapper.fromMutableEvent(builder.getEvent()));

        assertThat(m_reloads.get(), equalTo(0));
        verify(m_eventIpcManager, never()).sendNow(any(Event.class));
    }

    @Test
    public void resizeSchedulerToleratesMissingScheduler() {
        m_poller.setScheduler(null);
        when(m_config.getThreads()).thenReturn(6);

        m_poller.resizeScheduler();
    }

    private Event sentEvent() {
        final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(m_eventIpcManager).sendNow(captor.capture());
        return captor.getValue();
    }

    private static String parm(final Event event, final String name) {
        for (final Parm parm : event.getParmCollection()) {
            if (name.equals(parm.getParmName())) {
                return parm.getValue().getContent();
            }
        }
        return null;
    }

    private static IEvent reloadEventFor(final String daemonName) {
        final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test");
        builder.addParam(EventConstants.PARM_DAEMON_NAME, daemonName);
        return ImmutableMapper.fromMutableEvent(builder.getEvent());
    }
}
