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

package org.opennms.features.topology.plugins.topo.linkd.internal.operations;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LinkStatusToggleOperation extends AbstractCheckedOperation {

    private EdgeStatusProvider m_llpdStatusProvider;
    private EdgeStatusProvider m_ospfLinkStatusProvider;
    private EdgeStatusProvider m_isisLinkStatusProvider;
    private EdgeStatusProvider m_bridgeLinkStatusProvider;
    private EdgeStatusProvider m_cdpLinkStatusProvider;
    private List<EdgeStatusProvider> m_providers;
    //TODO: add functionality to check bundle context when bundle is deregistered
    //private BundleContext m_bundleContext;

    public void init() {
        m_providers = new ArrayList<EdgeStatusProvider>();
        m_providers.add(m_llpdStatusProvider);
        m_providers.add(m_ospfLinkStatusProvider);
        m_providers.add(m_isisLinkStatusProvider);
        m_providers.add(m_bridgeLinkStatusProvider);
        m_providers.add(m_cdpLinkStatusProvider);
    }

    @Override
    protected boolean isChecked(GraphContainer container) {
        Set<EdgeStatusProvider> edgeStatusProviders = container.getEdgeStatusProviders();

        return edgeStatusProviders.containsAll(m_providers);

    }

    @Override
    public void applyHistory(GraphContainer container, Map<String, String> settings) {
        Set<EdgeStatusProvider> edgeStatusProviders = container.getEdgeStatusProviders();

        String historyValue = settings.get(getClass().getName());
        // an history value is set, decide what to do
        boolean statusEnabled = Boolean.TRUE.toString().equals(historyValue);
        if (statusEnabled) {
            if(!edgeStatusProviders.containsAll(m_providers)) {
                edgeStatusProviders.addAll(m_providers);
            }

        } else {
            if(edgeStatusProviders.containsAll(m_providers)) {
                edgeStatusProviders.removeAll(m_providers);
            }
        }
    }

    @Override
    public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
        toggle(operationContext.getGraphContainer());
        return new Undoer() {
            @Override
            public void undo(OperationContext operationContext) {
                toggle(operationContext.getGraphContainer());
            }
        };
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    private void toggle(GraphContainer graphContainer) {
        Set<EdgeStatusProvider> edgeStatusProviders = graphContainer.getEdgeStatusProviders();

        if(edgeStatusProviders.containsAll(m_providers)) {
            edgeStatusProviders.removeAll(m_providers);
        } else {
            edgeStatusProviders.addAll(m_providers);
        }

        graphContainer.redoLayout();
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
