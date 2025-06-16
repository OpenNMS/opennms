/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
