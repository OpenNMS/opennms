/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

import java.util.List;

import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.PrefabGraphType;

/**
 * <p>GraphDao interface.</p>
 */
public interface GraphDao {
    /**
     * <p>findByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PrefabGraphType} object.
     */
    public PrefabGraphType findPrefabGraphTypeByName(String name);

    /**
     * <p>findAdhocByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.AdhocGraphType} object.
     */
    public AdhocGraphType findAdhocGraphTypeByName(String name);
    
    /**
     * <p>getAllPrefabGraphs</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<PrefabGraph> getAllPrefabGraphs();

    /**
     * <p>getPrefabGraph</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PrefabGraph} object.
     */
    public PrefabGraph getPrefabGraph(String name);
    
    /**
     * <p>getPrefabGraphsForResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @return an array of {@link org.opennms.netmgt.model.PrefabGraph} objects.
     */
    public PrefabGraph[] getPrefabGraphsForResource(OnmsResource resource);
}
