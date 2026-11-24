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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;

/**
 * Verifies that a reloadDaemonConfig event addressed to Collectd applies the
 * configured thread count to the running scheduler.
 */
public class CollectdReloadTest {

    private final Collectd m_collectd = new Collectd();
    private CollectdConfigFactory m_configFactory;
    private EventIpcManager m_eventIpcManager;
    private LegacyScheduler m_scheduler;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true);

        final MockTransactionTemplate transactionTemplate = new MockTransactionTemplate();
        transactionTemplate.afterPropertiesSet();
        m_collectd.setTransactionTemplate(transactionTemplate);

        m_configFactory = mock(CollectdConfigFactory.class);
        when(m_configFactory.getCollectors()).thenReturn(Collections.emptyList());
        m_collectd.setCollectdConfigFactory(m_configFactory);

        final NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.getNodeIds()).thenReturn(Collections.emptyList());
        m_collectd.setNodeDao(nodeDao);
        m_collectd.setFilterDao(mock(FilterDao.class));

        m_eventIpcManager = mock(EventIpcManager.class);
        m_collectd.setEventIpcManager(m_eventIpcManager);

        m_scheduler = new LegacyScheduler("Collectd", 2);
        m_collectd.setScheduler(m_scheduler);
    }

    @After
    public void tearDown() {
        m_scheduler.getRunner().shutdownNow();
        MockLogAppender.resetState();
    }

    @Test
    public void reloadAppliesConfiguredThreadCountToScheduler() throws Exception {
        when(m_configFactory.getThreads()).thenReturn(7);

        m_collectd.onEvent(ImmutableMapper.fromMutableEvent(reloadEventFor("Collectd")));

        verify(m_configFactory).reload();
        assertThat(m_scheduler.getThreads(), equalTo(7));
        assertThat(sentUei(), equalTo(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI));
    }

    @Test
    public void reloadForAnotherDaemonLeavesSchedulerAlone() throws Exception {
        when(m_configFactory.getThreads()).thenReturn(7);

        m_collectd.onEvent(ImmutableMapper.fromMutableEvent(reloadEventFor("Pollerd")));

        assertThat(m_scheduler.getThreads(), equalTo(2));
    }

    private String sentUei() {
        final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(m_eventIpcManager).sendNow(captor.capture());
        return captor.getValue().getUei();
    }

    private static Event reloadEventFor(final String daemonName) {
        final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test");
        builder.addParam(EventConstants.PARM_DAEMON_NAME, daemonName);
        return builder.getEvent();
    }
}
