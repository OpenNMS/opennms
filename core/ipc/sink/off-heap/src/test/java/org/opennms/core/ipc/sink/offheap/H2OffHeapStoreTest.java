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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Hashtable;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class H2OffHeapStoreTest {

    private static final Logger LOG = LoggerFactory.getLogger(H2OffHeapStoreTest.class);
    private H2OffHeapStore queue;
    private final static String OFFHEAP_SIZE = "offHeapSize";
    public static final String OFFHEAP_CONFIG = "org.opennms.core.ipc.sink.offheap";

    @Before
    public void setup() throws IOException {
        Hashtable<String, Object> configProperties = new Hashtable<>();
        configProperties.put(OFFHEAP_SIZE, "1MB");
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(OFFHEAP_CONFIG).getProperties()).thenReturn(configProperties);
        queue = new H2OffHeapStore(configAdmin);
        queue.init();
    }

    @Test
    public void testH2DataStore() throws InterruptedException, WriteFailedException {

        long beforeWrite = System.currentTimeMillis();
        Executors.newSingleThreadExecutor().execute(() -> {
            for (int i = 0; i < 1000; i++) {
                String message = "This is " + i + " trap message";
                try {
                    queue.writeMessage(message.getBytes(), "traps-test", Integer.toString(i));
                } catch (WriteFailedException e) {
                   // pass
                }
            }
        });
        
        Executors.newSingleThreadExecutor().execute(() -> {
            for (int i = 0; i < 1000; i++) {
                String message = "This is " + i + " syslog message";
                try {
                    queue.writeMessage(message.getBytes(), "syslog-test", Integer.toString(i));
                } catch (WriteFailedException e) {
                    // pass
                }
            }
        });

        for (int i = 0; i < 1000; i++) {
            String message = "This is " + i + " event message";
            queue.writeMessage(message.getBytes(), "events-test", Integer.toString(i));
        }

        long afterWrite = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            AbstractMap.SimpleImmutableEntry<String, byte[]> keyValue = queue.readNextMessage("traps-test");
                String message = new String(keyValue.getValue());
                String matcher = "This is " + i + " trap message";
                assertEquals(matcher, message);

        }
        for (int i = 0; i < 1000; i++) {
            AbstractMap.SimpleImmutableEntry<String, byte[]> keyValue = queue.readNextMessage("syslog-test");
                String message = new String(keyValue.getValue());
                String matcher = "This is " + i + " syslog message";
                assertEquals(matcher, message);
        }
        for (int i = 0; i < 1000; i++) {
            AbstractMap.SimpleImmutableEntry<String, byte[]> keyValue = queue.readNextMessage("events-test");
                String message = new String(keyValue.getValue());
                String matcher = "This is " + i + " event message";
                assertEquals(matcher, message);
        }
        long afterRead = System.currentTimeMillis();
        LOG.info("Total Write time  " + (afterWrite - beforeWrite));
        LOG.info("Total read time  " + (afterRead - afterWrite));
        LOG.info("Total time  " + (afterRead - beforeWrite));
    }

    @After
    public void destroy() throws InterruptedException {
        queue.destroy();
    }

}
