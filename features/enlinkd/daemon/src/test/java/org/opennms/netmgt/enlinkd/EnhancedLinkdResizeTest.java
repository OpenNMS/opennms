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
package org.opennms.netmgt.enlinkd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.scheduler.LegacyPriorityExecutor;
import org.opennms.netmgt.scheduler.LegacyScheduler;

/**
 * Verifies that Enlinkd applies the configured thread counts to its scheduler and
 * priority executor.
 */
public class EnhancedLinkdResizeTest {

    private final EnhancedLinkd m_linkd = new EnhancedLinkd();
    private EnhancedLinkdConfig m_config;
    private LegacyScheduler m_scheduler;
    private LegacyPriorityExecutor m_executor;

    @Before
    public void setUp() {
        m_config = mock(EnhancedLinkdConfig.class);
        m_linkd.setLinkdConfig(m_config);
        m_scheduler = new LegacyScheduler("EnhancedLinkd", 2);
        m_linkd.setScheduler(m_scheduler);
        m_executor = new LegacyPriorityExecutor("EnhancedLinkd", 2, 5);
        m_linkd.setExecutor(m_executor);
    }

    @After
    public void tearDown() {
        m_scheduler.getRunner().shutdownNow();
        m_executor.stop();
    }

    @Test
    public void resizeExecutorsAppliesBothConfiguredThreadCounts() {
        when(m_config.getThreads()).thenReturn(5);
        when(m_config.getExecutorThreads()).thenReturn(3);

        m_linkd.resizeExecutors();

        assertThat(m_scheduler.getThreads(), equalTo(5));
        assertThat(m_executor.getThreads(), equalTo(3));
    }

    @Test
    public void resizeExecutorsToleratesMissingPools() {
        m_linkd.setScheduler(null);
        m_linkd.setExecutor(null);
        when(m_config.getThreads()).thenReturn(5);
        when(m_config.getExecutorThreads()).thenReturn(3);

        m_linkd.resizeExecutors();
    }
}
