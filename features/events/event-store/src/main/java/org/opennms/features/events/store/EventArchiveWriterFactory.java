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
package org.opennms.features.events.store;

import org.postgresql.ds.PGSimpleDataSource;

/**
 * Blueprint factory for creating {@link EventArchiveWriter} instances.
 *
 * <p>Takes only String/int args to avoid Blueprint type-matching issues.
 * Creates a {@link PGSimpleDataSource} directly (no connection pool needed
 * for event archiving, avoids OSGi classloading issues with DriverManager).</p>
 */
public class EventArchiveWriterFactory {

    private EventArchiveWriterFactory() {
        // static factory — prevent instantiation
    }

    /**
     * Creates an {@link EventArchiveWriter} with a direct PostgreSQL DataSource.
     *
     * @param host     PostgreSQL host
     * @param port     PostgreSQL port
     * @param database database name
     * @param username database username
     * @param password database password
     * @return configured EventArchiveWriter
     */
    public static EventArchiveWriter create(String host, int port, String database,
                                             String username, String password) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{host});
        dataSource.setPortNumbers(new int[]{port});
        dataSource.setDatabaseName(database);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        return new EventArchiveWriter(dataSource);
    }
}
