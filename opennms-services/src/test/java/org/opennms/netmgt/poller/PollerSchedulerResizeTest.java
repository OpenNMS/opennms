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
package org.opennms.netmgt.poller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.scheduler.LegacyScheduler;

/**
 * Verifies that Pollerd applies the configured thread count to its running scheduler.
 */
public class PollerSchedulerResizeTest {

    private final Poller m_poller = new Poller();
    private PollerConfig m_pollerConfig;
    private LegacyScheduler m_scheduler;

    @Before
    public void setUp() {
        m_pollerConfig = mock(PollerConfig.class);
        m_poller.setPollerConfig(m_pollerConfig);
        m_scheduler = new LegacyScheduler("Poller", 2);
        m_poller.setScheduler(m_scheduler);
    }

    @After
    public void tearDown() {
        m_scheduler.getRunner().shutdownNow();
    }

    @Test
    public void resizeSchedulerAppliesConfiguredThreads() {
        when(m_pollerConfig.getThreads()).thenReturn(9);

        m_poller.resizeScheduler();

        assertThat(m_scheduler.getThreads(), equalTo(9));
    }

    @Test
    public void resizeSchedulerToleratesMissingScheduler() {
        m_poller.setScheduler(null);
        when(m_pollerConfig.getThreads()).thenReturn(9);

        m_poller.resizeScheduler();
    }
}
