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

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Schedule;

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
    public long getNumPoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getPoolSize();
        } else {
            return 0L;
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
    public long getCorePoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getCorePoolSize();
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

    @Override
    public long getNumPollsInFlight() {
        return getDaemon().getNetwork().getContext().getAsyncPollingEngine().getNumPollsInFlight();
    }

    private static final String[] SCHEDULE_ITEMS = new String[]{"nodeId", "nodeLabel", "nodeLocation", "ipAddress", "service", "readyTimeMs", "readyTimeAbsoluteMs", "status", "statusChangeTimeMs"};
    private static final String[] SCHEDULE_DESCS = new String[]{"nodeId", "nodeLabel", "nodeLocation", "ipAddress", "service", "readyTimeMs", "readyTimeAbsoluteMs", "status", "statusChangeTimeMs"};
    private static final OpenType<?>[] SCHEDULE_TYPES = new OpenType<?>[]{SimpleType.INTEGER, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.LONG, SimpleType.LONG, SimpleType.STRING, SimpleType.LONG};

    public final static CompositeType SCHEDULE_COMPOSITE_TYPE;
    public final static TabularType SCHEDULE_TABULAR_TYPE;

    static {
        try {
            SCHEDULE_COMPOSITE_TYPE = new CompositeType("Pollable Service", "Scheduled Pollable Services",
                    SCHEDULE_ITEMS,
                    SCHEDULE_DESCS,
                    SCHEDULE_TYPES
            );
            SCHEDULE_TABULAR_TYPE = new TabularType("Pollable Services", "Scheduled Pollable Service", SCHEDULE_COMPOSITE_TYPE, new String[]{"nodeId", "ipAddress", "service"});
        } catch (OpenDataException e) {
            throw new RuntimeException(e);
        }
    }

    public TabularData getSchedule() throws OpenDataException {
        final long currentTimeMs = System.currentTimeMillis();
        final TabularData tabularData = new TabularDataSupport(SCHEDULE_TABULAR_TYPE);
        final Map<Long, BlockingQueue<ReadyRunnable>> queue = ((LegacyScheduler) getDaemon().getScheduler()).getQueue();
        synchronized (queue) {
            for (final Map.Entry<Long, BlockingQueue<ReadyRunnable>> entry : queue.entrySet()) {
                final List<LegacyScheduler.TimeKeeper> pollableServiceList = entry.getValue().stream().map(r -> (LegacyScheduler.TimeKeeper) r).collect(Collectors.toList());
                for (final LegacyScheduler.TimeKeeper timeKeeper : pollableServiceList) {
                    final long readyTimeAbsoluteMs = timeKeeper.getTimeToRun();
                    final long readyTimeMs = readyTimeAbsoluteMs - currentTimeMs;
                    final PollableService pollableService = ((PollableService)((Schedule.ScheduleEntry)timeKeeper.getRunnable()).getSchedulable());
                    final String ipAddress = pollableService.getIpAddr();
                    final int nodeId= pollableService.getNodeId();
                    final String nodeLabel = pollableService.getNodeLabel();
                    final String nodeLocation = pollableService.getNodeLocation();
                    final String service=pollableService.getSvcName();
                    final String status = pollableService.getStatus().getStatusName();
                    final long statusChangeTimeMs = pollableService.getStatusChangeTime();
                    tabularData.put(new CompositeDataSupport(
                            SCHEDULE_COMPOSITE_TYPE,
                            SCHEDULE_ITEMS,
                            new Object[]{nodeId, nodeLabel, nodeLocation, ipAddress, service, readyTimeMs, readyTimeAbsoluteMs, status, statusChangeTimeMs}
                    ));
                }
            }
            return tabularData;
        }
    }

    private ThreadPoolExecutor getExecutor() {
        return (ThreadPoolExecutor) ((LegacyScheduler) getDaemon().getScheduler()).getRunner();
    }

    private boolean getThreadPoolStatsStatus() {
        return (getDaemon().getScheduler() instanceof LegacyScheduler);
    }
}
