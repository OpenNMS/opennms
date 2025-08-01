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

import java.util.Collection;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.Criteria;

public interface GenericPersistenceAccessor {
    <T> List<T> find(String query);

    <T> List<T> find(String query, Object... values);

    <T> List<T> findUsingNamedParameters(String query, String[] paramNames, Object[] values);

    <T> List<T> findUsingNamedParameters(final String query, String[] paramNames, Object[] values, Integer offset, Integer limit);

    <T> T get(Class<T> entityType, Serializable entityId);

    <T> List<T> findAll(Class<T> entityClass);

    List findMatching(Criteria criteria);

    <T> T save(T entity);

    <T> void update(T entity);

    <T> void saveAll(Collection<T> entities);

    <T> void deleteAll(Class<T> clazz);

    <T> void deleteAll(Collection<T> entities);

    <T> void delete(T entity);

    /**
     * Executes a native SQL query.
     * Use with care.
     *
     * @param sql The SQL query to execute
     * @param parameterMap An optional parameters map to apply to the query.
     * @param <T>
     * @return The result.
     */
    <T> List<T> executeNativeQuery(String sql, Map<String, Object> parameterMap);

}
