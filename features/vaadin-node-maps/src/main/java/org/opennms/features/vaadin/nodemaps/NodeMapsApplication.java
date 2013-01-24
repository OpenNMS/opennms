/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.nodemaps;

import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.vaadin.nodemaps.ui.OpenlayersWidgetComponent;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.ui.AbsoluteLayout;
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

    private NodeDao m_nodeDao;

    private Window m_window;

    private AbsoluteLayout m_rootLayout;

    private AssetRecordDao m_assetDao;

    private GeocoderService m_geocoderService;

    private Logger m_log = LoggerFactory.getLogger(getClass());

    /**
     * Sets the OpenNMS Node DAO.
     * 
     * @param m_nodeDao the new OpenNMS Node DAO
     */

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setAssetRecordDao(final AssetRecordDao assetDao) {
        m_assetDao = assetDao;
    }

    public void setGeocoderService(final GeocoderService geocoderService) {
        m_geocoderService = geocoderService;
    }

    /*
     * (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init() {
        m_log.debug("initializing");

        final OpenlayersWidgetComponent openlayers = new OpenlayersWidgetComponent();
        openlayers.setNodeDao(m_nodeDao);
        openlayers.setAssetRecordDao(m_assetDao);
        openlayers.setGeocoderService(m_geocoderService);
        openlayers.setSizeFull();

        m_rootLayout = new AbsoluteLayout();
        m_rootLayout.setSizeFull();

        m_window = new Window("OpenNMS Node Maps");
        m_window.setContent(m_rootLayout);
        setMainWindow(m_window);

        m_rootLayout.addComponent(openlayers, "top: 0px; left: 0px; right:0px; bottom:0px;");
    }

}
