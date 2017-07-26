/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.Criteria;

public interface GenericPersistenceAccessor {
    <T> List<T> find(String query);

    <T> List<T> find(String query, Object... values);

    <T> List<T> findUsingNamedParameters(String query, String[] paramNames, Object[] values);

    <T> List<T> findUsingNamedParameters(final String query, String[] paramNames, Object[] values, Integer offset, Integer limit);

    <T> T get(Class<T> entityType, int entityId);

    List findMatching(Criteria criteria);

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
