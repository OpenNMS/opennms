//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao;

import java.util.List;

import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.PrefabGraphType;

/**
 * <p>GraphDao interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface GraphDao {
    /**
     * <p>findByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PrefabGraphType} object.
     */
    public PrefabGraphType findByName(String name);

    /**
     * <p>findAdhocByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.AdhocGraphType} object.
     */
    public AdhocGraphType findAdhocByName(String name);
    
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
