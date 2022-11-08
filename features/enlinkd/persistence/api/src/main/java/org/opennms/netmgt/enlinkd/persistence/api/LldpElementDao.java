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

import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.netmgt.enlinkd.model.LldpElement;

/**
 * <p>LldpElementDao interface.</p>
 */
public interface LldpElementDao extends ElementDao<LldpElement, Integer> {

    List<LldpElement> findByChassisId(String chassisId, LldpChassisIdSubType type);

    /**
     * Returns all LldpElements that have a chassisId/chassisIdSubType that match the corresponding fields of a
     * LldpElement that is related to the given node. Used to retrieve all LldpElements that need to be accessed when
     * finding lldp links of a node.
     */
    List<LldpElement> findByChassisOfLldpLinksOfNode(int nodeId);

    LldpElement findBySysname(String sysname);

}
