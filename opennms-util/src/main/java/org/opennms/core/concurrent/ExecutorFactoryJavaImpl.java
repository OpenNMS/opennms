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
