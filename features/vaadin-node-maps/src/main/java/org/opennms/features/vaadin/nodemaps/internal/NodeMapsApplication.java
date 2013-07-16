/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.nodemaps.internal;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.opennms.web.api.OnmsHeaderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
        "http://maps.google.com/maps/api/js?sensor=false",
        "http://cdn.leafletjs.com/leaflet-0.5.1/leaflet-src.js",
        "gwt/public/openlayers/OpenLayers.js",
        "gwt/public/markercluster/leaflet.markercluster-src.js"
        
})
@StyleSheet({
        "gwt/public/markercluster/MarkerCluster.css",
        "gwt/public/markercluster/MarkerCluster.Default.css",
        "gwt/public/node-maps.css"})
public class NodeMapsApplication extends UI {

    private static final Logger LOG = LoggerFactory.getLogger(NodeMapsApplication.class);
    private static final int REFRESH_INTERVAL = 5 * 60 * 1000;
    private VerticalLayout m_rootLayout;

    private Logger m_log = LoggerFactory.getLogger(getClass());

    private MapWidgetComponent m_mapWidgetComponent;
    private OnmsHeaderProvider m_headerProvider;
    private String m_headerHtml;

    public void setHeaderProvider(final OnmsHeaderProvider headerProvider) {
        m_headerProvider = headerProvider;
    }

    public void setMapWidgetComponent(MapWidgetComponent m_mapWidgetComponent) {
        this.m_mapWidgetComponent = m_mapWidgetComponent;
    }

    public void setHeaderHtml(final String headerHtml) {
        m_headerHtml = headerHtml;

        /**
         * Added some magic to hide search controls and header if displayed inside an iframe
         */
        m_headerHtml += "<script type='text/javascript'>if (window.location != window.parent.location) { document.getElementById('header').style.display = 'none'; var style = document.createElement(\"style\"); style.type = 'text/css'; style.innerHTML = '.leaflet-control-container { display: none; }'; document.body.appendChild(style); }</script>";
    }


    @Override
    protected void init(VaadinRequest vaadinRequest) {
        m_log.debug("initializing");
        createMapPanel(vaadinRequest.getParameter("search"));
        createRootLayout(vaadinRequest);
        addRefresher();
    }

    private void createMapPanel(String searchString) {
        m_mapWidgetComponent.setSearchString(searchString);
        m_mapWidgetComponent.setSizeFull();
    }

    private void createRootLayout(VaadinRequest request) {
        m_rootLayout = new VerticalLayout();
        m_rootLayout.setSizeFull();
        setContent(m_rootLayout);

        addHeader(request);
        m_rootLayout.addComponent(m_mapWidgetComponent);
        m_rootLayout.setExpandRatio(m_mapWidgetComponent, 1.0f);
    }

    private void addHeader(VaadinRequest request) {
        if (m_headerProvider != null) {
            try {
                setHeaderHtml(m_headerProvider.getHeaderHtml(new HttpServletRequestVaadinImpl(request)));
            } catch (final Exception e) {
                LOG.warn("failed to get header HTML for request " + request.getPathInfo(), e.getCause());

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
                try {
                    is.close();
                } catch (final IOException closeE) {
                    m_log.debug("failed to close HTML input stream", closeE);
                }
                m_log.debug("failed to get header layout data", e);
            }
        }
    }

    private void addRefresher() {
        final Refresher refresher = new Refresher();
        refresher.setRefreshInterval(REFRESH_INTERVAL);
        addExtension(refresher);
    }
}
