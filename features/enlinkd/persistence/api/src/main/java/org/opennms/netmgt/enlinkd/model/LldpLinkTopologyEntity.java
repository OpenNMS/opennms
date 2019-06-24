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

import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.model.ReadOnlyEntity;

import com.google.common.base.MoreObjects;

@ReadOnlyEntity
public class LldpLinkTopologyEntity {

    private final Integer id;
    private final Integer nodeId;
    private final String lldpRemChassisId;
    private final String lldpRemPortId;
    private final LldpUtils.LldpPortIdSubType lldpRemPortIdSubType;
    private final String lldpPortId;
    private final LldpUtils.LldpPortIdSubType lldpPortIdSubType;
    private final String lldpPortDescr;
    private final Integer lldpPortIfindex;

    public LldpLinkTopologyEntity(Integer id, Integer nodeId, String lldpRemChassisId, String lldpRemPortId,
                                  LldpUtils.LldpPortIdSubType lldpRemPortIdSubType, String lldpPortId,
                                  LldpUtils.LldpPortIdSubType lldpPortIdSubType, String lldpPortDescr, Integer lldpPortIfindex) {
        this.id = id;
        this.nodeId = nodeId;
        this.lldpRemChassisId = lldpRemChassisId;
        this.lldpRemPortId = lldpRemPortId;
        this.lldpRemPortIdSubType = lldpRemPortIdSubType;
        this.lldpPortId = lldpPortId;
        this.lldpPortIdSubType = lldpPortIdSubType;
        this.lldpPortDescr = lldpPortDescr;
        this.lldpPortIfindex = lldpPortIfindex;
    }

    public static LldpLinkTopologyEntity create (LldpLink link) {
        return new LldpLinkTopologyEntity(
                link.getId()
                , link.getNode().getId()
                , link.getLldpRemChassisId()
                , link.getLldpRemPortId()
                , link.getLldpRemPortIdSubType()
                , link.getLldpPortId()
                , link.getLldpPortIdSubType()
                , link.getLldpPortDescr()
                , link.getLldpPortIfindex()
        );
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

    public String getLldpRemChassisId() {
        return lldpRemChassisId;
    }

    public String getLldpRemPortId() {
        return lldpRemPortId;
    }

    public LldpUtils.LldpPortIdSubType getLldpRemPortIdSubType() {
        return lldpRemPortIdSubType;
    }

    public String getLldpPortId() {
        return lldpPortId;
    }

    public LldpUtils.LldpPortIdSubType getLldpPortIdSubType() {
        return lldpPortIdSubType;
    }

    public String getLldpPortDescr() {
        return lldpPortDescr;
    }

    public Integer getLldpPortIfindex() {
        return lldpPortIfindex;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("nodeId", nodeId)
                .add("lldpRemChassisId", lldpRemChassisId)
                .add("lldpRemPortId", lldpRemPortId)
                .add("lldpRemPortIdSubType", lldpRemPortIdSubType)
                .add("lldpPortId", lldpPortId)
                .add("lldpPortIdSubType", lldpPortIdSubType)
                .add("lldpPortDescr", lldpPortDescr)
                .add("lldpPortIfindex", lldpPortIfindex)
                .toString();
    }
}
