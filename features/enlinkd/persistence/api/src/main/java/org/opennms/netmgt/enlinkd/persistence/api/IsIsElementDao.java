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

package org.opennms.netmgt.enlinkd.persistence.api;

import java.util.List;

import org.opennms.netmgt.enlinkd.model.IsIsElement;


/**
 * <p>IsIsElementDao interface.</p>
 */
public interface IsIsElementDao extends ElementDao<IsIsElement, Integer> {

    IsIsElement findByIsIsSysId(String isisSysId);

    /**
     * Returns all IsIsElements that have an isisSysID that matches an isisISAdjNeighSysID of an IsIsLink related to the given
     * node. Used to retrieve all IsIsElements that need to be accessed when finding IsIs links of a node.
     */
    List<IsIsElement> findBySysIdOfIsIsLinksOfNode(int nodeId);

}
