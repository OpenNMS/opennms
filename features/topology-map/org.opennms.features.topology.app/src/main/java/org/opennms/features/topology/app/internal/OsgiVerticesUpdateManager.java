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
package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.TopologyServiceClient;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.osgi.OnmsServiceManager;
import org.opennms.osgi.VaadinApplicationContext;

/**
 * A OSGI-variant of the {@link VerticesUpdateManager}
 */
public class OsgiVerticesUpdateManager implements VerticesUpdateManager {

    // the session scope.
    private final VaadinApplicationContext m_applicationContext;
    private final OnmsServiceManager m_serviceManager;

    public OsgiVerticesUpdateManager(OnmsServiceManager serviceManager, VaadinApplicationContext applicationContext) {
        m_applicationContext = applicationContext;
        m_serviceManager = serviceManager;
    }

    /**
     * The selected VertexRefs. If a selection is made only the selected VertexRefs are in focus.
     * If no selection is made all visible (or displayable) VertexRefs are in focus.
     */
    private final Set<VertexRef> m_selectedVertices = Collections.synchronizedSet(new HashSet<VertexRef>());

    /**
     * The currently displayable VertexRefs.
     */
    private final Set<VertexRef> m_displayableVertexRefs = Collections.synchronizedSet(new HashSet<VertexRef>());

    /**
     * The currently selected VertexRefs.
     */
    private final Set<VertexRef> m_verticesInFocus = Collections.synchronizedSet(new HashSet<VertexRef>());

    @Override
    public void graphChanged(GraphContainer graphContainer){
        if(graphContainer == null) return;
        Set<VertexRef> newDisplayableVertexRefs = new HashSet<VertexRef>(graphContainer.getGraph().getDisplayVertices());
        if(!m_displayableVertexRefs.equals(newDisplayableVertexRefs)){
            synchronized (m_displayableVertexRefs){
                m_displayableVertexRefs.clear();
                m_displayableVertexRefs.addAll(newDisplayableVertexRefs);
            }
        }

        fireVertexRefsUpdated(getVerticesInFocus(), graphContainer.getTopologyServiceClient());

    }

    @Override
    public void selectionChanged(SelectionContext selectionContext) {
        if(selectionContext == null) return;
        Collection<VertexRef> selectedVertexRefs = selectionContext.getSelectedVertexRefs();
        if(!selectedVertexRefs.equals(m_selectedVertices)) {
            synchronized (m_selectedVertices) {
                m_selectedVertices.clear();
                m_selectedVertices.addAll(selectedVertexRefs);
            }
        }
        fireVertexRefsUpdated(getVerticesInFocus(), selectionContext.getGraphContainer().getTopologyServiceClient());

    }

    private Set<VertexRef> getVerticesInFocus() {
        if(m_selectedVertices.isEmpty()) return m_displayableVertexRefs;
        return m_selectedVertices;
    }

    private boolean hasChanged(Collection<VertexRef> newVertexRefs, Collection<VertexRef> verticesInFocus) {
        // if newVertexRefs and verticesInFocus are empty, we assume that they have changed
        // this is usually only the case when the UI is initialized, because
        // then both lists are empty, but we need them to be different.
        if (newVertexRefs.isEmpty() && verticesInFocus.isEmpty()) {
            return true;
        }
        // otherwise, we do a full equals-check
        return !newVertexRefs.equals(m_verticesInFocus);
    }

    /**
     * Notifies all listeners that the focus of the vertices has changed.
     * @param newVertexRefs
     */
    private synchronized void fireVertexRefsUpdated(Collection<VertexRef> newVertexRefs, TopologyServiceClient source) {
        if (!hasChanged(newVertexRefs, m_verticesInFocus)) {
            return;
        }
        m_verticesInFocus.clear();
        m_verticesInFocus.addAll(newVertexRefs);
        final boolean displayedSelected = m_displayableVertexRefs.size() == m_verticesInFocus.size();
        final VerticesUpdateEvent updateEvent = new VerticesUpdateEvent(Collections.unmodifiableSet(m_verticesInFocus), source, displayedSelected);
        m_applicationContext.getEventProxy(m_serviceManager).fireEvent(updateEvent);
    }
}
