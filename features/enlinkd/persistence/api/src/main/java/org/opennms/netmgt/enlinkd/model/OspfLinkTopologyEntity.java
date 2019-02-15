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

import java.net.InetAddress;

import org.opennms.netmgt.model.ReadOnlyEntity;

import com.google.common.base.MoreObjects;

@ReadOnlyEntity
public class OspfLinkTopologyEntity {
    private final Integer id;
    private final Integer nodeId;
    private final InetAddress ospfIpAddr;
    private final InetAddress ospfIpMask;
    private final InetAddress ospfRemIpAddr;
    private final Integer ospfIfIndex;

    public OspfLinkTopologyEntity(Integer id, Integer nodeId, InetAddress ospfIpAddr, InetAddress ospfIpMask, InetAddress ospfRemIpAddr, Integer ospfIfIndex) {
        this.id = id;
        this.nodeId = nodeId;
        this.ospfIpAddr = ospfIpAddr;
        this.ospfIpMask = ospfIpMask;
        this.ospfRemIpAddr = ospfRemIpAddr;
        this.ospfIfIndex = ospfIfIndex;
    }

    public static OspfLinkTopologyEntity create (OspfLink link) {
        return new OspfLinkTopologyEntity(link.getId()
                , link.getNode().getId()
                , link.getOspfIpAddr()
                , link.getOspfIpMask()
                , link.getOspfRemIpAddr()
                , link.getOspfIfIndex());
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

    public InetAddress getOspfIpAddr() {
        return ospfIpAddr;
    }

    public InetAddress getOspfRemIpAddr() {
        return ospfRemIpAddr;
    }

    public Integer getOspfIfIndex() {
        return ospfIfIndex;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("nodeId", nodeId)
                .add("ospfIpAddr", ospfIpAddr)
                .add("ospfRemIpAddr", ospfRemIpAddr)
                .add("ospfIfIndex", ospfIfIndex)
                .toString();
    }

    public InetAddress getOspfIpMask() {
        return ospfIpMask;
    }
}
