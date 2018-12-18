/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.model;

import java.util.Optional;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ReadOnlyEntity;

@ReadOnlyEntity
public class LldpElementTopologyEntity {
    private final Integer id;
    private final String lldpChassisId;
    private final Integer nodeId;

    public LldpElementTopologyEntity(Integer id, String lldpChassisId, Integer nodeId) {
        this.id = id;
        this.lldpChassisId = lldpChassisId;
        this.nodeId = nodeId;
    }

    public static LldpElementTopologyEntity create(LldpElement element){
        return new LldpElementTopologyEntity(
                element.getId(),
                element.getLldpChassisId(),
                Optional.ofNullable(element.getNode()).map(OnmsNode::getId).orElse(null));
    }

    public Integer getId() {
        return id;
    }

    public String getLldpChassisId() {
        return lldpChassisId;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public String getNodeIdAsString() {
        if (getNodeId() != null) {
            return getNodeId().toString();
        }
        return null;
    }
}
