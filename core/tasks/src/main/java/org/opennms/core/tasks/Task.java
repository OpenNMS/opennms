/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * BaseTask
 *
 * @author brozow
 * @version $Id: $
 */
public interface Task {
	
    public static enum State {
        NEW,
        SCHEDULED,
        SUBMITTED,
        COMPLETED
    }

    /**
     * <p>getCoordinator</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskCoordinator} object.
     */
    TaskCoordinator getCoordinator();

    /**
     * <p>getMonitor</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskMonitor} object.
     */
    TaskMonitor getMonitor();

    /**
     * This is called to add the task to the queue of tasks that can be considered to be runnable
     */
    void schedule();

    /**
     * Wait for this task to complete.  The current thread will block until this task has been completed.
     *
     * @throws java.lang.InterruptedException if any.
     * @throws java.util.concurrent.ExecutionException if any.
     */
    void waitFor() throws InterruptedException, ExecutionException;

    /**
     * Wait for this task to complete or until a timeout occurs. If the
     * timeout elapses, then false is returned.
     *
     * @param timeout a long.
     * @param unit a {@link java.util.concurrent.TimeUnit} object.
     * @throws java.lang.InterruptedException if any.
     */
    boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException;
}
