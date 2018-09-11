/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.offheap;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class H2DataStoreTest {

    private H2DataStore queue;

    private boolean perf = false;

    @Before
    public void setup() {
        System.setProperty("org.opennms.minion.sink.queue.filename", "/home/chandra/dev/test/cq/h2/h2-data.data");
        queue = new H2DataStore();
        queue.init(3000000000L);
    }

    @Test
    public void testH2DataStore() throws InterruptedException {

        System.out.println("Size of store " + queue.getSize());
        long beforeWrite = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            String message = "This is " + i + "  message";
            queue.writeMessage(message.getBytes());
        }
        System.out.println("Size of store " + queue.getSize());
        long afterWrite = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            byte[] messageBytes = queue.readNextMessage();
            if (!perf) {
                String message = new String(messageBytes);
                String matcher = "This is " + i + "  message";
                assertEquals(matcher, message);
            }
        }
        long afterRead = System.currentTimeMillis();
        // if (perf) {
        System.out.println("Total Write time  " + (afterWrite - beforeWrite));
        System.out.println("Total read time  " + (afterRead - afterWrite));
        System.out.println("Total time  " + (afterRead - beforeWrite));
        // }
        Thread.sleep(3000);
        System.out.println("Size of store " + queue.getSize());
    }

    @After
    public void destroy() throws InterruptedException {
        queue.destroy();
        System.out.println("Size of store " + queue.getSize());
    }

}
