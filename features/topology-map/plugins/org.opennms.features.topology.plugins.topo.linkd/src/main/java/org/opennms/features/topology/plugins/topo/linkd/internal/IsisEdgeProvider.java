/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

import org.opennms.features.topology.api.topo.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class IsisEdgeProvider implements EdgeProvider{
    @Override
    public String getEdgeNamespace() {
        return AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD + "::IsIs";
    }

    @Override
    public boolean contributesTo(String namespace) {
        return namespace.equals(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD);
    }

    @Override
    public Edge getEdge(String namespace, String id) {
        return null;
    }

    @Override
    public Edge getEdge(EdgeRef reference) {
        return null;
    }

    @Override
    public List<Edge> getEdges(Criteria... criteria) {
        for (Criteria crit : criteria) {
            if (crit instanceof LinkdHopCriteria) {
                String nodeId = ((LinkdHopCriteria) crit).getId();
                //TODO: find all the links for this
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
        return Collections.emptyList();
    }

    @Override
    public void addEdgeListener(EdgeListener listener) {

    }

    @Override
    public void removeEdgeListener(EdgeListener listener) {

    }

    @Override
    public void clearEdges() {

    }
}
