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

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.provision.service.Provisioner;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>Provisiond class.</p>
 */
public class Provisiond extends AbstractSpringContextJmxServiceDaemon<Provisioner> implements
        ProvisiondMBean {

    /**
     * Log4j category
     */
    static final String LOG4J_CATEGORY = "provisiond";

    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return LOG4J_CATEGORY;
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "provisiondContext";
    }


    @Override
    public long getActiveThreads() {
        return getExecutor().getActiveCount();
    }

    @Override
    public long getNumPoolThreads() {
        return getExecutor().getPoolSize();
    }

    @Override
    public long getMaxPoolThreads() {
        return getExecutor().getMaximumPoolSize();
    }

    @Override
    public long getCorePoolThreads() {
        return getExecutor().getCorePoolSize();
    }

    @Override
    public long getTasksTotal() {
        return getExecutor().getTaskCount();
    }

    @Override
    public long getTasksCompleted() {
        return getExecutor().getCompletedTaskCount();
    }

    @Override
    public double getTaskCompletionRatio() {
        if (getExecutor().getTaskCount() > 0) {
            return getExecutor().getCompletedTaskCount() / (double) getExecutor().getTaskCount();
        } else {
            return 0.0;
        }
    }

    @Override
    public long getTaskQueuePendingCount() {
        return getExecutor().getQueue().size();
    }

    @Override
    public long getTaskQueueRemainingCapacity() {
        return getExecutor().getQueue().remainingCapacity();
    }

    private ThreadPoolExecutor getExecutor() {
        return (ThreadPoolExecutor)(getDaemon().getScheduledExecutor());
    }

}
