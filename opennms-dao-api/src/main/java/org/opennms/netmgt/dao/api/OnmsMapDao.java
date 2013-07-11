/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.api;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.OnmsMap;

/**
 * <p>OnmsMapDao interface.</p>
 */
public interface OnmsMapDao extends OnmsDao<OnmsMap, Integer> {
    /**
     * <p>findAll</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     * @param limit a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findAll(Integer offset, Integer limit);
    /**
     * <p>findMapsLike</p>
     *
     * @param mapLabel a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findMapsLike(String mapLabel);
    /**
     * <p>findMapsByName</p>
     *
     * @param mapLabel a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findMapsByName(String mapLabel);
    /**
     * <p>findMapById</p>
     *
     * @param id a int.
     * @return a {@link org.opennms.netmgt.model.OnmsMap} object.
     */
    OnmsMap findMapById(int id);
    /**
     * <p>findMapsByNameAndType</p>
     *
     * @param mapName a {@link java.lang.String} object.
     * @param mapType a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findMapsByNameAndType(String mapName, String mapType);
    /**
     * <p>findMapsByType</p>
     *
     * @param mapType a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findMapsByType(String mapType);
    /**
     * <p>findAutoMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findAutoMaps();
    /**
     * <p>findUserMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findUserMaps();
    /**
     * <p>findSaveMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findSaveMaps();
    /**
     * <p>findAutoAndSaveMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findAutoAndSaveMaps();
    /**
     * <p>findMapsByOwner</p>
     *
     * @param owner a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findMapsByOwner(String owner);
    /**
     * <p>findMapsByGroup</p>
     *
     * @param group a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findMapsByGroup(String group);
    /**
     * <p>findVisibleMapsByGroup</p>
     *
     * @param group a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsMap> findVisibleMapsByGroup(String group);
    /**
     * <p>updateAllAutomatedMap</p>
     *
     * @param time a {@link java.util.Date} object.
     * @return a int.
     */
    int updateAllAutomatedMap(Date time);
}
