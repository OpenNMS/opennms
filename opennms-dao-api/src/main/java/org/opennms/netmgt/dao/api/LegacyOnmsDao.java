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
import org.opennms.netmgt.model.OnmsCriteria;

/**
 * @deprecated DAO interfaces that inherit from this should be refactored to no longer
 * use the {@link OnmsCriteria} functions.
 * 
 * @param <T> The type of the Entity this DAO is intended to manage.
 * @param <K> The key of the Entity.
 */
public interface LegacyOnmsDao<T, K extends Serializable> extends OnmsDao<T,K> {
    
    /**
     * @deprecated use {@link #findMatching(Criteria)} instead.
     */
    List<T> findMatching(OnmsCriteria criteria);

    /**
     * @deprecated use {@link #countMatching(Criteria)} instead.
     */
    long countMatching(final OnmsCriteria onmsCrit);
}
