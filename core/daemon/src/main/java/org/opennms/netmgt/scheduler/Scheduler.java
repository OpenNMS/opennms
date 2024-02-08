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
package org.opennms.netmgt.scheduler;

/**
 * <p>Scheduler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface Scheduler extends ScheduleTimer {

	/**
	 * {@inheritDoc}
	 *
	 * This method is used to schedule a ready runnable in the system. The
	 * interval is used as the key for determining which queue to add the
	 * runnable.
	 */
        @Override
	public abstract void schedule(long interval, final ReadyRunnable runnable);

	/**
	 * This returns the current time for the scheduler
	 *
	 * @return a long.
	 */
        @Override
	public abstract long getCurrentTime();

	/**
	 * Starts the fiber.
	 *
	 * @throws java.lang.IllegalStateException
	 *             Thrown if the fiber is already running.
	 */
	public abstract void start();

	/**
	 * Stops the fiber. If the fiber has never been run then an exception is
	 * generated.
	 *
	 * @throws java.lang.IllegalStateException
	 *             Throws if the fiber has never been started.
	 */
	public abstract void stop();

	/**
	 * Pauses the scheduler if it is current running. If the fiber has not been
	 * run or has already stopped then an exception is generated.
	 *
	 * @throws java.lang.IllegalStateException
	 *             Throws if the operation could not be completed due to the
	 *             fiber's state.
	 */
	public abstract void pause();

	/**
	 * Resumes the scheduler if it has been paused. If the fiber has not been
	 * run or has already stopped then an exception is generated.
	 *
	 * @throws java.lang.IllegalStateException
	 *             Throws if the operation could not be completed due to the
	 *             fiber's state.
	 */
	public abstract void resume();

	/**
	 * Returns the current of this fiber.
	 *
	 * @return The current status.
	 */
	public abstract int getStatus();

        /**
         * Returns the total number of scheduled tasks (ReadyRunnables) that have
         * been executed since the scheduler was initialized.
         *
         * @return the number of task executed
         */
        public abstract long getNumTasksExecuted();
}
