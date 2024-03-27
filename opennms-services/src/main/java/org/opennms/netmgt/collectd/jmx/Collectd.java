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
package org.opennms.netmgt.collectd.jmx;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.opennms.netmgt.collectd.CollectableService;
import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.scheduler.LegacyScheduler;

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
    public long getCorePoolThreads() {
        if (getThreadPoolStatsStatus()) {
            return getExecutor().getCorePoolSize();
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

    private static final String[] SCHEDULE_ITEMS = new String[]{"nodeId", "ipAddress", "package", "service", "intervalMs", "lastRunMs", "nextRunMs", "lastRunAbsoluteMs", "nextRunAbsoluteMs"};
    private static final String[] SCHEDULE_DESCS = new String[]{"nodeId", "ipAddress", "package", "service", "intervalMs", "lastRunMs", "nextRunMs", "lastRunAbsoluteMs", "nextRunAbsoluteMs"};
    private static final OpenType<?>[] SCHEDULE_TYPES = new OpenType<?>[]{SimpleType.INTEGER, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG};

    public final static CompositeType SCHEDULE_COMPOSITE_TYPE;
    public final static TabularType SCHEDULE_TABULAR_TYPE;

    static {
        try {
            SCHEDULE_COMPOSITE_TYPE = new CompositeType("Collectable Service", "Scheduled Collectable Services",
                    SCHEDULE_ITEMS,
                    SCHEDULE_DESCS,
                    SCHEDULE_TYPES
            );
            SCHEDULE_TABULAR_TYPE = new TabularType("Collectable Services", "Scheduled Collectable Service", SCHEDULE_COMPOSITE_TYPE, new String[]{"nodeId", "ipAddress", "package", "service"});
        } catch (OpenDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TabularData getSchedule() throws OpenDataException {
        final long currentTimeMs = System.currentTimeMillis();
        final TabularData tabularData = new TabularDataSupport(SCHEDULE_TABULAR_TYPE);

        final List<CollectableService> collectableServiceList = getDaemon().getCollectableServices();

        synchronized (collectableServiceList) {
            final CompositeDataSupport[] compositeData = collectableServiceList.stream()
                    .map(c -> {
                                try {
                                    final String ipAddress = str(c.getAddress());
                                    final String pkg = c.getPackageName();
                                    final String service = c.getServiceName();
                                    final int nodeId = c.getNodeId();
                                    final long intervalMs = c.getSpecification().getInterval();
                                    final long lastRunAbsolute = c.getLastScheduledCollectionTime();
                                    final long lastRunMs =  lastRunAbsolute - currentTimeMs;
                                    final long timeLeftMs = c.getSpecification().getInterval() - (currentTimeMs - lastRunAbsolute);
                                    final long nextRunMs = timeLeftMs;
                                    final long nextRunAbsolute = lastRunAbsolute + timeLeftMs;

                                    return new CompositeDataSupport(
                                            SCHEDULE_COMPOSITE_TYPE,
                                            SCHEDULE_ITEMS,
                                            new Object[]{nodeId, ipAddress, pkg, service, intervalMs, lastRunMs, nextRunMs, lastRunAbsolute, nextRunAbsolute}
                                    );
                                } catch (OpenDataException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    )
                    .toArray(CompositeDataSupport[]::new);

            tabularData.putAll(compositeData);

            return tabularData;
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
