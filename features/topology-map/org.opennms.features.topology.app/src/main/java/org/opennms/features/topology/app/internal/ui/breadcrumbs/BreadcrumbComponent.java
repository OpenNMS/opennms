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
package org.opennms.features.topology.app.internal.ui.breadcrumbs;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.breadcrumbs.Breadcrumb;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbCriteria;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.themes.BaseTheme;


/**
 * Component to visualizes breadcrumbs.
 * It requires a {@link BreadcrumbCriteria} registered to the {@link GraphContainer}.
 * If no criteria is found, it does not show anything.
 *
 * @author mvrueden
 */
public class BreadcrumbComponent extends CustomComponent implements GraphContainer.ChangeListener {

    public BreadcrumbComponent() {
        final HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.setSpacing(true);
        setCompositionRoot(rootLayout);
        setId("breadcrumbs");
    }

    @Override
    public void graphChanged(GraphContainer graphContainer) {
        final BreadcrumbCriteria criteria = Criteria.getSingleCriteriaForGraphContainer(graphContainer, BreadcrumbCriteria.class, true);
        final HorizontalLayout breadcrumbLayout = (HorizontalLayout) getCompositionRoot();
        breadcrumbLayout.removeAllComponents();

        // Verify that breadcrumbs are enabled
        if (graphContainer.getTopologyServiceClient().getBreadcrumbStrategy() == BreadcrumbStrategy.SHORTEST_PATH_TO_ROOT) {
            final Collection<Vertex> displayVertices = graphContainer.getGraph().getDisplayVertices();
            if (!displayVertices.isEmpty()) {
                final PathTree pathTree = BreadcrumbPathCalculator.findPath(graphContainer.getTopologyServiceClient(), displayVertices.stream().map(v -> (VertexRef) v).collect(Collectors.toSet()));
                final List<Breadcrumb> breadcrumbs = pathTree.toBreadcrumbs();
                criteria.setBreadcrumbs(breadcrumbs);
            }
            for (Breadcrumb eachBreadcrumb : criteria.getBreadcrumbs()) {
                if (breadcrumbLayout.getComponentCount() >= 1) {
                    breadcrumbLayout.addComponent(new Label(" > "));
                }
                breadcrumbLayout.addComponent(createButton(graphContainer, eachBreadcrumb));
            }
        }
    }

    private static Button createButton(GraphContainer container, Breadcrumb breadcrumb) {
        final Button button = new Button();
        final String layerName = getLayerName(container, breadcrumb.getTargetNamespace());
        if (breadcrumb.getSourceVertices().isEmpty()) {
            button.setCaption(layerName);
        } else {
            String sourceLayerName = getLayerName(container, breadcrumb.getSourceVertices().get(0).getNamespace());
            if (breadcrumb.getSourceVertices().size() > 2) {
                button.setCaption("Multiple " + layerName);
                button.setDescription(String.format("Multiple vertices from %s", sourceLayerName));
            } else {
                button.setCaption(breadcrumb.getSourceVertices().stream().map(VertexRef::getLabel).collect(Collectors.joining(", ")));
                button.setDescription(String.format("%s from %s", button.getCaption(), sourceLayerName));
            }
        }
        button.addStyleName(BaseTheme.BUTTON_LINK);
        button.addClickListener((event) -> breadcrumb.clicked(container));
        return button;
    }

    private static String getLayerName(GraphContainer container, String namespace) {
        Objects.requireNonNull(container);
        Objects.requireNonNull(namespace);
        return container.getTopologyServiceClient().getGraphProviderBy(namespace).getTopologyProviderInfo().getName();
    }
}
