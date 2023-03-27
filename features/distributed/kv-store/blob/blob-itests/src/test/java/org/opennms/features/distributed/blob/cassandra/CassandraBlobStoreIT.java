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
