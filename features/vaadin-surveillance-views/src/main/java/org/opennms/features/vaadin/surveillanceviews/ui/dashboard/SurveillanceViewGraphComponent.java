/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.opennms.features.vaadin.components.graph.GraphContainer;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.vaadin.data.Property;
import com.vaadin.server.Page;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;

/**
 * This component is used to display the available graphs for a surveillance view dashboard.
 *
 * @author Christian Pape
 */
public class SurveillanceViewGraphComponent extends VerticalLayout implements SurveillanceViewDetail, Page.BrowserWindowResizeListener {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewGraphComponent.class);
    /**
     * the surveillance view service instance
     */
    private SurveillanceViewService m_surveillanceViewService;
    /**
     * flag, whether links are enabled
     */
    protected boolean m_enabled;
    /**
     * selection boxes for node, resource and graph
     */
    private NativeSelect m_nodeSelect, m_resourceSelect, m_graphSelect;
    /**
     * the graph layout
     */
    private VerticalLayout m_graphLayout;
    /**
     * the refresh future
     */
    protected ListenableFuture<List<OnmsNode>> m_future;

    /**
     * Constructor for instantiating this component.
     *
     * @param surveillanceViewService the surveillance view service to be used
     * @param enabled                 the flag should links be enabled?
     */
    public SurveillanceViewGraphComponent(SurveillanceViewService surveillanceViewService, boolean enabled) {
        /**
         * set the fields
         */
        m_surveillanceViewService = surveillanceViewService;
        m_enabled = enabled;

        /**
         * create layout for caption
         */
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setWidth(100, Unit.PERCENTAGE);
        horizontalLayout.setSpacing(false);
        horizontalLayout.setPrimaryStyleName("v-caption-surveillance-view");
        horizontalLayout.addComponent(new Label("Resource Graphs"));
        addComponent(horizontalLayout);

        /**
         * create node selection box
         */
        m_nodeSelect = new NativeSelect();
        m_nodeSelect.setNullSelectionAllowed(false);

        m_nodeSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                Integer onmsNodeId = (Integer) m_nodeSelect.getValue();

                m_resourceSelect.removeAllItems();

                if (onmsNodeId != null) {
                    Map<OnmsResourceType, List<OnmsResource>> map = getSurveillanceViewService().getResourceTypeMapForNodeId(onmsNodeId);

                    for (OnmsResourceType onmsResourceType : map.keySet()) {
                        for (OnmsResource onmsResource : map.get(onmsResourceType)) {
                            m_resourceSelect.addItem(onmsResource.getId().toString());
                            m_resourceSelect.setItemCaption(onmsResource.getId().toString(), onmsResourceType.getLabel() + ": " + onmsResource.getLabel());
                        }
                    }

                    Iterator<?> i = m_resourceSelect.getItemIds().iterator();

                    if (i.hasNext()) {
                        m_resourceSelect.select(i.next());
                    }
                }
            }
        });

        /**
         * create resource selection box
         */
        m_resourceSelect = new NativeSelect();
        m_resourceSelect.setNullSelectionAllowed(false);

        m_resourceSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                ResourceId onmsResourceId = ResourceId.fromString((String) m_resourceSelect.getValue());

                m_graphSelect.removeAllItems();

                if (onmsResourceId != null) {
                    Map<String, String> map = getSurveillanceViewService().getGraphResultsForResourceId(onmsResourceId);

                    for (String string : map.keySet()) {
                        m_graphSelect.addItem(map.get(string));
                        m_graphSelect.setItemCaption(map.get(string), string);
                    }

                    Iterator<?> i = m_graphSelect.getItemIds().iterator();

                    if (i.hasNext()) {
                        m_graphSelect.select(i.next());
                    }
                }
            }
        });

        /**
         * create graph selection box
         */
        m_graphSelect = new NativeSelect();
        m_graphSelect.setNullSelectionAllowed(false);

        m_graphSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                String graphName = (String) m_graphSelect.getValue();
                String resourceId = (String) m_resourceSelect.getValue();
                replaceGraph(graphName, resourceId);
            }
        });

        /**
         * set box sizes to full
         */
        m_nodeSelect.setSizeFull();
        m_resourceSelect.setSizeFull();
        m_graphSelect.setSizeFull();

        /**
         * add box styles
         */
        m_nodeSelect.addStyleName("surveillance-view");
        m_resourceSelect.addStyleName("surveillance-view");
        m_graphSelect.addStyleName("surveillance-view");

        /**
         * create layout for storing the graph
         */
        m_graphLayout = new VerticalLayout();
        m_graphLayout.setSizeUndefined();
        m_graphLayout.setWidth(100, Unit.PERCENTAGE);
        m_graphLayout.setMargin(true);

        /**
         * create layout for selection boxes
         */
        HorizontalLayout selectionBoxesLayout = new HorizontalLayout();
        selectionBoxesLayout.setSizeFull();
        selectionBoxesLayout.addComponent(m_nodeSelect);
        selectionBoxesLayout.addComponent(m_resourceSelect);
        selectionBoxesLayout.addComponent(m_graphSelect);

        m_graphLayout.setId("graphLayout");

        /**
         * ...and call it when page is constructed. Also add a resize listener...
         */
        addAttachListener(new AttachListener() {
            @Override
            public void attach(AttachEvent attachEvent) {
                getUI().getPage().addBrowserWindowResizeListener(SurveillanceViewGraphComponent.this);
            }
        });

        /**
         * ... and remove the resize listener on detach event
         */
        addDetachListener(new DetachListener() {
            @Override
            public void detach(DetachEvent detachEvent) {
                getUI().getPage().removeBrowserWindowResizeListener(SurveillanceViewGraphComponent.this);
            }
        });

        /**
         * add layout for selection boxes and image
         */
        addComponent(selectionBoxesLayout);
        addComponent(m_graphLayout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void browserWindowResized(Page.BrowserWindowResizeEvent browserWindowResizeEvent) {
        String graphName = (String) m_graphSelect.getValue();
        String resourceId = (String) m_resourceSelect.getValue();

        if (graphName != null && resourceId != null) {
            replaceGraph(graphName, resourceId);
        }
    }

    private void replaceGraph(String graphName, String resourceId) {
        m_graphLayout.removeAllComponents();

        if (graphName != null && resourceId != null) {
            final GraphContainer graph = new GraphContainer(graphName, resourceId);
            // Span the last hour
            final Calendar calendar = new GregorianCalendar();
            graph.setEnd(calendar.getTime());
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            graph.setStart(calendar.getTime());
            // Use all of the available width
            graph.setWidthRatio(1.0d);
            // Use a smaller height ratio
            graph.setHeightRatio(0.2d);
            m_graphLayout.addComponent(graph);
        }
    }

    /**
     * Return the associated surveillance view service.
     *
     * @return the surveillance view service.
     */
    protected SurveillanceViewService getSurveillanceViewService() {
        return m_surveillanceViewService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshDetails(final Set<OnmsCategory> rowCategories, final Set<OnmsCategory> colCategories) {
        if (m_future != null && !m_future.isDone()) {
            return;
        }

        m_nodeSelect.setEnabled(false);
        m_resourceSelect.setEnabled(false);
        m_graphSelect.setEnabled(false);

        m_future = getSurveillanceViewService().getExecutorService().submit(new Callable<List<OnmsNode>>() {
            @Override
            public List<OnmsNode> call() throws Exception {
                /**
                 * retrieve the matching nodes
                 */
                return getSurveillanceViewService().getNodesForCategories(rowCategories, colCategories);
            }
        });

        m_future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<OnmsNode> nodes = m_future.get();
                    getUI().access(new Runnable() {
                        @Override
                        public void run() {
                            /**
                             * save the current selection
                             */
                            Integer selectedNodeId = (Integer) m_nodeSelect.getValue();
                            String selectedResourceId = (String) m_resourceSelect.getValue();
                            String selectedGraph = (String) m_graphSelect.getValue();

                            LOG.debug("Saved selection={} / {} / {}", selectedNodeId == null ? "null" : selectedNodeId, selectedResourceId == null ? "null" : selectedResourceId, selectedGraph);

                            /**
                             * remove all entries in the node selection box
                             */
                            m_nodeSelect.removeAllItems();

                            /**
                             * add the new items
                             */
                            if (nodes != null && !nodes.isEmpty()) {
                                for (OnmsNode node : nodes) {
                                    m_nodeSelect.addItem(node.getId());
                                    m_nodeSelect.setItemCaption(node.getId(), "Node: " + node.getLabel());
                                }
                            }

                            m_nodeSelect.setEnabled(true);
                            m_resourceSelect.setEnabled(true);
                            m_graphSelect.setEnabled(true);

                            /**
                             * try to select the same node/resource/graph combination as before
                             */
                            if (selectedNodeId != null) {
                                for (Integer onmsNodeId : (Collection<Integer>) m_nodeSelect.getItemIds()) {
                                    if (onmsNodeId.equals(selectedNodeId)) {
                                        m_nodeSelect.select(onmsNodeId);
                                        if (selectedResourceId != null) {
                                            for (String onmsResourceId : (Collection<String>) m_resourceSelect.getItemIds()) {
                                                if (onmsResourceId.equals(selectedResourceId)) {
                                                    m_resourceSelect.select(onmsResourceId);
                                                    if (selectedGraph != null) {
                                                        m_graphSelect.select(selectedGraph);
                                                    }
                                                }
                                            }
                                        }
                                        return;
                                    }
                                }
                            }

                            /**
                             * if nothing was selected before, just select the first entry if possible
                             */
                            Iterator<?> i = m_nodeSelect.getItemIds().iterator();

                            if (i.hasNext()) {
                                m_nodeSelect.select(i.next());
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    LOG.error("Interrupted", e);
                } catch (ExecutionException e) {
                    LOG.error("Exception in task", e.getCause());
                }
            }
        }, MoreExecutors.directExecutor());
    }
}
