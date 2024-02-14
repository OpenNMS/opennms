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
/**
 * 
 */
package org.opennms.netmgt.provision;

import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;

import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperation;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationQueueListener;

class AdapterOperationChecker implements AdapterOperationQueueListener {

	public final CountDownLatch enqueueLatch;
	public final CountDownLatch dequeueLatch;
	public final CountDownLatch executeLatch;

	public AdapterOperationChecker(int numberOfOperations) {
		enqueueLatch = new CountDownLatch(numberOfOperations);
		dequeueLatch = new CountDownLatch(numberOfOperations);
		executeLatch = new CountDownLatch(numberOfOperations);
	}

	@Override
	public void onEnqueueOperation(AdapterOperation op) {
		enqueueLatch.countDown();
		System.out.println("Enqueued!");
	}

	@Override
	public void onDequeueOperation(final AdapterOperation op) {
		dequeueLatch.countDown();
		System.out.println("Dequeued!");

		final CountDownLatch threadStartup = new CountDownLatch(1);

		Thread dequeueWatcher = new Thread() {
			@Override
			public void run() {
				synchronized (op) {
					try {
						System.out.println("Running execution listener thread");
						threadStartup.countDown();
						// Wait for a notifyAll() to be called by the AdapterOperation
						op.wait();
						System.out.println("Executed!");
						executeLatch.countDown();
					} catch (InterruptedException e) {
						fail(e.getMessage());
					}
				}
			}
		};
		// Start the execution listener thread
		dequeueWatcher.start();
		try {
			threadStartup.await();
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}
}