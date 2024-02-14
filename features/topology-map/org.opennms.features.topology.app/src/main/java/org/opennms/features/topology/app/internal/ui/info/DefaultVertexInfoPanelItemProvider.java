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
import org.opennms.features.topology.api.info.VertexInfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;

public class DefaultVertexInfoPanelItemProvider extends VertexInfoPanelItemProvider {

    private Component createComponent(VertexRef ref) {
        FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(false);
        formLayout.setMargin(false);

        formLayout.addComponent(createLabel("Name", ref.getLabel()));
        formLayout.addComponent(createLabel("ID", String.format("%s:%s", ref.getNamespace(), ref.getId())));

        if (ref instanceof AbstractVertex) {
            AbstractVertex vertex = (AbstractVertex) ref;

            formLayout.addComponent(createLabel("Icon Key", vertex.getIconKey()));

            if (vertex.getIpAddress() != null) {
                formLayout.addComponent(createLabel("IP Address", vertex.getIpAddress()));
            }
        }

        return formLayout;
    }

    @Override
    protected boolean contributeTo(final VertexRef ref, final GraphContainer graphContainer) {
        return true;
    }

    @Override
    protected InfoPanelItem createInfoPanelItem(VertexRef ref, GraphContainer graphContainer) {
        return new DefaultInfoPanelItem()
                .withOrder(Integer.MAX_VALUE)
                .withTitle("Technical Details")
                .withComponent(createComponent(ref));
    }
}
