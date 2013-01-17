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
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
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

    /** The Constant GEOCODER_REQUEST_PREFIX_FOR_XML. */
    private static final String GEOCODER_REQUEST_PREFIX_FOR_XML = "http://maps.google.com/maps/api/geocode/xml";

    /** The Constant NODE_CLASS. */
    private static final String NODE_STYLE = "nodeCircle";

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

    /**
     * Gets the point from address.
     * <p>
     * This method will use the Google API to retrieve the geolocation
     * coordinates for a given address.
     * </p>
     * <p>
     * Source inspired on:<br/>
     * http://code.google.com/p/gmaps-samples/source/browse/trunk/geocoder/
     * java/GeocodingSample.java?r=2476
     * </p>
     * 
     * @param address the address
     * @return the point from address
     */
    /*
     * TODO We can create a provisioning adapter in order to populate the node
     * assets with the coordinates based on the current address configured on
     * the database.
     */
    /*
    private PointVector getPointFromAddress(OnmsNode onmsNode) {
        final String address = getNodeAddress(onmsNode);
        if (address == null) {
            LogUtils.debugf(this, "Node %s does not contain any address information.", onmsNode.getLabel());
            return null;
        }
        try {
            LogUtils.debugf(this, "Getting geolocation for node %s, located on %s", onmsNode.getLabel(), address);
            URL url = new URL(GEOCODER_REQUEST_PREFIX_FOR_XML + "?address=" + URLEncoder.encode(address, "UTF-8") + "&sensor=false");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Document geocoderResultDocument = null;
            try {
                conn.connect();
                InputSource geocoderResultInputSource = new InputSource(conn.getInputStream());
                geocoderResultDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(geocoderResultInputSource);
            } finally {
                conn.disconnect();
            }
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList resultNodeList = null;
            resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/*", geocoderResultDocument, XPathConstants.NODESET);
            double lat = Double.NaN;
            double lng = Double.NaN;
            for (int i = 0; i < resultNodeList.getLength(); ++i) {
                Node node = resultNodeList.item(i);
                if ("lat".equals(node.getNodeName())) {
                    lat = Double.parseDouble(node.getTextContent());
                }
                if ("lng".equals(node.getNodeName())) {
                    lng = Double.parseDouble(node.getTextContent());
                }
            }
            if (lat == Double.NaN || lng == Double.NaN) {
                LogUtils.warnf(this, "Couldn't find the coordinates for node %s located on %s", onmsNode.getLabel(), address);
                return null;
            }
            LogUtils.infof(this, "Found geolocation coordinates for node %s at (%s, %s)", onmsNode.getLabel(), lng, lat);
            return new PointVector(lng, lat);
        } catch (Exception e) {
            LogUtils.errorf(this, e, "An error occured when trying to get coordinates for %s.", address);
        }
        return null;
    }
    */

    /**
     * Creates the node layer.
     * 
     * @param map the map
     * @return the vector layer
     */
    /*
    private VectorLayer createNodeLayer(final OnmsOpenLayersMap map) {
        // Creating Vecctor Layers
        final VectorLayer nodeLayer = new VectorLayer();
        nodeLayer.setDisplayName("Nodes Layer");
        nodeLayer.setSelectionMode(SelectionMode.SIMPLE);

        // Configuring Node Styles
        Style nodeCircle = new Style(NODE_STYLE);
        nodeCircle.setPointRadius(20);
        nodeCircle.setFillColor("#000");
        nodeCircle.setFillOpacity(0.5);
        nodeLayer.setStyleMap(new StyleMap(nodeCircle));

        // Configuring Layer Listeners to display a PopUp for Node Vectors.
        nodeLayer.addListener(new VectorSelectedListener() {
            public void vectorSelected(VectorSelectedEvent event) {
                if (event.getVector() instanceof PointVector) {
                    final PointVector v = (PointVector) event.getVector();
                    final Popup popup = new Popup(v.getPoint().getLon(), v.getPoint().getLat(), v.getDescription());
                    popup.addListener(new CloseListener() {
                        public void onClose(CloseEvent event) {
                            map.removeComponent(popup);
                            nodeLayer.getSelectedVector().setRenderIntent(NODE_STYLE);
                            nodeLayer.setSelectedVector(null);
                        }
                    });
                    map.addPopup(popup);
                }
            }
        });

        return nodeLayer;
    }
	*/
    
    /**
     * Gets the node description.
     * 
     * @param node the OpenNMS Node
     * @return the node description
     */
    private String getNodeDescription(OnmsNode node) {
        StringBuffer sb = new StringBuffer(node.getLabel());
        sb.append("<br/>foreignSource=").append(node.getForeignSource());
        sb.append("<br/>foreignId=").append(node.getForeignId());
        return sb.toString();
    }

    /**
     * Gets the node address.
     * 
     * @param node the OpenNMS Node
     * @return the node address
     */
    private String getNodeAddress(final OnmsNode node) {
        if (node == null || node.getAssetRecord() == null) return null;
        final OnmsAssetRecord assetRecord = node.getAssetRecord();
        final OnmsGeolocation geolocation = assetRecord.getGeolocation();

        if (geolocation == null) return "";
        return geolocation.asAddressString();
    }

}
