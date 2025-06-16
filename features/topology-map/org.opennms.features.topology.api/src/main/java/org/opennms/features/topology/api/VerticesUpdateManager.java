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
package org.opennms.features.topology.api;

import java.util.Set;

import org.opennms.features.topology.api.topo.VertexRef;

/**
 * A VerticesUpdateManager is responsible to publish all nodeIds
 * which are currently in focus (e.g. selected or displayable).
 */
public interface VerticesUpdateManager extends SelectionListener, GraphContainer.ChangeListener {

    /**
     * If you want to listen to {@link VerticesUpdateEvent}s, implement this interface.
     */
    interface VerticesUpdateListener {
        void verticesUpdated(VerticesUpdateEvent event);
    }

    class VerticesUpdateEvent {

        private final Set<VertexRef> m_vertexRefs;
        private final boolean m_displayingAllVertices;
        private final TopologyServiceClient m_source;

        public VerticesUpdateEvent(Set<VertexRef> vertexRefs, TopologyServiceClient source) {
            this(vertexRefs, source, false);
        }

        /**
         * @param vertexRefs            The vertices currently selected.
         * @param source                The source of the event.
         * @param displayingAllVertices If all vertices are selected this should be true.
         */
        public VerticesUpdateEvent(Set<VertexRef> vertexRefs, TopologyServiceClient source, boolean displayingAllVertices){
            m_vertexRefs = vertexRefs;
            m_displayingAllVertices = displayingAllVertices;
            m_source = source;
        }

        public Set<VertexRef> getVertexRefs() {
            return m_vertexRefs;
        }

        public boolean allVerticesSelected(){
            return m_displayingAllVertices;
        }

        public TopologyServiceClient getSource() {
            return m_source;
        }

        @Override
        public String toString() {
            return "VerticesUpdateEvent@" + this.hashCode() + " [displayAll=" + m_displayingAllVertices + ", refs=" + m_vertexRefs + ", source=" + m_source + "]";
        }
    }
}
