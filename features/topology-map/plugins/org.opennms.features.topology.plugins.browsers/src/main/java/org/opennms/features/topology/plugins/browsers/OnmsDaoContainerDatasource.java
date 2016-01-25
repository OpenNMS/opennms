/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.browsers;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.OnmsDao;

public class OnmsDaoContainerDatasource<T, K extends Serializable> implements OnmsContainerDatasource<T, K> {

    private final OnmsDao<T, K> dao;

    public OnmsDaoContainerDatasource(OnmsDao<T, K> dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    @Override
    public void clear() {
        dao.clear();
    }

    @Override
    public void delete(K itemId) {
        dao.delete(itemId);
    }

    @Override
    public List<T> findMatching(Criteria criteria) {
        return dao.findMatching(criteria);
    }

    @Override
    public int countMatching(Criteria criteria) {
        return dao.countMatching(criteria);
    }

    @Override
    public T createInstance(Class<T> itemClass) throws IllegalAccessException, InstantiationException {
        return itemClass.newInstance();
    }
}
