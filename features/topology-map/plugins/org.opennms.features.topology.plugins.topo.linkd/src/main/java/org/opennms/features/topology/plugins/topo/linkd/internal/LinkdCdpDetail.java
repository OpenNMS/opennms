/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.model.CdpLink;

public class LinkdCdpDetail extends LinkdEdgeDetail<CdpLink,CdpLink> {

    public LinkdCdpDetail(String id, Vertex source, CdpLink sourceLink, Vertex target, CdpLink targetLink) {
        super(id, source, sourceLink, target, targetLink);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSourceLink() == null) ? 0 : getSource().getNodeID().hashCode()) + ((getTargetLink() == null) ? 0 : getTarget().getNodeID().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof LinkdCdpDetail){
            LinkdCdpDetail objDetail = (LinkdCdpDetail)obj;

            return getId().equals(objDetail.getId());
        } else  {
            return false;
        }
    }

    @Override
    public Integer getSourceIfIndex() {
        return getSourceLink().getCdpCacheIfIndex();
    }

    @Override
    public Integer getTargetIfIndex() {
        return getTargetLink().getCdpCacheIfIndex();
    }

    @Override
    public String getType() { return "CDP"; }

}