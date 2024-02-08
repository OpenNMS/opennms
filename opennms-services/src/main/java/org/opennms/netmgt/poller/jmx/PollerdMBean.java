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
package org.opennms.netmgt.poller.jmx;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>PollerdMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface PollerdMBean extends BaseOnmsMBean {
    /**
     * Returns the number of polls that have been executed so far (counter).
     *
     * @return the number of polls that have been executed
     */
    public long getNumPolls();

    /**
     * @return The number of currently active poller threads
     */
    public long getActiveThreads();
    
    /**
     * @return The cumulative number of polling tasks scheduled since poller startup
     */
    public long getTasksTotal();
    
    /**
     * @return The cumulative number of polling tasks completed since poller startup
     */
    public long getTasksCompleted();
    
    /**
     * @return The ratio of completed to scheduled polling tasks since poller startup
     */
    public double getTaskCompletionRatio();
    
     /**
     * @return The current number of threads in the pool
     */
    public long getNumPoolThreads();

    /**
     * @return The largest size of the poller thread pool since poller startup
     */
    public long getPeakPoolThreads();

    /**
     * @return The core number of threads
     */
    public long getCorePoolThreads();

    /**
     * @return The maximum number of threads allowed in the poller's thread pool
     */
    public long getMaxPoolThreads();

    /**
     * @return The number of pending tasks on our ExecutorService
     */
    public long getTaskQueuePendingCount();

    /**
     * @return The number of open slots on our ExecutorService queue.
     */
    public long getTaskQueueRemainingCapacity();

    public long getNumPollsInFlight();

    public TabularData getSchedule() throws OpenDataException;
}
