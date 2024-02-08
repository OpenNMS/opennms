/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import com.vaadin.v7.ui.Table;
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
