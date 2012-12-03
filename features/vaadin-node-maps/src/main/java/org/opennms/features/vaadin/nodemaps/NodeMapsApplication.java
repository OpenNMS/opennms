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

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.NodeDao;

import org.vaadin.vol.GoogleStreetMapLayer;
import org.vaadin.vol.OpenLayersMap;
import org.vaadin.vol.PointVector;
import org.vaadin.vol.Popup;
import org.vaadin.vol.Popup.CloseEvent;
import org.vaadin.vol.Popup.CloseListener;
import org.vaadin.vol.Style;
import org.vaadin.vol.StyleMap;
import org.vaadin.vol.VectorLayer;
import org.vaadin.vol.VectorLayer.SelectionMode;
import org.vaadin.vol.VectorLayer.VectorSelectedListener;
import org.vaadin.vol.VectorLayer.VectorSelectedEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.vaadin.Application;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The Class Node Maps Application.
 * 
 * <p>PointVectors are used instead of Markers because the idea is to use the Cluster Strategy feature.</p>
 * 
 * <p>Here are some samples:</p>
 * <ul>
 * <li>http://openlayers.org/dev/examples/strategy-cluster.html</li>
 * <li>http://developers.cloudmade.com/projects/web-maps-api/examples/marker-clustering</li>
 * </ul>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
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

    /* (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init() {
        // Initialize Vaadin Main Window
        final VerticalLayout layout = new VerticalLayout();
        final Window mainWindow = new Window("OpenNMS Node Maps", layout);
        setMainWindow(mainWindow);

        // Creating OpenLayers Map with Google Street Map Layer
        final OpenLayersMap map = new OpenLayersMap();
        map.setImmediate(true); // Update extent and zoom to server as they change
        map.setSizeFull();
        map.addLayer(new GoogleStreetMapLayer());
        VectorLayer nodeLayer = createNodeLayer(map);
        map.addLayer(nodeLayer);

        // Define Map Center
        // FIXME How to determinate the best center position according with the markers ?
        final PointVector office = getPointFromAddress("220 Chatham Business Drive, Pittsboro, NC, 27312");
        if (office != null) {
            map.setCenter(office.getPoint().getLon(), office.getPoint().getLat());
            map.setZoom(15);
        }

        // Populating Node Layer
        office.setDescription("OpenNMS Office");
        office.setRenderIntent(NODE_STYLE);
        nodeLayer.addVector(office);

        // Updating Vaadin Layout
        layout.setSizeFull();
        layout.addComponent(map);
        layout.setExpandRatio(map, 1);
    }

    /**
     * Gets the point from address.
     * <p>This method will use the Google API to retrieve the geolocation coordinates for a given address.</p>
     * <p>Source inspired on:<br/> http://code.google.com/p/gmaps-samples/source/browse/trunk/geocoder/java/GeocodingSample.java?r=2476</p>
     *
     * @param address the address
     * @return the point from address
     */
    /*
     * TODO We can create a provisioning adapter in order to populate the node assets with the coordinates based on the current
     *      address configured on the database.
     */
    private PointVector getPointFromAddress(String address) {
        try {
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
            for(int i=0; i<resultNodeList.getLength(); ++i) {
                Node node = resultNodeList.item(i);
                if ("lat".equals(node.getNodeName())) lat = Double.parseDouble(node.getTextContent());
                if ("lng".equals(node.getNodeName())) lng = Double.parseDouble(node.getTextContent());
            }
            if (lat == Double.NaN || lng == Double.NaN) {
                LogUtils.warnf(this, "Couldn't find the coordinates for: %s", address);
                return null;
            }
            return new PointVector(lng, lat);
        } catch (Exception e) {
            LogUtils.errorf(this, e, "An error occured when trying to get coordinates for %s.", address);
        }
        return null;
    }

    /**
     * Creates the node layer.
     *
     * @param map the map
     * @return the vector layer
     */
    private VectorLayer createNodeLayer(final OpenLayersMap map) {
        // Creating Vecctor Layers
        final VectorLayer nodeLayer = new VectorLayer();
        nodeLayer.setDisplayName("Nodes Layer");
        nodeLayer.setSelectionMode(SelectionMode.SIMPLE);

        // Configuring Node Styles
        Style nodeCircle = new Style(NODE_STYLE);
        nodeCircle.setPointRadius(30);
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

}
