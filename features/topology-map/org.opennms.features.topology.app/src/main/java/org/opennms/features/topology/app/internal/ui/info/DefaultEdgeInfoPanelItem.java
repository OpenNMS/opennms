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

import static org.opennms.netmgt.vaadin.core.UIHelper.createLabel;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.EdgeInfoPanelItem;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.EdgeRef;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;

public class DefaultEdgeInfoPanelItem extends EdgeInfoPanelItem {

    @Override
    protected Component getComponent(EdgeRef ref, GraphContainer container) {
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
    protected boolean contributesTo(EdgeRef ref, GraphContainer container) {
        return true;
    }

    @Override
    protected String getTitle(EdgeRef ref) {
        return "Technical Details";
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
