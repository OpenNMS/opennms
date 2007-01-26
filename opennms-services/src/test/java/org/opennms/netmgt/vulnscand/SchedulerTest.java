package org.opennms.netmgt.vulnscand;

import junit.framework.TestCase;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.netmgt.config.VulnscandConfigFactory;


public class SchedulerTest extends TestCase {

	protected void setUp() throws Exception {
		
		System.setProperty("opennms.home", "src/test/test-configurations/vulnscand");
		
		VulnscandConfigFactory.init();
		
		super.setUp();
	
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testCreate() throws Exception {
//		FifoQueue q = new FifoQueueImpl();
//		Scheduler scheduler = new Scheduler(q);
//		Vulnscand vulnscand = null;
//		vulnscand.initialize();
		
		

	}

}
