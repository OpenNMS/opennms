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
