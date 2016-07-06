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

import static java.util.Comparator.comparing;
import static org.opennms.features.topology.plugins.topo.bsm.info.BusinessServiceVertexStatusInfoPanelItemProvider.createStatusLabel;
import static org.opennms.netmgt.vaadin.core.UIHelper.createButton;
import static org.opennms.netmgt.vaadin.core.UIHelper.createLabel;

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.VertexInfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.AbstractBusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertexVisitor;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesTopologyProvider;
import org.opennms.features.topology.plugins.topo.bsm.GraphVertexToTopologyVertexConverter;
import org.opennms.features.topology.plugins.topo.bsm.IpServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.ReductionKeyVertex;
import org.opennms.features.topology.plugins.topo.bsm.simulate.SimulationAwareStateMachineFactory;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ExponentialPropagation;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverityAbove;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReduceFunctionVisitor;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ThresholdResultExplanation;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

public class BusinessServiceVertexInfoPanelItemProvider extends VertexInfoPanelItemProvider {

    private BusinessServiceManager businessServiceManager;

    private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;

    public BusinessServiceVertexInfoPanelItemProvider(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        this.transactionAwareBeanProxyFactory = transactionAwareBeanProxyFactory;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
    }

    @Override
    protected boolean contributeTo(VertexRef vertexRef, GraphContainer container) {
        return vertexRef.getNamespace().equals(BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE);
    }

    @Override
    protected InfoPanelItem createInfoPanelItem(VertexRef ref, GraphContainer graphContainer) {
        return new DefaultInfoPanelItem()
                .withOrder(0)
                .withTitle(getTitle((AbstractBusinessServiceVertex) ref))
                .withComponent(createComponent((AbstractBusinessServiceVertex) ref, graphContainer));
    }

    private Component createComponent(AbstractBusinessServiceVertex ref, GraphContainer graphContainer) {
        final FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(false);
        formLayout.setMargin(false);

        ref.accept(new BusinessServiceVertexVisitor<Void>() {
            @Override
            public Void visit(BusinessServiceVertex vertex) {
                final BusinessService businessService = businessServiceManager.getBusinessServiceById(vertex.getServiceId());
                formLayout.addComponent(createLabel("Reduce function", getReduceFunctionDescription(businessService.getReduceFunction())));

                // Apply Reduce Function specific details
                businessService.getReduceFunction().accept(new ReduceFunctionVisitor<Void>() {
                    @Override
                    public Void visit(HighestSeverity highestSeverity) {
                        return null;
                    }

                    @Override
                    public Void visit(HighestSeverityAbove highestSeverityAbove) {
                        return null;
                    }

                    @Override
                    // Threshold is not very transparent, we add an Explain Button in these cases
                    public Void visit(Threshold threshold) {
                        final Button explainButton = createButton("Explain",
                                                                  "Explain the Threshold function",
                                                                  FontAwesome.TABLE,
                                                                  (Button.ClickListener) event -> {
                                                                      ThresholdExplanationWindow explainWindow = new ThresholdExplanationWindow(
                                                                              SimulationAwareStateMachineFactory.createSimulatedStateMachine(businessServiceManager,
                                                                                                                                             graphContainer.getCriteria())
                                                                                                                .explain(businessService,
                                                                                                                         (Threshold) businessService.getReduceFunction()));
                                                                      UI.getCurrent().addWindow(explainWindow);
                                                                  });
                        explainButton.setStyleName(BaseTheme.BUTTON_LINK);
                        formLayout.addComponent(explainButton);
                        return null;
                    }

                    @Override
                    public Void visit(ExponentialPropagation exponentialPropagation) {
                        return null;
                    }
                });
                return null;
            }

            @Override
            public Void visit(IpServiceVertex vertex) {
                IpService ipService = businessServiceManager.getIpServiceById(vertex.getIpServiceId());
                formLayout.addComponent(createLabel("Interface", ipService.getIpAddress()));
                formLayout.addComponent(createLabel("Service", ipService.getServiceName()));
                if (!ipService.getServiceName().equals(vertex.getLabel())) {
                    formLayout.addComponent(createLabel("Friendly Name", vertex.getLabel()));
                }
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

    private static String getTitle(AbstractBusinessServiceVertex ref) {
        return ref.accept(new BusinessServiceVertexVisitor<String>() {
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

    private static String getReduceFunctionDescription(final ReductionFunction reductionFunction) {
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

            @Override
            public String visit(ExponentialPropagation function) {
                return String.format("%s (%s)",
                                     function.getClass().getSimpleName(),
                                     Double.toString(function.getBase()));
            }
        });
    }

    /**
     * Window to explain the Threshold function
     */
    private static class ThresholdExplanationWindow extends Window {
        private static final long serialVersionUID = 1L;

        private static final String EDGE_COLUMN = "Edge";
        private static final String STATUS_COLUMN = "Status";
        private static final String WEIGHT_COLUMN = "Weight";
        private static final String WEIGHT_FACTOR = "Weight Factor";

        private ThresholdExplanationWindow(final ThresholdResultExplanation explanation) {
            setCaption(String.format("Threshold Function Details (%s)", explanation.getFunction().getThreshold()));
            setClosable(true);
            setCloseShortcut(ShortcutAction.KeyCode.ESCAPE);
            setResizable(true);
            setModal(true);
            setWidth(1000, Sizeable.Unit.PIXELS);
            setHeight(200, Unit.PIXELS);
            addStyleName("threshold");
            addStyleName("severity");


            final Table table = new Table();

            // Highlight the selected column
            table.setCellStyleGenerator((Table.CellStyleGenerator) (source, itemId, propertyId) -> {
                if (propertyId != null && propertyId.equals(explanation.getStatus())) {
                    return "selected";
                }
                return null;
            });

            // set header
            table.addContainerProperty(EDGE_COLUMN, String.class, null);
            table.addContainerProperty(STATUS_COLUMN, Label.class, null);
            table.addContainerProperty(WEIGHT_COLUMN, Integer.class, Edge.DEFAULT_WEIGHT);
            table.addContainerProperty(WEIGHT_FACTOR, String.class, null);
            table.addContainerProperty(Status.CRITICAL, String.class, null);
            table.addContainerProperty(Status.MAJOR, String.class, null);
            table.addContainerProperty(Status.MINOR, String.class, null);
            table.addContainerProperty(Status.WARNING, String.class, null);
            table.addContainerProperty(Status.NORMAL, String.class, null);

            // Sort by worst severity, then by name
            List<GraphEdge> sortedGraphEdges = explanation.getGraphEdges().stream()
                    .sorted(
                            comparing(GraphEdge::getStatus)
                                    .reversed()
                                    .thenComparing(it -> getLabel(it, explanation)))
                    .collect(Collectors.toList());

            // draw table
            for (GraphEdge eachEdge : sortedGraphEdges) {
                table.addItem(new Object[] {
                        getLabel(eachEdge, explanation),
                        createStatusLabel(null, eachEdge.getStatus()),
                        eachEdge.getWeight(),
                        toString(explanation.getWeightFactor(eachEdge)),
                        toString(explanation.getStatusFactor(eachEdge, Status.CRITICAL)),
                        toString(explanation.getStatusFactor(eachEdge, Status.MAJOR)),
                        toString(explanation.getStatusFactor(eachEdge, Status.MINOR)),
                        toString(explanation.getStatusFactor(eachEdge, Status.WARNING)),
                        toString(explanation.getStatusFactor(eachEdge, Status.NORMAL)),
                }, eachEdge);
            }

            table.setFooterVisible(true);
            table.setColumnFooter(EDGE_COLUMN, "Total");
            table.setColumnFooter(STATUS_COLUMN, explanation.getStatus().getLabel());
            table.setColumnFooter(WEIGHT_COLUMN, String.valueOf(explanation.getWeightSum()));
            table.setColumnFooter(WEIGHT_FACTOR, toString(explanation.getWeightSumFactor()));
            table.setColumnFooter(Status.CRITICAL, toString(explanation.getStatusResult(Status.CRITICAL)));
            table.setColumnFooter(Status.MAJOR, toString(explanation.getStatusResult(Status.MAJOR)));
            table.setColumnFooter(Status.MINOR, toString(explanation.getStatusResult(Status.MINOR)));
            table.setColumnFooter(Status.WARNING, toString(explanation.getStatusResult(Status.WARNING)));
            table.setColumnFooter(Status.NORMAL, toString(explanation.getStatusResult(Status.NORMAL)));

            VerticalLayout root = new VerticalLayout();
            table.setSizeFull();
            root.addComponent(table);
            root.setExpandRatio(table, 1f);
            root.setSizeFull();

            setContent(root);
            center();
        }

        private String getLabel(GraphEdge graphEdge, ThresholdResultExplanation explanation) {
            if (!Strings.isNullOrEmpty(graphEdge.getFriendlyName())) {
                return graphEdge.getFriendlyName();
            }
            return GraphVertexToTopologyVertexConverter.createTopologyVertex(explanation.getGraphVertex(graphEdge)).getLabel();
        }

        private String toString(double weightFactor) {
            if (weightFactor == 0) {
                return "";
            }
            return String.format("%1.2f", weightFactor);
        }
    }
}


