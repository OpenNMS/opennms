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

package org.opennms.netmgt.capsd.jmx;

import java.util.concurrent.ThreadPoolExecutor;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;

/**
 * <p>Capsd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Capsd extends AbstractSpringContextJmxServiceDaemon<org.opennms.netmgt.capsd.Capsd> implements CapsdMBean {

    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return "OpenNMS.Capsd";
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "capsdContext";
    }
    
    /** {@inheritDoc} */
    @Override
    public long getActiveSuspectThreads() {
        return getSuspectExecutor().getActiveCount();
    }
    
    /** {@inheritDoc} */
    @Override
    public long getActiveRescanThreads() {
        return getRescanExecutor().getActiveCount();
    }
    
    /** {@inheritDoc} */
    @Override
    public long getSuspectCompletedTasks() {
        return getSuspectExecutor().getCompletedTaskCount();
    }

    /** {@inheritDoc} */
    @Override
    public long getRescanCompletedTasks() {
        return getRescanExecutor().getCompletedTaskCount();
    }
    
    /** {@inheritDoc} */
    @Override
    public long getSuspectTotalTasks() {
        return getSuspectExecutor().getTaskCount();
    }

    /** {@inheritDoc} */
    @Override
    public long getRescanTotalTasks() {
        return getRescanExecutor().getTaskCount();
    }
    
    /** {@inheritDoc} */
    @Override
    public double getSuspectTaskCompletionRatio() {
        if (getSuspectTotalTasks() == 0) {
            return 0.0;
        }
        return new Double(getSuspectCompletedTasks()) / new Double(getSuspectTotalTasks());
    }

    /** {@inheritDoc} */
    @Override
    public double getRescanTaskCompletionRatio() {
        if (getRescanTotalTasks() == 0) {
            return 0.0;
        }
        return new Double(getRescanCompletedTasks()) / new Double(getRescanTotalTasks());
    }

    /** {@inheritDoc} */
    @Override
    public long getSuspectQueueSize() {
        return getSuspectExecutor().getQueue().size();
    }

    /** {@inheritDoc} */
    @Override
    public long getRescanQueueSize() {
        return getRescanExecutor().getQueue().size();
    }

    private ThreadPoolExecutor getSuspectExecutor() {
        return (ThreadPoolExecutor) getDaemon().getSuspectRunner();
    }
    
    private ThreadPoolExecutor getRescanExecutor() {
        return (ThreadPoolExecutor) getDaemon().getRescanRunner();
    }

}
