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

import java.util.Map;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.VertexInfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertex;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;

public class BusinessServiceVertexAttributesInfoPanelItemProvider extends VertexInfoPanelItemProvider {

    private BusinessServiceManager businessServiceManager;

    private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;

    public BusinessServiceVertexAttributesInfoPanelItemProvider(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        this.transactionAwareBeanProxyFactory = transactionAwareBeanProxyFactory;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
    }

    private Component createComponent(VertexRef ref) {
        final FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(false);
        formLayout.setMargin(false);

        final BusinessService businessService = businessServiceManager.getBusinessServiceById(((BusinessServiceVertex) ref).getServiceId());

        for (Map.Entry<String, String> e : businessService.getAttributes().entrySet()) {
            formLayout.addComponent(createLabel(e.getKey(), e.getValue()));
        }

        return formLayout;
    }

    @Override
    protected boolean contributeTo(VertexRef vertexRef, GraphContainer container) {
        return vertexRef instanceof BusinessServiceVertex && !this.businessServiceManager.getBusinessServiceById(((BusinessServiceVertex) vertexRef).getServiceId()).getAttributes().isEmpty();
    }

    @Override
    protected InfoPanelItem createInfoPanelItem(VertexRef ref, GraphContainer graphContainer) {
        return new DefaultInfoPanelItem()
                .withOrder(10)
                .withTitle("Business Service Attributes")
                .withComponent(createComponent(ref));
    }

}


