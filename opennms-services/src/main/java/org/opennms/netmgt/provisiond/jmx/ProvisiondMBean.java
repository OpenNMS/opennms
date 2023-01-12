/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provisiond.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>CollectdMBean interface.</p>
 */
public interface ProvisiondMBean extends BaseOnmsMBean {
    /**
     * @return The number of currently active provision threads
     */
    long getActiveThreads();

    /**
     * @return The current number of threads in the pool
     */
    long getNumPoolThreads();

    /**
     * @return The maximum number of provision threads
     */
    long getMaxPoolThreads();

    /**
     * @return The core number of threads
     */
    long getCorePoolThreads();



    /**
     * @return The cumulative number of collection tasks scheduled since collector startup
     */
     long getTasksTotal();

    /**
     * @return The cumulative number of collection tasks completed since collector startup
     */
     long getTasksCompleted();

    /**
     * @return The ratio of completed to scheduled collection tasks since collector startup
     */
     double getTaskCompletionRatio();

    /**
     * @return The number of pending tasks
     */
     long getTaskQueuePendingCount();

    /**
     * @return The number of pending tasks
     */
     long getTaskQueueRemainingCapacity();
}
