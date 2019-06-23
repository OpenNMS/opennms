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

import java.util.concurrent.ThreadPoolExecutor;
import org.opennms.netmgt.scheduler.LegacyScheduler;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;

/**
 * <p>Collectd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Collectd extends AbstractSpringContextJmxServiceDaemon<org.opennms.netmgt.collectd.Collectd> implements
        CollectdMBean {

    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return org.opennms.netmgt.collectd.Collectd.getLoggingCategory();
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "collectdContext";
    }

    @Override
    public long getActiveThreads() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getActiveCount();
        } else {
            return 0L;
        }
    }

    @Override
    public long getTasksTotal() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getTaskCount();
        } else {
            return 0L;
        }
    }

    @Override
    public long getTasksCompleted() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getCompletedTaskCount();
        } else {
            return 0L;
        }
    }

    @Override
    public double getTaskCompletionRatio() {
        if (getThreadPoolStatsStatus()) {
            if (getExecutor().getTaskCount() > 0) {
                return new Double(getExecutor().getCompletedTaskCount() / new Double(getExecutor().getTaskCount()));
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }

    @Override
    public long getNumPoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getPoolSize();
        } else {
            return 0L;
        }
    }

    @Override
    public long getMaxPoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getMaximumPoolSize();
        } else {
            return 0L;
        }
    }

    @Override
    public long getPeakPoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getLargestPoolSize();
        } else {
            return 0L;
        }
    }
    
    @Override
    public long getTaskQueuePendingCount() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getQueue().size();
        } else {
            return 0L;
        }
    }

    @Override
    public long getTaskQueueRemainingCapacity() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getQueue().remainingCapacity();
        } else {
            return 0L;
        }
    }

    @Override
    public long getCollectableServiceCount() {
        return getDaemon().getCollectableServiceCount();
    }
    
    private ThreadPoolExecutor getExecutor() {
        return (ThreadPoolExecutor) ((LegacyScheduler) getDaemon().getScheduler()).getRunner();
    }

    private boolean getThreadPoolStatsStatus() {
        return (getDaemon().getScheduler() instanceof LegacyScheduler);
    }
}
