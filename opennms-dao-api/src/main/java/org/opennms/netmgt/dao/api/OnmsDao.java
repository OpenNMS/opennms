/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.io.Serializable;
import java.util.List;

import org.opennms.core.criteria.Criteria;

/**
 * OnmsDao interface.
 * @param <T> The type of the Entity this DAO is intended to manage.
 * @param <K> The key of the Entity.
 */
public interface OnmsDao<T, K extends Serializable> {
    
    /**
     * This is used to lock the table in order to implement upsert type operations
     */
    void lock();

    void initialize(Object obj);

    void flush();

    void clear();

    int countAll();

    void delete(T entity);

    void delete(K key);

    List<T> findAll();
    
    List<T> findMatching(Criteria criteria);

    int countMatching(final Criteria onmsCrit);

    T get(K id);

    T load(K id);

    K save(T entity);

    void saveOrUpdate(T entity);

    void update(T entity);

}
