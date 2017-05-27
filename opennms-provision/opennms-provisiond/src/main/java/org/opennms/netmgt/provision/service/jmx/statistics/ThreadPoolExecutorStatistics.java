/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.jmx.statistics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.provision.service.jmx.annotation.Description;
import org.opennms.netmgt.provision.service.jmx.annotation.TableId;

@Description("Holds statistic about a ThreadPoolExecutor")
public class ThreadPoolExecutorStatistics {

    @TableId
    @Description("The name of the executor service")
    private final String poolName;

    @Description("The core number of threads")
    private int corePoolSize;

    @Description("The approximate number of threads that are actively executing tasks")
    private int activeCount;

    @Description("The approximate total number of tasks that have completed execution. \n" +
            "Because the states of tasks and threads may change dynamically during computation, the returned value \n" +
            "is only an approximation, but one that does not ever decrease across successive calls.")
    private long completedTaskCount;

    @Description("The thread keep-alive time, which is the amount of time that threads in excess of the core pool " +
            "size may remain idle before being terminated.")
    private long keepAliveTimeInMs;

    @Description("The largest number of threads that have ever simultaneously been in the pool.")
    private int largestPoolSize;

    @Description("The current number of threads in the pool.")
    private int poolSize;

    @Description("The approximate total number of tasks that have ever been scheduled for execution. \n" +
            "Because the states of tasks and threads may change dynamically during computation, the returned \n" +
            "value is only an approximation.")
    private long taskCount;

    private BlockingQueue<Runnable> queue;

    public ThreadPoolExecutorStatistics(String name, ThreadPoolExecutor threadPoolExecutor) {
        this.poolName = name;
        this.activeCount = threadPoolExecutor.getActiveCount();
        this.completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        this.corePoolSize = threadPoolExecutor.getCorePoolSize();
        this.keepAliveTimeInMs = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
        this.largestPoolSize = threadPoolExecutor.getLargestPoolSize();
        this.poolSize = threadPoolExecutor.getPoolSize();
        this.taskCount = threadPoolExecutor.getTaskCount();
        this.queue = threadPoolExecutor.getQueue();
    }

    public String getPoolName() {
        return poolName;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    public long getKeepAliveTimeInMs() {
        return keepAliveTimeInMs;
    }

    public int getLargestPoolSize() {
        return largestPoolSize;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public long getTaskCount() {
        return taskCount;
    }

    @Description("The current size of the queue used by this executor service")
    public int getQueueCurrentSize() {
        if (queue != null) {
            return queue.size();
        }
        return -1;
    }

    @Description("The total size of the queue used by this executor service")
    public long getQueueTotalSize() {
        if (queue != null) {
            return queue.remainingCapacity() + queue.size();
        }
        return -1;
    }

    @Description("The remaining capacity of the queue used by this executor service")
    public int getQueueRemainingCapacity() {
        if (queue != null) {
            return queue.remainingCapacity();
        }
        return -1;
    }

    @Description("The task completion ratio of this executor service")
    public double getTaskCompletionRatio() {
        if (getTaskCount() > 0) {
            return getCompletedTaskCount() / getTaskCount();
        }
        return 0;
    }
}
