/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.jmx;

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
     * 
     * @return The largest size of the poller thread pool since poller startup
     */
    public long getPeakPoolThreads();
    
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
}
