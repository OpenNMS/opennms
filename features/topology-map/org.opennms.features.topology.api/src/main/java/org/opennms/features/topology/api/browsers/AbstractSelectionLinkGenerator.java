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

package org.opennms.features.topology.api.browsers;

import java.util.Collection;
import java.util.Set;

import org.opennms.features.topology.api.TopologyServiceClient;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.api.WidgetContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.osgi.EventProxy;
import org.opennms.osgi.EventProxyAware;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

public abstract class AbstractSelectionLinkGenerator implements Table.ColumnGenerator, EventProxyAware {

    private EventProxy m_eventProxy;

    protected void fireVertexUpdatedEvent(Collection<VertexRef> vertexRefs) {
        Set<VertexRef> vertexRefSet = Sets.newHashSet(vertexRefs);
        getEventProxy().fireEvent(new VerticesUpdateManager.VerticesUpdateEvent(vertexRefSet, getGraphProvider()));
    }

    protected void fireVertexUpdatedEvent(VertexRef vertexRef) {
        fireVertexUpdatedEvent(Lists.newArrayList(vertexRef));
    }

    private TopologyServiceClient getGraphProvider() {
        UI ui = UI.getCurrent();
        if (ui instanceof WidgetContext) {
            return ((WidgetContext) ui).getGraphContainer().getTopologyServiceClient();
        }
        return null;
    }

    public void setEventProxy(EventProxy eventProxy) {
        this.m_eventProxy = eventProxy;
    }

    public EventProxy getEventProxy() {
        return m_eventProxy;
    }
}
