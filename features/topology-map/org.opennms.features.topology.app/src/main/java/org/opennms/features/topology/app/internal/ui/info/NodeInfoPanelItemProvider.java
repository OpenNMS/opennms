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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.BaseTheme;

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
