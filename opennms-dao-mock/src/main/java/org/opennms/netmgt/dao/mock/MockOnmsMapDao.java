/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.OnmsMapDao;
import org.opennms.netmgt.model.OnmsMap;

public class MockOnmsMapDao extends AbstractMockDao<OnmsMap, Integer> implements OnmsMapDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsMap map) {
        map.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsMap map) {
        return map.getId();
    }

    @Override
    public Collection<OnmsMap> findAll(final Integer offset, final Integer limit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsLike(final String mapLabel) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsByName(final String mapLabel) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsMap findMapById(final int id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsByNameAndType(final String mapName, final String mapType) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsByType(final String mapType) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findAutoMaps() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findUserMaps() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findSaveMaps() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findAutoAndSaveMaps() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsByOwner(final String owner) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findMapsByGroup(final String group) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMap> findVisibleMapsByGroup(final String group) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int updateAllAutomatedMap(final Date time) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
