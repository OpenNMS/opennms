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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opennms.core.concurrent.LogPreservingThreadFactory;

public class ExecutorFactoryJavaImpl implements ExecutorFactory {

	@Override
	public ExecutorService newExecutor(String daemonName, String executorName) {
		return newExecutor(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, daemonName, executorName);
	}

	@Override
	public ExecutorService newExecutor(int threads, String daemonName, String executorName) {
		return newExecutor(threads, Integer.MAX_VALUE, daemonName, executorName);
	}

	@Override
	public ExecutorService newExecutor(int threads, int queueSize, String daemonName, String executorName) {
		return new ThreadPoolExecutor(
			threads,
			threads,
			1000L,
			TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(queueSize),
			new LogPreservingThreadFactory(daemonName + "-" + executorName, Integer.MAX_VALUE)
		);
	}
}
