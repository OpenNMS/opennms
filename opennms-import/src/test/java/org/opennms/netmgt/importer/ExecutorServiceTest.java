/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.opennms.core.concurrent.LogPreservingThreadFactory;

public class ExecutorServiceTest {

	public void sleep(long millis) {
		try { Thread.sleep(millis); } catch(InterruptedException e) {}
	}
	
	private int runs = 0;
	
	public synchronized void incr() {
	    runs++;
	}
	
	public synchronized int getRuns() {
	    return runs;
	}

	@Test
	public void testThreadPool() throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(11, new LogPreservingThreadFactory(getClass().getSimpleName() + ".testThreadPool", 11, false));

		for(int i = 1; i <= 100; i++) {
			final int index = i;
			Runnable r = new Runnable() {
                                @Override
				public void run() {
					System.err.println(Thread.currentThread()+": "+new Date()+": "+index);
					sleep(500);
					incr();
				}
			};
			pool.execute(r);
		}
		

		shutdownAndWaitForCompletion(pool);
		
		assertEquals(100, getRuns());
	}

    public void shutdownAndWaitForCompletion(ExecutorService executorService) {
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // loop util the await returns false
            }
        } catch (InterruptedException e) {
            fail("Thread Interrupted unexpectedly!!!");
        }
    }
 
	
}
