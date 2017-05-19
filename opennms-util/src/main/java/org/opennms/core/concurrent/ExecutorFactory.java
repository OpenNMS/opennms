/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
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
