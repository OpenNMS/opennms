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
                return null;
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
