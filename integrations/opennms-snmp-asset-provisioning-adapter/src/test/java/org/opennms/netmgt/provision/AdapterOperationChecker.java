/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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