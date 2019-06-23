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

import java.util.concurrent.ThreadPoolExecutor;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.scheduler.LegacyScheduler;

/**
 * <p>Pollerd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Pollerd extends AbstractSpringContextJmxServiceDaemon<org.opennms.netmgt.poller.Poller> implements PollerdMBean {

    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return org.opennms.netmgt.poller.Poller.getLoggingCategory();
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "pollerdContext";
    }
    
    /** {@inheritDoc} */
    @Override
    public long getActiveThreads() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getActiveCount();
        } else {
            return 0L;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public long getTasksTotal() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getTaskCount();
        } else {
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getTasksCompleted() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getCompletedTaskCount();
        } else {
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getNumPolls() {
        return getDaemon().getNumPolls();
    }

    public double getTaskCompletionRatio() {
        if (getThreadPoolStatsStatus()) {
            if (getExecutor().getTaskCount() > 0) {
                return new Double(getExecutor().getCompletedTaskCount() / new Double(getExecutor().getTaskCount()));
            } else {
                return new Double(0);
            }
        } else {
            return new Double(0);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public long getPeakPoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getLargestPoolSize();
        } else {
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getMaxPoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getMaximumPoolSize();
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
    
    private ThreadPoolExecutor getExecutor() {
        return (ThreadPoolExecutor) ((LegacyScheduler) getDaemon().getScheduler()).getRunner();
    }
    
    private boolean getThreadPoolStatsStatus() {
        return (getDaemon().getScheduler() instanceof LegacyScheduler);
    }
}
