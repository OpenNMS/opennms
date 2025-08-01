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
package org.opennms.features.activemq;

import javax.jms.Connection;

import org.apache.activemq.jms.pool.ConnectionPool;
import org.opennms.core.logging.Logging;

/**
 * Used to create connection pools using our own ConnectionPool subclass.
 * See {@link org.opennms.features.activemq.ConnectionPool} for details.
 *
 * @author jwhite
 */
public class PooledConnectionFactory extends org.apache.activemq.pool.PooledConnectionFactory {

    @Override
    protected ConnectionPool createConnectionPool(Connection connection) {
        // The threads that request and use connections from this pool
        // are related to the IPC modules, so we set the prefix accordingly
        Logging.putPrefix("ipc");
        return super.createConnectionPool(connection);
    }

}
