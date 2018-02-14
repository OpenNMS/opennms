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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api;

import java.util.List;

import org.opennms.features.topology.api.topo.Vertex;

public interface IconManager {

    /**
     * Returns the first {@link IconRepository} which has the provided <code>iconKey</code> mapping defined.
     *
     * @param iconKey the <code>iconKey</code> to look up
     * @return the first {@link IconRepository} which has the provided <code>iconKey</code> mapping defined, or <code>null</code> if no {@link IconRepository} exists with the provided <code>iconKey</code>
     */
    IconRepository findRepositoryByIconKey(String iconKey);

    /**
     * Returns the list of available svg-files, e.g. 'theme://svg/file.svg'.
     *
     * @return the list of available svg-files, e.g. 'theme://svg/file.svg'
     */
    List<String> getSVGIconFiles();

    /**
     * Returns the icon id assigned to the provided <code>iconKey</code>.
     *
     * @param iconKey the <code>iconKey</code> to look up
     * @return the icon id assigned to the provided <code>iconKey</code>
     */
    String getSVGIconId(String iconKey);

    /**
     * Returns the icon id assigned to the provided {@link Vertex}.
     *
     * @param vertex the vertex to get the icon id for
     * @return the icon id assigned to the provided {@link Vertex}
     */
    String getSVGIconId(Vertex vertex);

    /**
     * Sets a new icon mapping from the {@link Vertex} to the <code>newIconId</code>.
     *
     * @param vertex the vertex to map
     * @param newIconId the icon id to map the vertex to
     * @return the icon key of the vertex if this {@link IconManager} was able to save the mapping, null otherwise
     */
    String setIconMapping(Vertex vertex, String newIconId);

    /**
     * Removes the icon mapping for the {@link Vertex} if defined.
     *
     * @param vertex the {@link Vertex} to remove the icon mapping for
     * @return <code>true</code> if the mapping was removed, <code>false</code> if no icon mapping was found for the provided {@link Vertex} and therefore could not be removed
     */
    boolean removeIconMapping(Vertex vertex);
}
