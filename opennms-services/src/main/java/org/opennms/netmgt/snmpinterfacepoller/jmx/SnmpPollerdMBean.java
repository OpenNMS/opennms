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
package org.opennms.netmgt.snmpinterfacepoller.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>SnmpPollerdMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface SnmpPollerdMBean extends BaseOnmsMBean {
    /**
     * @return The number of currently active snmp interface poller threads
     */
    public long getActiveThreads();

    /**
     * @return The current number of threads in the pool
     */
    public long getNumPoolThreads();

    /**
     * @return The maximum number of snmp interface poller threads
     */
    public long getMaxPoolThreads();

    /**
     * @return The core number of threads
     */
    public long getCorePoolThreads();

    /**
     * @return The peak number of snmp interface poller threads in use.
     */
    public long getPeakPoolThreads();

    /**
     * @return The cumulative number of snmp interface poller tasks scheduled since collector startup
     */
    public long getTasksTotal();

    /**
     * @return The cumulative number of snmp interface poller tasks completed since collector startup
     */
    public long getTasksCompleted();

    /**
     * @return The ratio of completed to scheduled snmp interface poller tasks since poller startup
     */
    public double getTaskCompletionRatio();

    /**
     * @return The number of pending tasks
     */
    public long getTaskQueuePendingCount();

    /**
     * @return The number of pending tasks
     */
    public long getTaskQueueRemainingCapacity();
}
