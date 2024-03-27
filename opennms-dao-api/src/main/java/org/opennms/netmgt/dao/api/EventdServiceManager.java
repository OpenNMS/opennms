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
package org.opennms.netmgt.dao.api;

import org.springframework.dao.DataAccessException;

/**
 * <p>EventdServiceManager interface.</p>
 *
 * @deprecated This is only used when using {@link JdbcEventWriter}
 * so when we remove the JDBC implementation, we can get rid of this
 * class.
 * 
 * @author ranger
 * @version $Id: $
 */
public interface EventdServiceManager {
    /**
     * Lookup the service ID for a specific service by name.
     *
     * @return service ID for the given service name or -1 if not found
     * @exception DataAccessException if there is an error accessing the database
     * @param serviceName a {@link java.lang.String} object.
     * @throws org.springframework.dao.DataAccessException if any.
     * 
     * @deprecated This is only used when using {@link JdbcEventWriter}
     * so when we remove the JDBC implementation, we can get rid of this
     * class.
     */
    public abstract int getServiceId(String serviceName) throws DataAccessException;

    /**
     * Synchronize the in-memory cache with the service table in the database.
     * 
     * @deprecated This is only used when using {@link JdbcEventWriter}
     * so when we remove the JDBC implementation, we can get rid of this
     * class.
     */
    public abstract void dataSourceSync();
}
