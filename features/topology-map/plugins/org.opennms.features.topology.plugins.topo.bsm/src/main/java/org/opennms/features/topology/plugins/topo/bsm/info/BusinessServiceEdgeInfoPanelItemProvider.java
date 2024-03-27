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
package org.opennms.features.topology.plugins.topo.bsm.info;

import static org.opennms.netmgt.vaadin.core.UIHelper.createLabel;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.EdgeInfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
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

public class BusinessServiceEdgeInfoPanelItemProvider extends EdgeInfoPanelItemProvider {

    @Override
    protected boolean contributeTo(EdgeRef edgeRef, GraphContainer container) {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE.equals(edgeRef.getNamespace());
    }

    @Override
    protected InfoPanelItem createInfoPanelItem(EdgeRef ref, GraphContainer graphContainer) {
        return new DefaultInfoPanelItem()
                .withComponent(createComponent((BusinessServiceEdge) ref))
                .withTitle("Map Function Details")
                .withOrder(0);
    }

    private Component createComponent(BusinessServiceEdge ref) {
        FormLayout formLayout = new FormLayout();
        formLayout.setMargin(false);
        formLayout.setSpacing(false);

        formLayout.addComponent(createLabel("Map Function", describeMapFunction(ref.getMapFunction())));
        formLayout.addComponent(createLabel("Weight", Float.toString(ref.getWeight())));

        return formLayout;
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


