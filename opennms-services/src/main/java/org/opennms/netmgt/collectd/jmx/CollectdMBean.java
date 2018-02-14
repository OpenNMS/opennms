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

package org.opennms.netmgt.collectd.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>CollectdMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectdMBean extends BaseOnmsMBean {
    /**
     * @return The number of currently active collection threads
     */
    public long getActiveThreads();

    /**
     * @return The active number of collection threads
     */
    public long getNumPoolThreads();

    /**
     * @return The maximum number of collection threads
     */
    public long getMaxPoolThreads();

    /**
     * @return The peak number of collection threads in use.
     */
    public long getPeakPoolThreads();

    /**
     * @return The cumulative number of collection tasks scheduled since collector startup
     */
    public long getTasksTotal();

    /**
     * @return The cumulative number of collection tasks completed since collector startup
     */
    public long getTasksCompleted();

    /**
     * @return The ratio of completed to scheduled collection tasks since collector startup
     */
    public double getTaskCompletionRatio();
    
    /**
     * @return The number of collectable services currently seen by Collectd
     */
    public long getCollectableServiceCount();

    /**
     * @return The number of pending tasks
     */
    public long getTaskQueuePendingCount();

    /**
     * @return The number of pending tasks
     */
    public long getTaskQueueRemainingCapacity();
}
