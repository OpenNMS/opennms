/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import com.google.common.base.MoreObjects;
import org.opennms.netmgt.model.ReadOnlyEntity;

import java.net.InetAddress;

@ReadOnlyEntity
public class OspfAreaTopologyEntity {

    private final Integer id;
    private final Integer nodeId;
    private final InetAddress ospfAreaId;
    private final Integer ospfAuthType;
    private final Integer ospfImportAsExtern;
    private final Integer ospfAreaBdrRtrCount;
    private final Integer ospfAsBdrRtrCount;
    private final Integer ospfAreaLsaCount;

    public OspfAreaTopologyEntity(Integer id, Integer nodeId, InetAddress ospfAreaId, Integer ospfAuthType, Integer ospfImportAsExtern, Integer ospfAreaBdrRtrCount, Integer ospfAsBdrRtrCount, Integer ospfAreaLsaCount) {
        this.id = id;
        this.nodeId = nodeId;
        this.ospfAreaId = ospfAreaId;
        this.ospfAuthType = ospfAuthType;
        this.ospfImportAsExtern = ospfImportAsExtern;
        this.ospfAreaBdrRtrCount = ospfAreaBdrRtrCount;
        this.ospfAsBdrRtrCount = ospfAsBdrRtrCount;
        this.ospfAreaLsaCount = ospfAreaLsaCount;
    }

    public static org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity create(OspfArea area) {
        return new org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity(area.getId()
                , area.getNode().getId()
                , area.getOspfAreaId()
                , area.getOspfAuthType()
                , area.getOspfImportAsExtern()
                , area.getOspfAreaBdrRtrCount()
                , area.getOspfAsBdrRtrCount()
                , area.getOspfAreaLsaCount());
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

    public InetAddress getOspfAreaId() {
        return ospfAreaId;
    }

    public Integer getOspfAuthType() {
        return ospfAuthType;
    }

    public Integer getOspfImportAsExtern() {
        return ospfImportAsExtern;
    }

    public Integer getOspfAreaBdrRtrCount() {
        return ospfAreaBdrRtrCount;
    }

    public Integer getOspfAsBdrRtrCount() {
        return ospfAsBdrRtrCount;
    }

    public Integer getOspfAreaLsaCount() {
        return ospfAreaLsaCount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("nodeId", nodeId)
                .add("ospfAreaId", ospfAreaId)
                .add("ospfAuthType", ospfAuthType)
                .add("ospfImportAsExtern", ospfImportAsExtern)
                .add("ospfAreaBdrRtrCount", ospfAreaBdrRtrCount)
                .add("ospfAsBdrRtrCount", ospfAsBdrRtrCount)
                .add("ospfAreaLsaCount", ospfAreaLsaCount)
                .toString();
    }
}


