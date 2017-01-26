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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.Status;
import org.osgi.framework.ServiceReference;

public class LinkdWrappedEdgeStatusProviders implements EdgeStatusProvider {

    private EdgeStatusProvider m_llpdStatusProvider;
    private EdgeStatusProvider m_ospfLinkStatusProvider;
    private EdgeStatusProvider m_isisLinkStatusProvider;
    private EdgeStatusProvider m_bridgeLinkStatusProvider;
    private EdgeStatusProvider m_cdpLinkStatusProvider;
    private List<EdgeStatusProvider> m_providers;

    private Boolean m_enlinkdIsActive = false;

    public void init() {
        m_providers = new ArrayList<>();
        m_providers.add(m_llpdStatusProvider);
        m_providers.add(m_ospfLinkStatusProvider);
        m_providers.add(m_isisLinkStatusProvider);
        m_providers.add(m_bridgeLinkStatusProvider);
        m_providers.add(m_cdpLinkStatusProvider);
    }

    @Override
    public Map<EdgeRef, Status> getStatusForEdges(EdgeProvider edgeProvider, Collection<EdgeRef> edges, Criteria[] criteria) {
        final Map<EdgeRef,Status> edgeRefStatusMap = new HashMap<>();
        for (EdgeStatusProvider statusProvider : m_providers) {
            edgeRefStatusMap.putAll(statusProvider.getStatusForEdges(edgeProvider, edges, criteria));
        }
        return edgeRefStatusMap;
    }

    @Override
    public String getNamespace() {
        return AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return namespace != null &&  namespace.equals(getNamespace());
    }

    public void setEnlinkdService(ServiceReference<?> enlinkdService) {
        if(enlinkdService != null){
            m_enlinkdIsActive = true;
        }
    }

    public void setLlpdStatusProvider(EdgeStatusProvider llpdStatusProvider) {
        m_llpdStatusProvider = llpdStatusProvider;
    }

    public void setOspfLinkStatusProvider(EdgeStatusProvider ospfLinkStatusProvider) {
        m_ospfLinkStatusProvider = ospfLinkStatusProvider;
    }

    public void setIsisLinkStatusProvider(EdgeStatusProvider isisLinkStatusProvider) {
        m_isisLinkStatusProvider = isisLinkStatusProvider;
    }

    public void setBridgeLinkStatusProvider(EdgeStatusProvider bridgeLinkStatusProvider) {
        m_bridgeLinkStatusProvider = bridgeLinkStatusProvider;
    }

    public EdgeStatusProvider getCdpLinkStatusProvider() {
        return m_cdpLinkStatusProvider;
    }

    public void setCdpLinkStatusProvider(EdgeStatusProvider cdpLinkStatusProvider) {
        m_cdpLinkStatusProvider = cdpLinkStatusProvider;
    }

}
