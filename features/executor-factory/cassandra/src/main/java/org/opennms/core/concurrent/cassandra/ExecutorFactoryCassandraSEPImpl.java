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
package org.opennms.core.concurrent.cassandra;

import java.util.concurrent.ExecutorService;

import org.apache.cassandra.concurrent.JMXEnabledSharedExecutorPool;
import org.apache.cassandra.concurrent.SharedExecutorPool;
import org.apache.cassandra.config.Config;
import org.opennms.core.concurrent.ExecutorFactory;

/**
 * This {@link ExecutorFactory} returns {@link ExecutorService} instances that are 
 * implemented with Cassandra's high performance, low-context-switching 
 * {@link SharedExecutorPool}.
 */
public class ExecutorFactoryCassandraSEPImpl implements ExecutorFactory {
	/**
	 * Create a shared executor pool for all of the threads used in this class
	 * 
	 * TODO: Make this into a map of separate pools per daemon?
	 */
	public static final JMXEnabledSharedExecutorPool SHARED = new JMXEnabledSharedExecutorPool("OpenNMS");

	static {
		// Turn Cassandra client mode on so that we can use the {@link SharedExecutorPool} classes.
		Config.setClientMode(true);
	}

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
		// Yes, the last two arguments are supposed to be reversed
		return SHARED.newExecutor(threads, queueSize, executorName, daemonName);
	}
}
