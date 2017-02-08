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

package org.opennms.features.topology.plugins.topo.bsm.info;

import static org.opennms.netmgt.vaadin.core.UIHelper.createLabel;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.EdgeInfoPanelItem;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceEdge;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesTopologyProvider;
import org.opennms.netmgt.bsm.service.model.functions.map.Decrease;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunctionVisitor;
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;

public class BusinessServiceEdgeInfoPanelItem extends EdgeInfoPanelItem {

    @Override
    protected Component getComponent(EdgeRef ref, GraphContainer container) {
        FormLayout formLayout = new FormLayout();
        formLayout.setMargin(false);
        formLayout.setSpacing(false);

        final BusinessServiceEdge businessServiceEdge = ((BusinessServiceEdge) ref);
        formLayout.addComponent(createLabel("Map Function", describeMapFunction(businessServiceEdge.getMapFunction())));
        formLayout.addComponent(createLabel("Weight", Float.toString(businessServiceEdge.getWeight())));

        return formLayout;
    }

    @Override
    protected boolean contributesTo(EdgeRef edgeRef, GraphContainer containe) {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE.equals(edgeRef.getNamespace());
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected String getTitle(EdgeRef edgeRef) {
        return "Map Function Details";
    }

    private static String describeMapFunction(final MapFunction mapFunction) {
        return mapFunction.accept(new MapFunctionVisitor<String>() {
            @Override
            public String visit(Decrease function) {
                return function.getClass().getSimpleName();
            }

            @Override
            public String visit(Identity function) {
                return function.getClass().getSimpleName();
            }

            @Override
            public String visit(Ignore function) {
                return function.getClass().getSimpleName();
            }

            @Override
            public String visit(Increase function) {
                return function.getClass().getSimpleName();
            }

            @Override
            public String visit(SetTo function) {
                return String.format("%s (%s)", function.getClass().getSimpleName(), function.getStatus().getLabel());
            }
        });
    }
}


