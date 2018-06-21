/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.dashlets;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.StringUtils;
import org.opennms.features.vaadin.components.graph.GraphContainer;
import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * This dashlet class is used to display the reports of a Ksc report.
 *
 * @author Christian Pape
 */
public class KscDashlet extends AbstractDashlet {
    private NodeDao m_nodeDao;
    private ResourceDao m_resourceDao;
    private TransactionOperations m_transactionOperations;
    private DashletComponent m_wallboardComponent;
    private DashletComponent m_dashboardComponent;
    private static final int DEFAULT_GRAPH_WIDTH_PX = 400;

    /**
     * Constructor for instantiating new objects.
     *
     * @param name        the name of the dashlet
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public KscDashlet(String name, DashletSpec dashletSpec, NodeDao nodeDao, ResourceDao resourceDao, TransactionOperations transactionOperations) {
        super(name, dashletSpec);
        /**
         * Setting the member fields
         */
        m_nodeDao = nodeDao;
        m_resourceDao = resourceDao;
        m_transactionOperations = transactionOperations;
    }

    @Override
    public DashletComponent getWallboardComponent() {
        if (m_wallboardComponent == null) {
            m_wallboardComponent = new AbstractDashletComponent() {
                private GridLayout m_gridLayout = new GridLayout();

                {
                    m_gridLayout.setCaption(getName());
                    m_gridLayout.setSizeFull();
                    m_gridLayout.setColumns(1);
                    m_gridLayout.setRows(1);
                }

                @Override
                public void refresh() {
                    m_gridLayout.removeAllComponents();

                    /**
                     * initializing the parameters
                     */
                    int columns = 0;
                    int rows = 0;

                    String kscReportName = getDashletSpec().getParameters().get("kscReport");

                    if (kscReportName == null || "".equals(kscReportName)) {
                        return;
                    }

                    KSC_PerformanceReportFactory kscPerformanceReportFactory = KSC_PerformanceReportFactory.getInstance();

                    Map<Integer, String> reportsMap = kscPerformanceReportFactory.getReportList();

                    int kscReportId = -1;

                    for (Map.Entry<Integer, String> entry : reportsMap.entrySet()) {

                        if (kscReportName.equals(entry.getValue())) {
                            kscReportId = entry.getKey();
                            break;
                        }
                    }

                    if (kscReportId == -1) {
                        return;
                    }

                    Report kscReport = kscPerformanceReportFactory.getReportByIndex(kscReportId);

                    columns = Math.max(1, kscReport.getGraphsPerLine().orElse(1));

                    rows = kscReport.getGraphs().size() / columns;

                    if (rows == 0) {
                        rows = 1;
                    }

                    if (kscReport.getGraphs().size() % columns > 0) {
                        rows++;
                    }

                    /**
                     * setting new columns/rows
                     */
                    m_gridLayout.setColumns(columns);
                    m_gridLayout.setRows(rows);

                    int i = 0;

                    /**
                     * adding the components
                     */

                    Page.getCurrent().getStyles().add(".box { margin: 5px; background-color: #444; border: 1px solid #999; border-top: 0; overflow: auto; }");
                    Page.getCurrent().getStyles().add(".text { color:#ffffff; line-height: 11px; font-size: 9px; font-family: 'Lucida Grande', Verdana, sans-serif; font-weight: bold; }");
                    Page.getCurrent().getStyles().add(".margin { margin:5px; }");

                    for (int y = 0; y < m_gridLayout.getRows(); y++) {
                        for (int x = 0; x < m_gridLayout.getColumns(); x++) {

                            if (i < kscReport.getGraphs().size()) {
                                final int index = i;
                                Graph graph = kscReport.getGraphs().get(index);

                                Map<String, String> data = getDataForResourceId(graph.getNodeId().orElse(null), graph.getResourceId().orElse(null));

                                Calendar beginTime = Calendar.getInstance();
                                Calendar endTime = Calendar.getInstance();

                                KSC_PerformanceReportFactory.getBeginEndTime(graph.getTimespan(), beginTime, endTime);

                                GraphContainer graphContainer = getGraphContainer(graph, beginTime.getTime(), endTime.getTime());

                                VerticalLayout verticalLayout = new VerticalLayout();

                                HorizontalLayout horizontalLayout = new HorizontalLayout();
                                horizontalLayout.addStyleName("box");
                                horizontalLayout.setWidth("100%");
                                horizontalLayout.setHeight("42px");

                                VerticalLayout leftLayout = new VerticalLayout();
                                leftLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
                                leftLayout.addStyleName("margin");

                                Label labelTitle;

                                if (graph.getTitle() == null || "".equals(graph.getTitle())) {
                                    labelTitle = new Label("&nbsp;");
                                    labelTitle.setContentMode(ContentMode.HTML);
                                } else {
                                    labelTitle = new Label(graph.getTitle());
                                }

                                labelTitle.addStyleName("text");

                                Label labelFrom = new Label("From: " + StringUtils.toStringEfficiently(beginTime.getTime()));
                                labelFrom.addStyleName("text");

                                Label labelTo = new Label("To: " + StringUtils.toStringEfficiently(endTime.getTime()));
                                labelTo.addStyleName("text");

                                Label labelNodeLabel = new Label(data.get("nodeLabel"));
                                labelNodeLabel.addStyleName("text");

                                Label labelResourceLabel = new Label(data.get("resourceTypeLabel") + ": " + data.get("resourceLabel"));
                                labelResourceLabel.addStyleName("text");

                                leftLayout.addComponent(labelTitle);
                                leftLayout.addComponent(labelFrom);
                                leftLayout.addComponent(labelTo);

                                VerticalLayout rightLayout = new VerticalLayout();
                                rightLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
                                rightLayout.addStyleName("margin");

                                rightLayout.addComponent(labelNodeLabel);
                                rightLayout.addComponent(labelResourceLabel);

                                horizontalLayout.addComponent(leftLayout);
                                horizontalLayout.addComponent(rightLayout);

                                horizontalLayout.setExpandRatio(leftLayout, 1.0f);
                                horizontalLayout.setExpandRatio(rightLayout, 1.0f);

                                verticalLayout.addComponent(horizontalLayout);
                                verticalLayout.addComponent(graphContainer);
                                verticalLayout.setWidth(DEFAULT_GRAPH_WIDTH_PX, Unit.PIXELS);

                                m_gridLayout.addComponent(verticalLayout, x, y);

                                verticalLayout.setComponentAlignment(horizontalLayout, Alignment.MIDDLE_CENTER);
                                verticalLayout.setComponentAlignment(graphContainer, Alignment.MIDDLE_CENTER);
                                m_gridLayout.setComponentAlignment(verticalLayout, Alignment.MIDDLE_CENTER);
                            }
                            i++;
                        }
                    }
                }

                @Override
                public Component getComponent() {
                    return m_gridLayout;
                }
            };
        }

        return m_wallboardComponent;
    }

    @Override
    public DashletComponent getDashboardComponent() {
        if (m_dashboardComponent == null) {
            m_dashboardComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setSizeFull();
                }

                @Override
                public void refresh() {
                    m_verticalLayout.removeAllComponents();

                    String kscReportName = getDashletSpec().getParameters().get("kscReport");

                    if (kscReportName == null || "".equals(kscReportName)) {
                        return;
                    }

                    KSC_PerformanceReportFactory kscPerformanceReportFactory = KSC_PerformanceReportFactory.getInstance();

                    Map<Integer, String> reportsMap = kscPerformanceReportFactory.getReportList();

                    int kscReportId = -1;

                    for (Map.Entry<Integer, String> entry : reportsMap.entrySet()) {

                        if (kscReportName.equals(entry.getValue())) {
                            kscReportId = entry.getKey();
                            break;
                        }
                    }

                    if (kscReportId == -1) {
                        return;
                    }

                    Report kscReport = kscPerformanceReportFactory.getReportByIndex(kscReportId);

                    Page.getCurrent().getStyles().add(".box { margin: 5px; background-color: #444; border: 1px solid #999; border-top: 0; overflow: auto; }");
                    Page.getCurrent().getStyles().add(".text { color:#ffffff; line-height: 11px; font-size: 9px; font-family: 'Lucida Grande', Verdana, sans-serif; font-weight: bold; }");
                    Page.getCurrent().getStyles().add(".margin { margin:5px; }");

                    Accordion accordion = new Accordion();
                    accordion.setSizeFull();
                    m_verticalLayout.addComponent(accordion);

                    for (Graph graph : kscReport.getGraphs()) {
                        Map<String, String> data = getDataForResourceId(graph.getNodeId().orElse(null), graph.getResourceId().orElse(null));

                        Calendar beginTime = Calendar.getInstance();
                        Calendar endTime = Calendar.getInstance();

                        KSC_PerformanceReportFactory.getBeginEndTime(graph.getTimespan(), beginTime, endTime);

                        GraphContainer graphContainer = getGraphContainer(graph, beginTime.getTime(), endTime.getTime());

                        VerticalLayout verticalLayout = new VerticalLayout();

                        HorizontalLayout horizontalLayout = new HorizontalLayout();
                        horizontalLayout.addStyleName("box");
                        horizontalLayout.setWidth("100%");
                        horizontalLayout.setHeight("42px");

                        VerticalLayout leftLayout = new VerticalLayout();
                        leftLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
                        leftLayout.addStyleName("margin");

                        Label labelTitle;

                        if (graph.getTitle() == null || "".equals(graph.getTitle())) {
                            labelTitle = new Label("&nbsp;");
                            labelTitle.setContentMode(ContentMode.HTML);
                        } else {
                            labelTitle = new Label(graph.getTitle());
                        }

                        labelTitle.addStyleName("text");

                        Label labelFrom = new Label("From: " + StringUtils.toStringEfficiently(beginTime.getTime()));
                        labelFrom.addStyleName("text");

                        Label labelTo = new Label("To: " + StringUtils.toStringEfficiently(endTime.getTime()));
                        labelTo.addStyleName("text");

                        Label labelNodeLabel = new Label(data.get("nodeLabel"));
                        labelNodeLabel.addStyleName("text");

                        Label labelResourceLabel = new Label(data.get("resourceTypeLabel") + ": " + data.get("resourceLabel"));
                        labelResourceLabel.addStyleName("text");

                        leftLayout.addComponent(labelTitle);
                        leftLayout.addComponent(labelFrom);
                        leftLayout.addComponent(labelTo);

                        VerticalLayout rightLayout = new VerticalLayout();
                        rightLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
                        rightLayout.addStyleName("margin");

                        rightLayout.addComponent(labelNodeLabel);
                        rightLayout.addComponent(labelResourceLabel);

                        horizontalLayout.addComponent(leftLayout);
                        horizontalLayout.addComponent(rightLayout);

                        horizontalLayout.setExpandRatio(leftLayout, 1.0f);
                        horizontalLayout.setExpandRatio(rightLayout, 1.0f);

                        verticalLayout.addComponent(horizontalLayout);
                        verticalLayout.addComponent(graphContainer);
                        verticalLayout.setWidth(DEFAULT_GRAPH_WIDTH_PX, Unit.PIXELS);

                        accordion.addTab(verticalLayout, data.get("nodeLabel") + "/" + data.get("resourceTypeLabel") + ": " + data.get("resourceLabel"));

                        verticalLayout.setComponentAlignment(horizontalLayout, Alignment.MIDDLE_CENTER);
                        verticalLayout.setComponentAlignment(graphContainer, Alignment.MIDDLE_CENTER);
                        verticalLayout.setMargin(true);
                    }
                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }

        return m_dashboardComponent;
    }

    /**
     * Returns a map with graph metadata for a given nodeId.
     *
     * @return a map with meta data, like resourceLabel, resourceTypeLabel
     */
    public Map<String, String> getDataForResourceId(final String nodeId, final String resourceIdString) {
        return m_transactionOperations.execute(new TransactionCallback<Map<String, String>>() {
            @Override
            public Map<String, String> doInTransaction(TransactionStatus transactionStatus) {
                Map<String, String> data = new HashMap<>();
                ResourceId resourceId = ResourceId.fromString(resourceIdString);

                OnmsNode node;
                OnmsResource resource;
                if(nodeId == null){
                    resource = determineResourceByResourceId(resourceId);
                    node = ResourceTypeUtils.getNodeFromResource(resource);
                } else {
                    node = m_nodeDao.get(nodeId);
                    resource = m_resourceDao.getResourceForNode(node);
                }
                data.put("nodeId", node.getNodeId());
                data.put("nodeLabel", node.getLabel());

                for (OnmsResource onmsResource : resource.getChildResources()) {
                    if (resourceId.equals(onmsResource.getId())) {
                        data.put("resourceLabel", onmsResource.getLabel());
                        data.put("resourceTypeLabel", onmsResource.getResourceType().getLabel());
                        break;
                    }
                }
                return data;
            }
        });
    }

    OnmsResource determineResourceByResourceId(ResourceId resourceId){
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        resource =(resource.getParent()== null) ? resource : resource.getParent();
        return resource;
    }

    private static GraphContainer getGraphContainer(Graph graph, Date start, Date end) {
        GraphContainer graphContainer = new GraphContainer(graph.getGraphtype(), graph.getResourceId().orElse(null));
        graphContainer.setTitle(graph.getTitle());
        // Setup the time span
        graphContainer.setStart(start);
        graphContainer.setEnd(end);
        // Use all of the available width
        graphContainer.setWidthRatio(1.0d);
        return graphContainer;
    }
}
