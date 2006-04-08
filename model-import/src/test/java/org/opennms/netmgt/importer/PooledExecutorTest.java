package org.opennms.netmgt.importer;

import java.util.Date;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import junit.framework.TestCase;

public class PooledExecutorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void sleep(long millis) {
		try { Thread.sleep(millis); } catch(InterruptedException e) {}
	}
	
	public void testThreadPool() throws Exception {
		PooledExecutor threadPool = new PooledExecutor(new LinkedQueue(), 11);
		threadPool.setMinimumPoolSize(11);
		for(int i = 1; i <= 100; i++) {
			final int index = i;
			Runnable r = new Runnable() {
				public void run() {
					System.err.println(Thread.currentThread()+": "+new Date()+": "+index);
					sleep(1000);
				}
			};
			threadPool.execute(r);
		}
		threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
		threadPool.awaitTerminationAfterShutdown();
	}

}
