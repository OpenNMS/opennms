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
package org.opennms.netmgt.enlinkd.jmx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.netmgt.scheduler.LegacyPriorityExecutor;

public class EnhancedLinkdMBeanTest {

    private static EnhancedLinkd mbeanFor(final LegacyPriorityExecutor executor) {
        return new EnhancedLinkd() {
            @Override
            LegacyPriorityExecutor getPriorityExecutor() {
                return executor;
            }
        };
    }

    @Test
    public void reportsZeroBeforeTheExecutorExists() {
        final EnhancedLinkd mbean = mbeanFor(null);
        assertEquals(0L, mbean.getActiveThreads());
        assertEquals(0L, mbean.getTasksTotal());
        assertEquals(0L, mbean.getTasksCompleted());
        assertEquals(0d, mbean.getTaskCompletionRatio(), 0d);
        assertEquals(0L, mbean.getNumPoolThreads());
        assertEquals(0L, mbean.getPeakPoolThreads());
        assertEquals(0L, mbean.getCorePoolThreads());
        assertEquals(0L, mbean.getMaxPoolThreads());
        assertEquals(0L, mbean.getTaskQueuePendingCount());
        assertEquals(0L, mbean.getTaskQueueDelayedCount());
    }

    @Test
    public void reportsTheExecutorsCurrentPoolSize() {
        final LegacyPriorityExecutor executor = new LegacyPriorityExecutor("EnhancedLinkd", 2, 5);
        try {
            final EnhancedLinkd mbean = mbeanFor(executor);
            assertEquals(2L, mbean.getCorePoolThreads());
            assertEquals(2L, mbean.getMaxPoolThreads());
            assertEquals(0d, mbean.getTaskCompletionRatio(), 0d);

            // A reload resizes the same executor in place; the MBean must follow it.
            executor.setThreads(4);
            assertEquals(4L, mbean.getCorePoolThreads());
            assertEquals(4L, mbean.getMaxPoolThreads());
        } finally {
            executor.stop();
        }
    }
}
