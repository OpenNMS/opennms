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

package org.opennms.features.vaadin.nodemaps.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

public class NodeMapQueryLink implements Operation {
    private static final Logger LOG = Logger.getLogger(NodeMapQueryLink.class.getName());
    private GeoAssetProvider m_geoAssetProvider;

    public void setGeoAssetProvider(final GeoAssetProvider provider) {
        m_geoAssetProvider = provider;
    }

    @Override
    public String getId() {
        return "NodeMapQueryLink";
    }

    @Override
    public void execute(final List<VertexRef> targets, final OperationContext operationContext) {
        final Collection<VertexRef> availableNodes = m_geoAssetProvider.getNodesWithCoordinates();

        final StringBuilder sb = new StringBuilder();
        sb.append(VaadinServlet.getCurrent().getServletContext().getContextPath());
        sb.append("/node-maps#search/nodeId%20in%20");

        final List<String> nodeIds = new ArrayList<>();
        for (final VertexRef ref : targets) {
            if (availableNodes.contains(ref)) {
                nodeIds.add(ref.getId());
            }
        }

        final Iterator<String> i = nodeIds.iterator();
        while (i.hasNext()) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(",");
            }
        }

        final String redirectUrl = sb.toString();
        LOG.info("redirecting to: " + redirectUrl);
        final UI ui = operationContext.getMainWindow();
        ui.getPage().getJavaScript().execute("window.location = '" + redirectUrl + "';");
    }

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        return (targets != null && targets.size() > 0) && hasCoordinates(targets);
    }

    @Override
    public boolean enabled(final List<VertexRef> targets, final OperationContext operationContext) {
        return hasCoordinates(targets);
    }

    private boolean hasCoordinates(List<VertexRef> targets) {
        final Collection<VertexRef> availableNodes = m_geoAssetProvider.getNodesWithCoordinates();

        for (final VertexRef ref : targets) {
            if (availableNodes.contains(ref)) {
                return true;
            }
        }
        return false;
    }

}
