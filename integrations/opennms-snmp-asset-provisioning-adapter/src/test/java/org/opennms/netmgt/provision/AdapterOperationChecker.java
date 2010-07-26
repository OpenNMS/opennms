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

	public void onEnqueueOperation(AdapterOperation op) {
		enqueueLatch.countDown();
		System.out.println("Enqueued!");
	}

	public void onDequeueOperation(final AdapterOperation op) {
		dequeueLatch.countDown();
		System.out.println("Dequeued!");

		final CountDownLatch threadStartup = new CountDownLatch(1);

		Thread dequeueWatcher = new Thread() {
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