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

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.OffHeapStore;
import org.opennms.core.ipc.sink.api.OffHeapFifoQueue;

public class H2DataStore implements OffHeapFifoQueue {

    private static String fileName = System.getProperty("org.opennms.minion.sink.queue.filename");
    private MVStore store;
    private Queue<String> keys;
    private long maxSizeInBytes;

    public void init(long maxSizeInBytes) {
        OffHeapStore fileStore = new OffHeapStore();
        if (fileName != null) {
            store = new MVStore.Builder().fileName(fileName).open();
        } else {
            // Use offHeap store.
            store = new MVStore.Builder().fileStore(fileStore).open();
        }
        keys = new LinkedList<String>();
        this.maxSizeInBytes = maxSizeInBytes;
    }

    public void writeMessage(byte[] message) {
        store.getFileStore().size();
        if (store.getFileStore().size() + message.length > maxSizeInBytes) {
            // TODO: Change it to custom exception.
            throw new RuntimeException();
        } else {
            // System.out.println("file size of h2 data store is " +
            // fileStore.size());
        }

        MVMap<String, byte[]> map = store.openMap("traps");
        String uuid = UUID.randomUUID().toString();
        map.put(uuid, message);
        keys.add(uuid);
    }

    public byte[] readNextMessage() throws InterruptedException {
        String uuid = keys.remove();
        MVMap<String, byte[]> map = store.openMap("traps");
        byte[] message = map.get(uuid);
        map.remove(uuid);
        return message;
    }

    public void destroy() {
        System.out.println("file size of h2 data store is " + store.getFileStore().size());
        store.getFileStore().close();
        store.close();
    }

    public long getSize() {
        store.commit();
        return store.getFileStore().size();

    }

}
