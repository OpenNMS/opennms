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

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.vaadin.nodemaps.ui.OpenlayersWidgetComponent;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;

import com.vaadin.Application;
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

    /** The OpenNMS Node DAO. */
    private NodeDao nodeDao;

    /**
     * Sets the OpenNMS Node DAO.
     * 
     * @param nodeDao the new OpenNMS Node DAO
     */

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    /**
     * Gets the OpenNMS Node DAO.
     * 
     * @return the OpenNMS Node DAO
     */
    public NodeDao getNodeDao() {
        if (nodeDao == null) {
            LogUtils.infof(this, "Initializing NodeDao");
            nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
        }
        return nodeDao;
    }

    /*
     * (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init() {
        final OpenlayersWidgetComponent openlayers = new OpenlayersWidgetComponent();
        openlayers.setSizeFull();

        final Window mainWindow = new Window("OpenNMS Node Maps", openlayers);
        setMainWindow(mainWindow);
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
    private VectorLayer createNodeLayer(final OpenLayersMap map) {
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

        final StringBuffer sb = new StringBuffer();

        if (assetRecord.getAddress1() != null) {
            sb.append(assetRecord.getAddress1());
            if (assetRecord.getAddress2() != null) {
                sb.append(" ").append(assetRecord.getAddress2());
            }
        }

        if (sb.length() > 0 && assetRecord.getCity() != null) sb.append(", ").append(assetRecord.getCity());
        if (sb.length() > 0 && assetRecord.getState() != null) sb.append(", ").append(assetRecord.getState());
        if (sb.length() > 0 && assetRecord.getZip() != null) sb.append(" ").append(assetRecord.getZip());
        return sb.toString();
    }

}
