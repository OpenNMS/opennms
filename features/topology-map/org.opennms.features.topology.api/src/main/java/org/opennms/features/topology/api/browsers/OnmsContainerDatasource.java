/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.browsers;

import java.io.Serializable;
import java.util.List;

import org.opennms.core.criteria.Criteria;

/**
 * Abstraction to allow the {@link OnmsVaadinContainer} to use different kinds of data sources, not only DAOs.
 *
 * @param <T> The entity type (e.g. OnmsAlarm).
 * @param <K> The key type of the entity (e.g. Integer)
 */
public interface OnmsContainerDatasource<T, K extends Serializable> {
    void clear();

    void delete(K itemId);

    List<T> findMatching(Criteria criteria);

    int countMatching(Criteria criteria);

    T createInstance(Class<T> itemClass) throws IllegalAccessException, InstantiationException;
}
