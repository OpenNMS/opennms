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
package org.opennms.core.concurrent;

import java.util.concurrent.ExecutorService;

/**
 * This factory is used to create {@link ExecutorService} thread pools for use by
 * daemon processes. This is provided as a service so that thread pools throughout
 * the system are constructed in the same manner and have consistent thread names,
 * logging, and queue implementations.
 * 
 * @author Seth
 */
public interface ExecutorFactory {

	/**
	 * Construct a new {@link ExecutorService} with an unbounded queue size and
	 * a thread pool size equal to the value of {@link Runtime#availableProcessors()}
	 * so that one thread per core is started.
	 * 
	 * @param daemonName
	 * @param executorName
	 * @return
	 */
	ExecutorService newExecutor(String daemonName, String executorName);

	/**
	 * Construct a new {@link ExecutorService} with an unbounded queue size 
	 * ({@link Integer#MAX_VALUE).
	 * 
	 * For CPU-intensive tasks, it is a good idea to use the value of
	 * {@link Runtime#availableProcessors()} (or a reasonable multiple of it
	 * based on the tasks) for the <code>threads</code> parameter to ensure 
	 * that the CPU is fully utilized.
	 * 
	 * @param threads
	 * @param daemonName
	 * @param executorName
	 * @return An ExecutorService pool
	 */
	ExecutorService newExecutor(int threads, String daemonName, String executorName);

	/**
	 * Construct a new {@link ExecutorService} with a specified queue size for 
	 * the backlog of tasks. When the queue is full, the pool may block,
	 * discard the incoming task, or throw an exception. This behavior is
	 * dependent on the implementation of the {@link ExecutorService}.
	 * 
	 * For CPU-intensive tasks, it is a good idea to use the value of
	 * {@link Runtime#availableProcessors()} (or a reasonable multiple of it
	 * based on the tasks) for the <code>threads</code> parameter to ensure 
	 * that the CPU is fully utilized.
	 * 
	 * @param threads
	 * @param daemonName
	 * @param executorName
	 * @return An ExecutorService pool
	 */
	ExecutorService newExecutor(int threads, int queueSize, String daemonName, String executorName);
}
