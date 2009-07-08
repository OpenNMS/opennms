/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao;

import org.opennms.netmgt.model.OnmsMap;

import java.util.Collection;

public interface OnmsMapDao extends OnmsDao<OnmsMap, Integer> {
    public abstract Collection<OnmsMap> findAll(Integer offset, Integer limit);
    public abstract Collection<OnmsMap> findMapsLike(String mapLabel);
    public abstract Collection<OnmsMap> findMapsByName(String mapLabel);
    public abstract OnmsMap findMapById(int id);
    public abstract Collection<OnmsMap> findMapsByNameAndType(String mapName, String mapType);
    public abstract Collection<OnmsMap> findMapsByType(String mapType);
    public abstract Collection<OnmsMap> findAutoMaps();
    public abstract Collection<OnmsMap> findUserMaps();
    public abstract Collection<OnmsMap> findMapsByOwner(String owner);
    public abstract Collection<OnmsMap> findMapsByGroup(String group);
    public abstract Collection<OnmsMap> findVisibleMapsByGroup(String group);
}