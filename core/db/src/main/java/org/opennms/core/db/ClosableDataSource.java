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
package org.opennms.core.db;

import java.io.Closeable;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * <p>ClosableDataSource interface.</p>
 */
public interface ClosableDataSource extends DataSource, Closeable {
    
    /**
     * Close the datasource, if necessary.
     */
    @Override
    void close();

    /**
     * How long, in seconds, an idle connection is kept in the pool before it is removed.
     * @param idleTimeout
     */
    void setIdleTimeout(final int idleTimeout);

    /**
     * How long, in seconds, to attempt to make a connection to the database.
     */
    @Override
    void setLoginTimeout(final int loginTimeout) throws SQLException;

    /**
     * The minimum number of pooled connections to retain.
     * @param minPool
     */
    void setMinPool(final int minPool);
    
    /**
     * The maximum number of pooled connections to retain.
     * @param maxPool
     */
    void setMaxPool(final int maxPool);
    
    /**
     * The maximum number of connections that can be created.
     * @param maxSize
     */
    void setMaxSize(final int maxSize);
}
