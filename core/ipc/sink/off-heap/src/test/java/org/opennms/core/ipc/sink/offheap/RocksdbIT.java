/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Hashtable;
import java.util.concurrent.Executors;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksdbIT {

    private static final Logger LOG = LoggerFactory.getLogger(RocksdbIT.class);
    private RocksdbStore rocksdb;
    private final long NUM_OF_MESSAGES = 1000;
    private ConfigurationAdmin configAdmin;


    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException, RocksDBException {
        File data = tempFolder.newFolder("rocksdb");
        Hashtable<String, Object> configProperties = new Hashtable<>();
        configProperties.put(RocksdbStore.OFFHEAP_SIZE, "10MB");
        configProperties.put(RocksdbStore.OFFHEAP_PATH, data.getAbsolutePath());
        configAdmin = mock(ConfigurationAdmin.class, RETURNS_DEEP_STUBS);
        when(configAdmin.getConfiguration(RocksdbStore.OFFHEAP_CONFIG).getProperties()).thenReturn(configProperties);
        rocksdb = new RocksdbStore(configAdmin);
        rocksdb.init();
    }


    /**
     * Test Rocksdb by writing and reading from the store with different modules in parallel.
     *
     * @throws InterruptedException
     * @throws WriteFailedException
     */
    @Test
    public void testRocksDBMultipleModulesInParallel() throws InterruptedException, WriteFailedException {

        Executors.newSingleThreadExecutor().execute(() -> {
            // Trying to make read happens first and then write happens
            try {
                Thread.sleep(1000);
                writeMessages("Syslog", 0, NUM_OF_MESSAGES);
            } catch (InterruptedException | WriteFailedException e) {
                //pass
            }

        });
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                writeMessages("Trap", 0, NUM_OF_MESSAGES);
            } catch (WriteFailedException e) {
                //pass
            }
        });
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                writeMessages("Events", 0, NUM_OF_MESSAGES);
            } catch (WriteFailedException e) {
                //pass
            }
        });
        readAndVerifyMessages("Syslog", 0, NUM_OF_MESSAGES);
        readAndVerifyMessages("Trap", 0, NUM_OF_MESSAGES);
        readAndVerifyMessages("Events", 0, NUM_OF_MESSAGES);
        // Add more messages now and check if they can also be read
        Executors.newSingleThreadExecutor().execute(() -> {
            // Trying to make read happens first and then write happens
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //pass
            }
            try {
                writeMessages("Events", NUM_OF_MESSAGES, NUM_OF_MESSAGES);
            } catch (WriteFailedException e) {
                //pass
            }
        });
        readAndVerifyMessages("Events", NUM_OF_MESSAGES, NUM_OF_MESSAGES);

    }


    /**
     * Test to verify that max size can be specified and Rocksdb throws Exception.
     * We should be able to read messages after that and write new messages.
     */
    @Test
    @Ignore("Want to verify if this test is causing issues in bamboo")
    public void testRocksDBThrowExceptionWhenMaxLimitReached() throws InterruptedException, WriteFailedException, IOException, RocksDBException {

        int numOfElements = 2000000;
        for (int i = 0; i < numOfElements; i++) {
            String message = "Events" + " message " + i;
            try {
                rocksdb.writeMessage("Events", message.getBytes());
            } catch (WriteFailedException e) {
                LOG.error("Num of elements written to DB {} size = {}", rocksdb.getNumOfMessages("Events"), rocksdb.getSize(), e);
                break;
            }
        }
        assertNotEquals(rocksdb.getNumOfMessages("Events"), 0);
        assertThat(rocksdb.getNumOfMessages("Events"), Matchers.lessThan(numOfElements));
        int numOfMessagesWritten = rocksdb.getNumOfMessages("Events");
        // The read should happen even after Exception ( we restart DB if we encounter IOError in read).
        readAndVerifyMessages("Events", 0, numOfMessagesWritten);
        // Should be able to write messages again.
        writeMessages("Events", 0, 10000);

    }


    @Test
    public void testRocksDBStateAcrossSessions() throws WriteFailedException, IOException, InterruptedException, RocksDBException {

        writeMessages("Test", 0, 100);
        rocksdb.destroy();
        rocksdb = new RocksdbStore(configAdmin);
        rocksdb.init();
        //Read first 25 messages
        readAndVerifyMessages("Test", 0, 25);
        rocksdb.destroy();
        rocksdb = new RocksdbStore(configAdmin);
        rocksdb.init();
        //Read next 75 messages
        readAndVerifyMessages("Test", 25, 75);

    }

    private void readAndVerifyMessages(String moduleName, long initialValue, long numOfElements) throws InterruptedException {

        for (long i = initialValue; i < initialValue + numOfElements; i++) {
            AbstractMap.SimpleImmutableEntry<Long, byte[]> keyValue = rocksdb.readNextMessage(moduleName);
            if (keyValue == null) {
                i--;
                continue;
            }
            String message = new String(keyValue.getValue());
            String matcher = moduleName + " message " + i;
            assertEquals(matcher, message);

        }
    }


    private long writeMessages(String moduleName, long initialValue, long numofElements) throws WriteFailedException {
        long j = 0;
        for (long i = initialValue; i < initialValue + numofElements; i++) {
            String message = moduleName + " message " + i;
            rocksdb.writeMessage(moduleName, message.getBytes());
            j++;
        }
        return j;
    }


    @After
    public void destroy() {
        rocksdb.destroy();
    }
}
