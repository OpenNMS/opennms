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

import org.opennms.netmgt.model.ReadOnlyEntity;

import com.google.common.base.MoreObjects;

@ReadOnlyEntity
public class IsIsLinkTopologyEntity {
    private final Integer id;
    private final Integer nodeId;
    private final Integer isisISAdjIndex;
    private final Integer isisCircIfIndex;
    private final String isisISAdjNeighSysID;
    private final String isisISAdjNeighSNPAAddress;


    public IsIsLinkTopologyEntity(Integer id, Integer nodeId, Integer isisISAdjIndex, Integer isisCircIfIndex, String isisISAdjNeighSysID,
                                  String isisISAdjNeighSNPAAddress){
        this.id = id;
        this.nodeId = nodeId;
        this.isisISAdjIndex = isisISAdjIndex;
        this.isisCircIfIndex = isisCircIfIndex;
        this.isisISAdjNeighSysID = isisISAdjNeighSysID;
        this.isisISAdjNeighSNPAAddress = isisISAdjNeighSNPAAddress;
    }

    public Integer getId() {
        return id;
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

    public Integer getIsisISAdjIndex() {
        return isisISAdjIndex;
    }

    public Integer getIsisCircIfIndex() {
        return isisCircIfIndex;
    }

    public String getIsisISAdjNeighSysID() {
        return isisISAdjNeighSysID;
    }

    public String getIsisISAdjNeighSNPAAddress() {
        return isisISAdjNeighSNPAAddress;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("nodeId", nodeId)
                .add("isisISAdjIndex", isisISAdjIndex)
                .add("isisCircIfIndex", isisCircIfIndex)
                .add("isisISAdjNeighSysID", isisISAdjNeighSysID)
                .add("isisISAdjNeighSNPAAddress", isisISAdjNeighSNPAAddress)
                .toString();
    }
}
