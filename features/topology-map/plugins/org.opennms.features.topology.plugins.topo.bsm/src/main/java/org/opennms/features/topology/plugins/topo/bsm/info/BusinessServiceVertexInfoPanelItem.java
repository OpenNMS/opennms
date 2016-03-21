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
import org.opennms.features.topology.api.info.VertexInfoPanelItem;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.AbstractBusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertexVisitor;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesTopologyProvider;
import org.opennms.features.topology.plugins.topo.bsm.IpServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.ReductionKeyVertex;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverityAbove;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReduceFunctionVisitor;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;

public class BusinessServiceVertexInfoPanelItem implements VertexInfoPanelItem {

    private BusinessServiceManager businessServiceManager;

    private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;

    public BusinessServiceVertexInfoPanelItem(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        this.transactionAwareBeanProxyFactory = transactionAwareBeanProxyFactory;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
    }

    @Override
    public boolean contributesTo(VertexRef vertexRef, GraphContainer container) {
        return vertexRef.getNamespace().equals(BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE);
    }

    @Override
    public Component getComponent(VertexRef ref, GraphContainer container) {
        final FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(false);
        formLayout.setMargin(false);

        formLayout.addComponent(createLabel("Name", ref.getLabel()));

        ((AbstractBusinessServiceVertex) ref).accept(new BusinessServiceVertexVisitor<Void>() {
            @Override
            public Void visit(BusinessServiceVertex vertex) {
                BusinessService businessService = businessServiceManager.getBusinessServiceById(vertex.getServiceId());

                formLayout.addComponent(createLabel("Reduce function", describeReduceFunction(businessService.getReduceFunction())));

                return null;
            }

            @Override
            public Void visit(IpServiceVertex vertex) {
                IpService ipService = businessServiceManager.getIpServiceById(vertex.getIpServiceId());
                formLayout.addComponent(createLabel("Interface", ipService.getIpAddress()));
                formLayout.addComponent(createLabel("Service", ipService.getServiceName()));
                return null;
            }

            @Override
            public Void visit(ReductionKeyVertex vertex) {
                formLayout.addComponent(createLabel("Reduction Key", vertex.getReductionKey()));
                if (!vertex.getReductionKey().equals(vertex.getLabel())) {
                    formLayout.addComponent(createLabel("Friendly Name", vertex.getLabel()));
                }
                return null;
            }
        });

        return formLayout;
    }

    @Override
    public String getTitle(VertexRef ref) {
        return ((AbstractBusinessServiceVertex) ref).accept(new BusinessServiceVertexVisitor<String>() {
            @Override
            public String visit(BusinessServiceVertex vertex) {
                return "Business Service Details";
            }

            @Override
            public String visit(IpServiceVertex vertex) {
                return "IP Service Details";
            }

            @Override
            public String visit(ReductionKeyVertex vertex) {
                return "Reduction Key Details";
            }
        });
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private static String describeReduceFunction(final ReductionFunction reductionFunction) {
        return reductionFunction.accept(new ReduceFunctionVisitor<String>() {
            @Override
            public String visit(HighestSeverity function) {
                return function.getClass().getSimpleName();
            }

            @Override
            public String visit(HighestSeverityAbove function) {
                return String.format("%s (%s)",
                        function.getClass().getSimpleName(),
                        function.getThreshold().getLabel());
            }

            @Override
            public String visit(Threshold function) {
                return String.format("%s (%s)",
                        function.getClass().getSimpleName(),
                        Float.toString(function.getThreshold()));
            }
        });
    }
}


