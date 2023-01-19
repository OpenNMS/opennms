/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
 * <p>ProvisiondMBean interface.</p>
 */
public interface ProvisiondMBean extends BaseOnmsMBean {
    /*
     Scheduled / Rescan
     */
    
    /**
     * @return The scheduled number of currently active provision threads
     */
    long getScheduledActiveThreads();

    /**
     * @return The current scheduled number of threads in the pool
     */
    long getScheduledNumPoolThreads();

    /**
     * @return The maximum number of provision threads
     */
    long getScheduledMaxPoolThreads();

    /**
     * @return The core number of threads
     */
    long getScheduledCorePoolThreads();

    /**
     * @return The cumulative number of scheduled tasks since provision startup
     */
    long getScheduledTasksTotal();

    /**
     * @return The cumulative number of scheduled tasks completed since provision startup
     */
    long getScheduledTasksCompleted();

    /**
     * @return The ratio of completed to scheduled tasks since provision startup
     */
    double getScheduledTaskCompletionRatio();

    /**
     * @return The number of pending tasks
     */
    long getScheduledTaskQueuePendingCount();

    /**
     * @return The number of pending tasks
     */
    long getScheduledTaskQueueRemainingCapacity();

/*
    Scan
 */
    /**
     * @return The Scan number of currently active provision threads
     */
    long getScanActiveThreads();

    /**
     * @return The current Scan number of threads in the pool
     */
    long getScanNumPoolThreads();

    /**
     * @return The maximum Scan number of provision threads
     */
    long getScanMaxPoolThreads();

    /**
     * @return The core Scan number of threads
     */
    long getScanCorePoolThreads();

    /**
     * @return The cumulative number of Scan tasks Scan since provision startup
     */
    long getScanTasksTotal();

    /**
     * @return The cumulative number of Scan tasks completed since provision startup
     */
    long getScanTasksCompleted();

    /**
     * @return The ratio of completed to Scan tasks since provision startup
     */
    double getScanTaskCompletionRatio();

    /**
     * @return The number of pending tasks
     */
    long getScanTaskQueuePendingCount();

    /**
     * @return The number of pending tasks
     */
    long getScanTaskQueueRemainingCapacity();

    /*
    Import
    */
    /**
     * @return The Import number of currently active provision threads
     */
    long getImportActiveThreads();

    /**
     * @return The current Import number of threads in the pool
     */
    long getImportNumPoolThreads();

    /**
     * @return The maximum Import number of provision threads
     */
    long getImportMaxPoolThreads();

    /**
     * @return The core Import number of threads
     */
    long getImportCorePoolThreads();

    /**
     * @return The cumulative number of Import tasks Import since provision startup
     */
    long getImportTasksTotal();

    /**
     * @return The cumulative number of Import tasks completed since provision startup
     */
    long getImportTasksCompleted();

    /**
     * @return The ratio of completed to Import tasks since provision startup
     */
    double getImportTaskCompletionRatio();

    /**
     * @return The number of pending tasks
     */
    long getImportTaskQueuePendingCount();

    /**
     * @return The number of pending tasks
     */
    long getImportTaskQueueRemainingCapacity();
    
    /*
    Write
    */
    /**
     * @return The Write number of currently active provision threads
     */
    long getWriteActiveThreads();

    /**
     * @return The current Write number of threads in the pool
     */
    long getWriteNumPoolThreads();

    /**
     * @return The maximum Write number of provision threads
     */
    long getWriteMaxPoolThreads();

    /**
     * @return The core Write number of threads
     */
    long getWriteCorePoolThreads();

    /**
     * @return The cumulative number of Write tasks Write since provision startup
     */
    long getWriteTasksTotal();

    /**
     * @return The cumulative number of Write tasks completed since provision startup
     */
    long getWriteTasksCompleted();

    /**
     * @return The ratio of completed to Write tasks since provision startup
     */
    double getWriteTaskCompletionRatio();

    /**
     * @return The number of pending tasks
     */
    long getWriteTaskQueuePendingCount();

    /**
     * @return The number of pending tasks
     */
    long getWriteTaskQueueRemainingCapacity();
    
}
