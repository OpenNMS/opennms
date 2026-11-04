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

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>LinkdMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface EnhancedLinkdMBean extends BaseOnmsMBean {

    /**
     * @return the number of discovery tasks currently running
     */
    long getActiveThreads();

    /**
     * @return the cumulative number of discovery tasks handed to the thread pool since startup
     */
    long getTasksTotal();

    /**
     * @return the cumulative number of discovery tasks completed since startup
     */
    long getTasksCompleted();

    /**
     * @return the ratio of completed to submitted discovery tasks since startup
     */
    double getTaskCompletionRatio();

    /**
     * @return the current number of threads in the pool
     */
    long getNumPoolThreads();

    /**
     * @return the largest size of the thread pool since startup
     */
    long getPeakPoolThreads();

    /**
     * @return the configured core size of the thread pool
     */
    long getCorePoolThreads();

    /**
     * @return the configured maximum size of the thread pool
     */
    long getMaxPoolThreads();

    /**
     * @return discovery tasks that are ready to run and waiting for a free thread
     */
    long getTaskQueuePendingCount();

    /**
     * @return discovery tasks waiting in the delay queue, including those backing off
     */
    long getTaskQueueDelayedCount();
}
