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

import static org.opennms.netmgt.vaadin.core.UIHelper.createButton;
import static org.opennms.netmgt.vaadin.core.UIHelper.createLabel;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.VertexInfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.ui.NodeInfoWindow;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.themes.BaseTheme;

public class NodeInfoPanelItemProvider extends VertexInfoPanelItemProvider {

    private final NodeDao nodeDao;

    public NodeInfoPanelItemProvider(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    private Component createComponent(AbstractVertex ref) {
        Preconditions.checkState(ref.getNodeID() != null, "no Node ID defined.");
        OnmsNode node = nodeDao.get(ref.getNodeID());
        FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(false);
        formLayout.setMargin(false);

        formLayout.addComponent(createLabel("Node ID", "" + node.getId()));

        final HorizontalLayout nodeButtonLayout = new HorizontalLayout();
        Button nodeButton = createButton(node.getLabel(), null, null, event -> new NodeInfoWindow(ref.getNodeID()).open());
        nodeButton.setStyleName(BaseTheme.BUTTON_LINK);
        nodeButtonLayout.addComponent(nodeButton);
        nodeButtonLayout.setCaption("Node Label");
        formLayout.addComponent(nodeButtonLayout);

        if (!Strings.isNullOrEmpty(node.getSysObjectId())) {
            formLayout.addComponent(createLabel("Enterprise OID", node.getSysObjectId()));
        }

        return formLayout;
    }

    @Override
    protected boolean contributeTo(VertexRef ref, GraphContainer container) {
        return ref instanceof AbstractVertex && ((AbstractVertex) ref).getNodeID() != null;
    }

    @Override
    protected InfoPanelItem createInfoPanelItem(VertexRef ref, GraphContainer graphContainer) {
        return new DefaultInfoPanelItem()
                .withOrder(-500)
                .withTitle("Node Details")
                .withComponent(createComponent((AbstractVertex) ref));
    }

}
