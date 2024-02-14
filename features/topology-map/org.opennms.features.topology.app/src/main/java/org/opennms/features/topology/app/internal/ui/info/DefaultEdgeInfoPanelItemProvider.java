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
package org.opennms.features.topology.app.internal.ui.info;

import static org.opennms.netmgt.vaadin.core.UIHelper.createLabel;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.EdgeInfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.EdgeRef;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;

public class DefaultEdgeInfoPanelItemProvider extends EdgeInfoPanelItemProvider {

    private Component createComponent(EdgeRef ref) {
        FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(false);
        formLayout.setMargin(false);

        if (ref instanceof AbstractEdge) {
            AbstractEdge edge = (AbstractEdge) ref;

            formLayout.addComponent(createLabel("Source", edge.getSource().getVertex().getLabel()));
            formLayout.addComponent(createLabel("Target", edge.getTarget().getVertex().getLabel()));
        }

        return formLayout;
    }

    @Override
    protected boolean contributeTo(EdgeRef ref, GraphContainer container) {
        return true;
    }

    @Override
    protected InfoPanelItem createInfoPanelItem(EdgeRef ref, GraphContainer graphContainer) {
        return new DefaultInfoPanelItem()
                .withOrder(Integer.MAX_VALUE)
                .withTitle("Technical Details")
                .withComponent(createComponent(ref));
    }
}
