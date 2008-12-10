//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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
					sleep(500);
				}
			};
			threadPool.execute(r);
		}
		threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
		threadPool.awaitTerminationAfterShutdown();
	}

}
