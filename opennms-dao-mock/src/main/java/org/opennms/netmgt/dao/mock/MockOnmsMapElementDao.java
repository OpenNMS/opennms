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
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.OnmsMapElementDao;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;

public class MockOnmsMapElementDao extends AbstractMockDao<OnmsMapElement, Integer> implements OnmsMapElementDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsMapElement me) {
        me.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsMapElement mapElement) {
        return mapElement.getId();
    }

    @Override
    public Collection<OnmsMapElement> findAll(final Integer offset, final Integer limit) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsMapElement findElementById(final int id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsMapElement findElement(final int elementId, final String type, final OnmsMap map) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMapElement> findElementsByMapId(final OnmsMap map) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMapElement> findElementsByNodeId(final int nodeId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMapElement> findElementsByElementIdAndType(final int elementId, final String type) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMapElement> findElementsByMapIdAndType(final int mapId, final String type) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMapElement> findElementsByType(final String type) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void deleteElementsByMapId(final OnmsMap map) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMapElement> findMapElementsOnMap(final int mapId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsMapElement> findNodeElementsOnMap(final int mapId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void deleteElementsByNodeid(final int nodeId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void deleteElementsByType(final String type) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void deleteElementsByElementIdAndType(final int elementId, final String type) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void deleteElementsByMapType(final String mapType) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countElementsOnMap(final int mapid) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
