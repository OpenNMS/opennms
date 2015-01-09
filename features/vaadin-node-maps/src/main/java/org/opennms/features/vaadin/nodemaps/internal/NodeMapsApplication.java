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

package org.opennms.features.vaadin.nodemaps.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.HasExtraComponents;
import org.opennms.features.topology.api.VerticesUpdateManager;
import org.opennms.features.topology.api.VerticesUpdateManager.VerticesUpdateEvent;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.browsers.AlarmTable;
import org.opennms.features.topology.plugins.browsers.NodeTable;
import org.opennms.features.topology.plugins.browsers.SelectionAwareTable;
import org.opennms.osgi.EventProxy;
import org.opennms.osgi.VaadinApplicationContextImpl;
import org.opennms.web.api.OnmsHeaderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

/**
 * The Class Node Maps Application.
 * <p>
 * PointVectors are used instead of Markers because the idea is to use the
 * Cluster Strategy feature.
 * </p>
 * <p>
 * Here are some samples:
 * </p>
 * <ul>
 * <li>http://openlayers.org/dev/examples/strategy-cluster.html</li>
 * <li>http://developers.cloudmade.com/projects/web-maps-api/examples/marker-clustering</li>
 * </ul>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
/*
 * TODO Design questions
 * 
 * 1) Several nodes can share the exact location, so we should determinate the points and associate a
 *    list of nodes to it.
 * 2) Regions are polygons that contains a list of points (nodes or group of nodes).
 *  
 */
/*
 * Display Strategies
 * 
 * 1. Create the NodePoints object
 *    (which is essentially Map<PointVector,List<OnmsNode>>, or Map<PointVector,List<Integer>>).
 * 2. Create the NodeGroups objects
 *    (which is essentially Map<Area,List<OnmsNode>>, or Map<Area,List<Integer>>)
 * 3. Use NodeGroups for the Cluster Strategy (node aggregation)
 * 4. Create a VectorLayer for the NodePoints.
 * 5. Create a strategy to build/display the Popups even using Vaadin Widgets or OpenLayer widgets).
 */
@SuppressWarnings("serial")
@Title("OpenNMS Node Maps")
@Theme("opennms")
@JavaScript({
    "//maps.google.com/maps/api/js?sensor=false",
    "gwt/public/leaflet-0.5.1/leaflet-src.js",
    "gwt/public/openlayers/OpenLayers.js",
    "gwt/public/markercluster/leaflet.markercluster-src.js"

})
@StyleSheet({
    "gwt/public/markercluster/MarkerCluster.css",
    "gwt/public/markercluster/MarkerCluster.Default.css",
    "gwt/public/node-maps.css"
})
public class NodeMapsApplication extends UI {
    private static final Logger LOG = LoggerFactory.getLogger(NodeMapsApplication.class);
    // private static final int REFRESH_INTERVAL = 5 * 60 * 1000;
    private static final int REFRESH_INTERVAL = 10 * 1000;
    private VerticalLayout m_rootLayout;
    private VerticalLayout m_layout;

    private MapWidgetComponent m_mapWidgetComponent;
    private OnmsHeaderProvider m_headerProvider;
    private String m_headerHtml;
    private VaadinRequest m_request;
    private AlarmTable m_alarmTable;
    private NodeTable m_nodeTable;

    public void setHeaderProvider(final OnmsHeaderProvider headerProvider) {
        m_headerProvider = headerProvider;
    }

    public void setMapWidgetComponent(final MapWidgetComponent mapWidgetComponent) {
        m_mapWidgetComponent = mapWidgetComponent;
    }

    public void setHeaderHtml(final String headerHtml) {
        m_headerHtml = headerHtml;
    }

    public void setAlarmTable(final AlarmTable table) {
        m_alarmTable = table;
    }

    public void setNodeTable(final NodeTable table) {
        m_nodeTable = table;
    }

    private void updateWidgetView() {
        if (m_layout != null) {
            synchronized (m_layout) {
                m_layout.removeAllComponents();

                final VerticalSplitPanel bottomLayoutBar = new VerticalSplitPanel();
                bottomLayoutBar.setFirstComponent(m_mapWidgetComponent);

                // Split the screen 70% top, 30% bottom
                bottomLayoutBar.setSplitPosition(70, Unit.PERCENTAGE);
                bottomLayoutBar.setSizeFull();
                bottomLayoutBar.setSecondComponent(getTabSheet());
                m_layout.addComponent(bottomLayoutBar);
                m_layout.markAsDirty();
            }
        } else {
            LOG.warn("updateWidgetView() called, but there's no layout yet!");
        }
    }

    /**
     * Gets a {@link TabSheet} view for all widgets in this manager.
     * 
     * @return TabSheet
     */
    private Component getTabSheet() {
        // Use an absolute layout for the bottom panel
        AbsoluteLayout bottomLayout = new AbsoluteLayout();
        bottomLayout.setSizeFull();

        final TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        for(final SelectionAwareTable view : new SelectionAwareTable[] { m_alarmTable, m_nodeTable }) {
            // Icon can be null
            tabSheet.addTab(view, (view == m_alarmTable? "Alarms":"Nodes"), null);

            // If the component supports the HasExtraComponents interface, then add the extra 
            // components to the tab bar
            try {
                final Component[] extras = ((HasExtraComponents)view).getExtraComponents();
                if (extras != null && extras.length > 0) {
                    // For any extra controls, add a horizontal layout that will float
                    // on top of the right side of the tab panel
                    final HorizontalLayout extraControls = new HorizontalLayout();
                    extraControls.setHeight(32, Unit.PIXELS);
                    extraControls.setSpacing(true);

                    // Add the extra controls to the layout
                    for (final Component component : extras) {
                        extraControls.addComponent(component);
                        extraControls.setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
                    }

                    // Add a TabSheet.SelectedTabChangeListener to show or hide the extra controls
                    tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
                        @Override
                        public void selectedTabChange(final SelectedTabChangeEvent event) {
                            final TabSheet source = (TabSheet) event.getSource();
                            if (source == tabSheet) {
                                // Bizarrely enough, getSelectedTab() returns the contained
                                // Component, not the Tab itself.
                                //
                                // If the first tab was selected...
                                if (source.getSelectedTab() == view) {
                                    extraControls.setVisible(true);
                                } else {
                                    extraControls.setVisible(false);
                                }
                            }
                        }
                    });

                    // Place the extra controls on the absolute layout
                    bottomLayout.addComponent(extraControls, "top:0px;right:5px;z-index:100");
                }
            } catch (ClassCastException e) {}
            view.setSizeFull();
        }

        // Add the tabsheet to the layout
        bottomLayout.addComponent(tabSheet, "top: 0; left: 0; bottom: 0; right: 0;");

        return bottomLayout;
    }

    @Override
    protected void init(final VaadinRequest vaadinRequest) {
        m_request = vaadinRequest;
        LOG.debug("initializing");

        final VaadinApplicationContextImpl context = new VaadinApplicationContextImpl();
        final UI currentUI = UI.getCurrent();
        context.setSessionId(currentUI.getSession().getSession().getId());
        context.setUiId(currentUI.getUIId());
        context.setUsername(vaadinRequest.getRemoteUser());

        Assert.notNull(m_alarmTable);
        Assert.notNull(m_nodeTable);

        m_alarmTable.setVaadinApplicationContext(context);
        final EventProxy eventProxy = new EventProxy() {
            @Override public <T> void fireEvent(final T eventObject) {
                LOG.debug("got event: {}", eventObject);
                if (eventObject instanceof VerticesUpdateEvent) {
                    final VerticesUpdateEvent event = (VerticesUpdateEvent)eventObject;
                    final List<Integer> nodeIds = new ArrayList<Integer>();
                    for (final VertexRef ref : event.getVertexRefs()) {
                        if ("nodes".equals(ref.getNamespace()) && ref.getId() != null) {
                            nodeIds.add(Integer.valueOf(ref.getId()));
                        }
                    }
                    m_mapWidgetComponent.setSelectedNodes(nodeIds);
                    return;
                }
                LOG.warn("Unsure how to deal with event: {}", eventObject);
            }
            @Override public <T> void addPossibleEventConsumer(final T possibleEventConsumer) {
                LOG.debug("(ignoring) add consumer: {}", possibleEventConsumer);
                /* throw new UnsupportedOperationException("Not yet implemented!"); */
            }
        };

        m_alarmTable.setEventProxy(eventProxy);
        m_nodeTable.setEventProxy(eventProxy);

        createMapPanel(vaadinRequest.getParameter("search"));
        createRootLayout();
        addRefresher();
    }

    private void createMapPanel(final String searchString) {
        m_mapWidgetComponent.setSearchString(searchString);
        m_mapWidgetComponent.setSizeFull();
    }

    private void createRootLayout() {
        m_rootLayout = new VerticalLayout();
        m_rootLayout.setSizeFull();
        m_rootLayout.addStyleName("root-layout");
        setContent(m_rootLayout);
        addHeader();

        addContentLayout();
    }

    private void addContentLayout() {
        m_layout = new VerticalLayout();
        m_layout.setSizeFull();
        m_rootLayout.addComponent(m_layout);
        m_rootLayout.setExpandRatio(m_layout, 1);

        updateWidgetView();
    }

    private void addHeader() {
        if (m_headerProvider != null) {
            try {
                URL pageUrl = Page.getCurrent().getLocation().toURL();
                setHeaderHtml(m_headerProvider.getHeaderHtml(new HttpServletRequestVaadinImpl(m_request, pageUrl)));
            } catch (final Exception e) {
                LOG.error("failed to get header HTML for request " + m_request.getPathInfo(), e.getCause());
            }
        }
        if (m_headerHtml != null) {
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(m_headerHtml.getBytes());
                final CustomLayout headerLayout = new CustomLayout(is);
                headerLayout.setWidth("100%");
                headerLayout.addStyleName("onmsheader");
                m_rootLayout.addComponent(headerLayout);
            } catch (final IOException e) {
                closeQuietly(is);
                LOG.debug("failed to get header layout data", e);
            }
        }
    }

    private void closeQuietly(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (final IOException closeE) {
                LOG.debug("failed to close HTML input stream", closeE);
            }
        }
    }

    private void addRefresher() {
        final Refresher refresher = new Refresher();
        refresher.setRefreshInterval(REFRESH_INTERVAL);
        addExtension(refresher);
    }

    public void refresh() {
        m_mapWidgetComponent.refresh();
    }

    public void setFocusedNodes(final List<Integer> nodeIds) {
        for (final SelectionAwareTable view : new SelectionAwareTable[] { m_alarmTable, m_nodeTable }) {
            if (view instanceof VerticesUpdateManager.VerticesUpdateListener) {
                final VerticesUpdateManager.VerticesUpdateListener listener = (VerticesUpdateManager.VerticesUpdateListener)view;

                final Set<VertexRef> nodeSet = new HashSet<VertexRef>();
                for (final Integer nodeId : nodeIds) {
                    nodeSet.add(new DefaultVertexRef("nodes", nodeId.toString(), null));
                }

                listener.verticesUpdated(new VerticesUpdateEvent(nodeSet));

                if (view instanceof Table) {
                    final Table table = (Table)view;
                    table.refreshRowCache();
                } else {
                    LOG.error("View {} is not a table!  I can't refresh it.", view);
                }
            } else {
                LOG.error("View {} is not a vertices update listener!", view);
            }
        }
    }

    @Override
    public String toString() {
        return "NodeMapsApplication@" + hashCode();
    }

}