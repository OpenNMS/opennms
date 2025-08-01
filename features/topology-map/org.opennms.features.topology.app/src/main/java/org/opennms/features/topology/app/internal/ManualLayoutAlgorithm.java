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

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.app.internal.jung.GridLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.LayoutManager;
import org.opennms.netmgt.topology.persistence.api.LayoutEntity;
import org.opennms.netmgt.topology.persistence.api.PointEntity;

public class ManualLayoutAlgorithm implements LayoutAlgorithm {

    private final LayoutManager layoutManager;

    public ManualLayoutAlgorithm(LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
	public void updateLayout(Graph graph) {
        final LayoutEntity layoutEntity = layoutManager != null ? layoutManager.loadLayout(graph) : null;
        if (layoutEntity != null) {
            // if we have a persisted layout, we apply it ...
            final Layout layout = graph.getLayout();
            final Collection<Vertex> vertices = graph.getDisplayVertices();
            for (Vertex vertex : vertices) {
                PointEntity pointEntity = layoutEntity.getPosition(vertex.getNamespace(), vertex.getId());
                layout.setLocation(vertex, new Point(pointEntity.getX(), pointEntity.getY()));
            }
        } else {
            // otherwise we apply the manual layout ...
            final Collection<Vertex> vertices = graph.getDisplayVertices();
            final Layout layout = graph.getLayout();
            final long notLayedOutCount = vertices.stream().filter(v -> {
                Point location = layout.getLocation(v);
                return location.getX() == 0 && location.getY() == 0;
            }).count();
            final long noVertexLocationCount = vertices.stream().filter(v -> {
                boolean hasNoX = v.getX() == null || v.getX().intValue() == 0;
                boolean hasNoY = v.getY() == null || v.getY().intValue() == 0;
                return hasNoX && hasNoY;
            }).count();

            // If nothing was manually layed out before, or the vertices do not have x,y coordinates assigned, we
            // manually apply the Grid Layout
            if (notLayedOutCount == vertices.size() && noVertexLocationCount == vertices.size()) {
                new GridLayoutAlgorithm().updateLayout(graph);
            } else if(noVertexLocationCount != vertices.size()) {
                // If we have at least one vertex with coordinates != (0,0), we apply them to the layout
                for (Vertex vertex : vertices) {
                    layout.setLocation(vertex, new Point(vertex.getX(), vertex.getY()));
                }
            } else {
                // Otherwise, we apply the x,y coordinates already assigned.
                // This means, the last calculated layout coordinates are re-applied.
                // At the moment no communication between ui and backend is made, when changing the coordinates manually
                // This is only done when the user explicitly saves the layout
                for (Vertex vertex : vertices) {
                    Point p = layout.getLocation(vertex);
                    layout.setLocation(vertex, p);
                }
            }
        }
	}

}
