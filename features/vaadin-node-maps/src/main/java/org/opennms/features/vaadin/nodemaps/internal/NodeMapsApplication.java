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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.opennms.features.geocoder.GeocoderService;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.Application;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

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
public class NodeMapsApplication extends Application {

    private static final int REFRESH_INTERVAL = 5 * 60 * 1000;

    private NodeDao m_nodeDao;
    private AssetRecordDao m_assetDao;
    private AlarmDao m_alarmDao;
    private GeocoderService m_geocoderService;
    private TransactionOperations m_transaction;
    private String m_headerHtml;

    private Window m_window;
    private AbsoluteLayout m_rootLayout;

    private Logger m_log = LoggerFactory.getLogger(getClass());

    /**
     * Sets the OpenNMS Node DAO.
     * 
     * @param m_nodeDao
     *            the new OpenNMS Node DAO
     */

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setAssetRecordDao(final AssetRecordDao assetDao) {
        m_assetDao = assetDao;
    }

    public void setAlarmDao(final AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public void setGeocoderService(final GeocoderService geocoderService) {
        m_geocoderService = geocoderService;
    }

    public void setTransactionOperations(final TransactionOperations tx) {
        m_transaction = tx;
    }

    public void setHeaderHtml(final String headerHtml) {
        m_headerHtml = headerHtml;
    }

    /*
     * (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init() {
        m_log.debug("initializing");

        setTheme("opennms");

        final MapWidgetComponent mapPanel = new MapWidgetComponent();
        mapPanel.setNodeDao(m_nodeDao);
        mapPanel.setAssetRecordDao(m_assetDao);
        mapPanel.setAlarmDao(m_alarmDao);
        mapPanel.setGeocoderService(m_geocoderService);
        mapPanel.setTransactionOperation(m_transaction);
        mapPanel.setSizeFull();

        m_rootLayout = new AbsoluteLayout();
        m_rootLayout.setSizeFull();

        m_window = new Window("OpenNMS Node Maps");
        m_window.setContent(m_rootLayout);
        m_window.addParameterHandler(new ParameterHandler() {
            @Override
            public void handleParameters(final Map<String, String[]> parameters) {
                if (parameters.containsKey("search")) {
                    mapPanel.setSearchString(parameters.get("search")[0].toString());
                    /*
                    int nodeId = parseInt(parameters.get("nodeId")[0], 0);
                    if (nodeId > 0) {
                        mapPanel.setSearchString("nodeId=" + nodeId);
                    }
                    */
                }
            }
        });
        setMainWindow(m_window);

        String mapLayerPosition = "top:0px; left:0px; right:0px; bottom:0px;";
        if (m_headerHtml != null) {
            final Panel header = new Panel("header");
            header.setCaption(null);
            header.setSizeUndefined();
            header.addStyleName("onmsheader");
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(m_headerHtml.getBytes());
                final CustomLayout layout = new CustomLayout(is);
                header.setContent(layout);
                m_rootLayout.addComponent(header, "top: 0px; left: 0px; right:0px;");
                mapLayerPosition = "top:100px; left:0px; right:0px; bottom:0px;";
            } catch (final IOException e) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (final IOException closeE) {
                        VConsole.log("failed to close HTML input stream");
                        VConsole.log(closeE);
                    }
                }
                VConsole.log("failed to get header layout data");
                VConsole.log(e);
            }
        }
        m_rootLayout.addComponent(mapPanel, mapLayerPosition);

        final Refresher refresher = new Refresher();
        refresher.setRefreshInterval(REFRESH_INTERVAL);
        m_window.addComponent(refresher);
    }

    public int parseInt(String intStr, int defaultValue) {
        try {
            return Integer.parseInt(intStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
