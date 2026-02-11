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
/**
 * <p>Linkd class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
package org.opennms.netmgt.enlinkd.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.scheduler.LegacyPriorityExecutor;

import java.util.concurrent.ThreadPoolExecutor;

public class EnhancedLinkd extends AbstractSpringContextJmxServiceDaemon<org.opennms.netmgt.enlinkd.EnhancedLinkd> implements EnhancedLinkdMBean {

    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return "enlinkd";
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "enhancedLinkdContext";
    }

    /** {@inheritDoc} */
    @Override
    public long getActiveThreads() {
        if (getThreadPoolStatsStatus()) {
            return getThreadPoolExecutor().getActiveCount();
        } else {
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getTasksTotal() {
        if (getThreadPoolStatsStatus()) {
            return getThreadPoolExecutor().getTaskCount();
        } else {
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getTasksCompleted() {
        if (getThreadPoolStatsStatus()) {
            return getThreadPoolExecutor().getCompletedTaskCount();
        } else {
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getTaskCompletionRatio() {
        if (getThreadPoolStatsStatus()) {
            if (getThreadPoolExecutor().getTaskCount() > 0) {
                return (double) getThreadPoolExecutor().getCompletedTaskCount() / (double) getThreadPoolExecutor().getTaskCount();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getNumPoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getThreadPoolExecutor().getPoolSize();
        } else {
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getPeakPoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getThreadPoolExecutor().getLargestPoolSize();
        } else {
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getCorePoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getThreadPoolExecutor().getCorePoolSize();
        } else {
            return 0L;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getMaxPoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getThreadPoolExecutor().getMaximumPoolSize();
        } else {
            return 0L;
        }
    }

    @Override
    public long getTaskQueuePendingCount() {
        final LegacyPriorityExecutor executor = getPriorityExecutor();
        if (executor != null) {
            return executor.getTaskQueuePendingCount();
        } else {
            return 0L;
        }
    }

    @Override
    public long getTaskQueueRemainingCapacity() {
        final LegacyPriorityExecutor executor = getPriorityExecutor();
        if (executor != null) {
            return executor.getTaskQueueRemainingCapacity();
        } else {
            return 0L;
        }
    }

    private LegacyPriorityExecutor getPriorityExecutor() {
        return getDaemon().getExecutor();
    }

    private ThreadPoolExecutor getThreadPoolExecutor() {
        return (ThreadPoolExecutor) getPriorityExecutor().getRunner();
    }

    private boolean getThreadPoolStatsStatus() {
        final LegacyPriorityExecutor executor = getPriorityExecutor();
        return executor != null && executor.getRunner() instanceof ThreadPoolExecutor;
    }
}
