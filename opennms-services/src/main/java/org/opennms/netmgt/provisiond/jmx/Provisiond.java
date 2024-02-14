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
package org.opennms.netmgt.provisiond.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.provision.service.Provisioner;

import java.util.concurrent.ThreadPoolExecutor;

import static org.opennms.netmgt.provisiond.jmx.Provisiond.ExecutorType.Import;
import static org.opennms.netmgt.provisiond.jmx.Provisiond.ExecutorType.Rescan;
import static org.opennms.netmgt.provisiond.jmx.Provisiond.ExecutorType.Scan;
import static org.opennms.netmgt.provisiond.jmx.Provisiond.ExecutorType.Write;

/**
 * <p>
 * Provisiond class is used to expose JMX properties for Import, Scan, Rescan and Write thread pools
 * Import, Scan and Write executors are handled by a TaskCoordinator
 * Rescan seems to be handled by two different executors in Provisioner: newSuspectExecutor and scheduledExecutor
 * <p>
 * The main Manage Bean is Provisioner from opennms-provision
 * </p>
 */
public class Provisiond extends AbstractSpringContextJmxServiceDaemon<Provisioner> implements
        ProvisiondMBean {

    /**
     * Log4j category
     */
    static final String LOG4J_CATEGORY = "provisiond";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getLoggingPrefix() {
        return LOG4J_CATEGORY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSpringContext() {
        return "provisiondContext";
    }

    /***** Scheduled *****/
    @Override
    public long getScheduledActiveThreads() {
        return getExecutor(Rescan).getActiveCount();
    }

    @Override
    public long getScheduledNumPoolThreads() {
        return getExecutor(Rescan).getPoolSize();
    }

    @Override
    public long getScheduledMaxPoolThreads() {
        return getExecutor(Rescan).getMaximumPoolSize();
    }

    @Override
    public long getScheduledCorePoolThreads() {
        return getExecutor(Rescan).getCorePoolSize();
    }

    @Override
    public long getScheduledTasksTotal() {
        return getExecutor(Rescan).getTaskCount();
    }

    @Override
    public long getScheduledTasksCompleted() {
        return getExecutor(Rescan).getCompletedTaskCount();
    }

    @Override
    public double getScheduledTaskCompletionRatio() {
        if (getExecutor(ExecutorType.Rescan).getTaskCount() > 0) {
            return getExecutor(ExecutorType.Rescan).getCompletedTaskCount() / (double) getExecutor(ExecutorType.Rescan).getTaskCount();
        } else {
            return 0.0;
        }
    }

    @Override
    public long getScheduledTaskQueuePendingCount() {
        return getExecutor(ExecutorType.Rescan).getQueue().size();
    }

    @Override
    public long getScheduledTaskQueueRemainingCapacity() {
        return getExecutor(ExecutorType.Rescan).getQueue().remainingCapacity();
    }

    /****** Scan *****/

    @Override
    public long getScanActiveThreads() {
        return getExecutor(Scan).getActiveCount();
    }

    @Override
    public long getScanNumPoolThreads() {
        return getExecutor(Scan).getPoolSize();
    }

    @Override
    public long getScanMaxPoolThreads() {
        return getExecutor(Scan).getMaximumPoolSize();
    }

    @Override
    public long getScanCorePoolThreads() {
        return getExecutor(Scan).getCorePoolSize();
    }

    @Override
    public long getScanTasksTotal() {
        return getExecutor(Scan).getTaskCount();
    }

    @Override
    public long getScanTasksCompleted() {
        return getExecutor(Scan).getCompletedTaskCount();
    }


    @Override
    public double getScanTaskCompletionRatio() {
        if (getExecutor(Scan).getTaskCount() > 0) {
            return getExecutor(Scan).getCompletedTaskCount() / (double) getExecutor(Scan).getTaskCount();
        } else {
            return 0.0;
        }
    }

    @Override
    public long getScanTaskQueuePendingCount() {
        return getExecutor(Scan).getQueue().size();
    }

    @Override
    public long getScanTaskQueueRemainingCapacity() {
        return getExecutor(Scan).getQueue().remainingCapacity();
    }

    /****** Import *****/

    @Override
    public long getImportActiveThreads() {
        return getExecutor(Import).getActiveCount();
    }

    @Override
    public long getImportNumPoolThreads() {
        return getExecutor(Import).getPoolSize();
    }

    @Override
    public long getImportMaxPoolThreads() {
        return getExecutor(Import).getMaximumPoolSize();
    }

    @Override
    public long getImportCorePoolThreads() {
        return getExecutor(Import).getCorePoolSize();
    }

    @Override
    public long getImportTasksTotal() {
        return getExecutor(Import).getTaskCount();
    }

    @Override
    public long getImportTasksCompleted() {
        return getExecutor(Import).getCompletedTaskCount();
    }


    @Override
    public double getImportTaskCompletionRatio() {
        if (getExecutor(Import).getTaskCount() > 0) {
            return getExecutor(Import).getCompletedTaskCount() / (double) getExecutor(Import).getTaskCount();
        } else {
            return 0.0;
        }
    }

    @Override
    public long getImportTaskQueuePendingCount() {
        return getExecutor(Import).getQueue().size();
    }

    @Override
    public long getImportTaskQueueRemainingCapacity() {
        return getExecutor(Import).getQueue().remainingCapacity();
    }

    /****** Write *****/

    @Override
    public long getWriteActiveThreads() {
        return getExecutor(Write).getActiveCount();
    }

    @Override
    public long getWriteNumPoolThreads() {
        return getExecutor(Write).getPoolSize();
    }

    @Override
    public long getWriteMaxPoolThreads() {
        return getExecutor(Write).getMaximumPoolSize();
    }

    @Override
    public long getWriteCorePoolThreads() {
        return getExecutor(Write).getCorePoolSize();
    }

    @Override
    public long getWriteTasksTotal() {
        return getExecutor(Write).getTaskCount();
    }

    @Override
    public long getWriteTasksCompleted() {
        return getExecutor(Write).getCompletedTaskCount();
    }


    @Override
    public double getWriteTaskCompletionRatio() {
        if (getExecutor(Write).getTaskCount() > 0) {
            return getExecutor(Write).getCompletedTaskCount() / (double) getExecutor(Write).getTaskCount();
        } else {
            return 0.0;
        }
    }

    @Override
    public long getWriteTaskQueuePendingCount() {
        return getExecutor(Write).getQueue().size();
    }

    @Override
    public long getWriteTaskQueueRemainingCapacity() {
        return getExecutor(Write).getQueue().remainingCapacity();
    }

    private ThreadPoolExecutor getExecutor(ExecutorType type) {
        switch (type) {
            case Import:
                return (ThreadPoolExecutor) (getDaemon().getTaskCoordinatorExecutorService(Import.name));
            case Scan:
                return (ThreadPoolExecutor) (getDaemon().getTaskCoordinatorExecutorService(Scan.name));
            case Rescan:
                return (ThreadPoolExecutor) (getDaemon().getScheduledExecutor());
            case Write:
                return (ThreadPoolExecutor) (getDaemon().getTaskCoordinatorExecutorService(Write.name));
            default:
                throw new IllegalArgumentException("unable to get executor for type: " + type);
        }
    }

    public enum ExecutorType {
        Import("import"),
        Scan("scan"),
        Rescan("rescan"),
        Write("write");

        public final String name;

        private ExecutorType(String label) {
            this.name = label;
        }
    }
}
