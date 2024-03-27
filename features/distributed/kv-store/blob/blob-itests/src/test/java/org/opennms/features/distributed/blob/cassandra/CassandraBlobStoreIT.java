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
package org.opennms.features.distributed.blob.cassandra;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.opennms.features.distributed.blob.BaseBlobStoreIT;
import org.opennms.features.distributed.cassandra.api.CassandraSession;
import org.opennms.features.distributed.kvstore.api.SerializingBlobStore;
import org.opennms.features.distributed.kvstore.blob.cassandra.CassandraBlobStore;
import org.opennms.newts.cassandra.NewtsInstance;
import org.opennms.newts.cassandra.SchemaManager;

import org.opennms.features.distributed.cassandra.impl.NewtsCassandraSessionFactory;

public class CassandraBlobStoreIT extends BaseBlobStoreIT {

    @ClassRule
    public static NewtsInstance newtsInstance = new NewtsInstance();

    private CassandraSession cassandraSession;

    @BeforeClass
    public static void setUpClazz() {
        System.setProperty("datastax-java-driver.basic.request.timeout", "30 seconds");
    }

    @Override
    public void init() throws IOException {
        cassandraSession = NewtsCassandraSessionFactory.of(newtsInstance.getCassandraSession());
        cassandraSession.execute(String.format("DROP TABLE IF EXISTS %s.kvstore_blob;", newtsInstance.getKeyspace()));

        InetSocketAddress cassandraAddress = InetSocketAddress.createUnresolved(newtsInstance.getHost(), newtsInstance.getPort());
        blobStore = new CassandraBlobStore(() -> {
            cassandraSession.execute(String.format("USE %s;", newtsInstance.getKeyspace()));
            return cassandraSession;
        }, () -> schema -> {
            try (SchemaManager sm = new SchemaManager(newtsInstance.getDatacenter(), newtsInstance.getKeyspace(), cassandraAddress.getHostName(),
                    cassandraAddress.getPort(), "cassandra", "cassandra", false, null)) {
                sm.create(schema::getInputStream);
            }
        });

        serializingBlobStore = new SerializingBlobStore<>(blobStore, String::getBytes, String::new);
    }

    @Override
    public void destroy() throws ExecutionException, InterruptedException, TimeoutException {
        cassandraSession.shutdown().get(1, TimeUnit.MINUTES);
    }
}
