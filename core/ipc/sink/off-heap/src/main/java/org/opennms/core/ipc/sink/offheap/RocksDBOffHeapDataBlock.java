/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.Objects;
import java.util.function.Function;

public class RocksDBOffHeapDataBlock<T> extends OffHeapDataBlock<T> {

    private RocksDB rocksdb;

    public RocksDBOffHeapDataBlock(String name, int queueSize, Function<T, byte[]> serializer, Function<byte[], T> deserializer, RocksDB rocksdb, byte[] data) {
        super(name, queueSize, serializer, deserializer, data);
        this.rocksdb = Objects.requireNonNull(rocksdb);
    }

    public RocksDBOffHeapDataBlock(int queueSize, Function<T, byte[]> serializer, Function<byte[], T> deserializer, RocksDB rocksdb) {
        this(null, queueSize, serializer, deserializer, rocksdb, null);
    }

    void writeData(String key, byte[] data) {
        try {
            rocksdb.put(key.getBytes(), data);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    byte[] loadData(String key) {
        try {
            var data = rocksdb.get(key.getBytes());
            rocksdb.delete(key.getBytes());
            return data;
        } catch (RocksDBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
