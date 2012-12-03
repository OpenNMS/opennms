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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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
import com.vaadin.ui.themes.Runo;

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

    private static final String GEOCODER_REQUEST_PREFIX_FOR_XML = "http://maps.google.com/maps/api/geocode/xml";

    /** The OpenNMS Node DAO. */
    private NodeDao nodeDao;

    private final Map<PointVector,Popup> popups = new HashMap<PointVector,Popup>();

    /**
     * Sets the OpenNMS Node DAO.
     *
     * @param nodeDao the new OpenNMS Node DAO
     */

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    /* (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init() {
        // Verify that NodeDao is not empty
        if (nodeDao == null)
            throw new RuntimeException("nodeDao cannot be null.");

        // Initialize Vaadin Main Window
        setTheme(Runo.THEME_NAME);
        final VerticalLayout layout = new VerticalLayout();
        final Window mainWindow = new Window("OpenNMS Node Maps", layout);
        setMainWindow(mainWindow);

        // Creating OpenLayers Map with Google Street Map Layer
        final OpenLayersMap map = new OpenLayersMap();
        map.setImmediate(true); // Update extent and zoom to server as they change
        map.setSizeFull();
        map.addLayer(new GoogleStreetMapLayer());

        final PointVector office = getPointFromAddress("220 Chatham Business Drive, Pittsboro, NC, 27312");
        if (office != null) {
            // FIXME How to determinate the best center position according with the markers ?
            map.setCenter(office.getPoint().getLat(), office.getPoint().getLon());
            map.setZoom(15);
        }

        // Creating Vecctor Layers
        VectorLayer vectorLayer = new VectorLayer();
        vectorLayer.setSelectionMode(SelectionMode.SIMPLE);

        // Configuring Node Styles
        Style nodeCircle = new Style();
        nodeCircle.setPointRadiusByAttribute("25");
        nodeCircle.setFillColor("#000");
        nodeCircle.setFillOpacity(0.5);
        StyleMap stylemap = new StyleMap();
        stylemap.setStyle("nodeCircle", nodeCircle);
        vectorLayer.setStyleMap(stylemap);

        // Configuring Layer Listeners for Node Popups
        // TODO Is this the best way to do that ?
        // The reason for that is because vectors don't support popups as markers
        vectorLayer.addListener(new VectorSelectedListener() {
            public void vectorSelected(VectorSelectedEvent event) {
                map.addPopup(popups.get(event.getVector()));
            }
        });

        // Adding Sample Point to the map
        addPoint(map, vectorLayer, office, "OpenNMS Office");

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
    public PointVector getPointFromAddress(String address) {
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
            return new PointVector(lat, lng);
        } catch (Exception e) {
            LogUtils.errorf(this, e, "An error occured when trying to get coordinates for %s.", address);
        }
        return null;
    }

    /**
     * Adds the point.
     *
     * @param map the map
     * @param vectorLayer the vector layer
     * @param vector the vector
     * @param content the content
     */
    private void addPoint(final OpenLayersMap map, final VectorLayer vectorLayer, final PointVector vector, final String content) {
        vector.setRenderIntent("nodeCircle");
        vectorLayer.addVector(vector);

        final Popup popup = new Popup(vector.getPoint().getLon(), vector.getPoint().getLat(), content);
        popup.addListener(new CloseListener() {
            public void onClose(CloseEvent event) {
                map.removeComponent(popup);
                vectorLayer.getSelectedVector().setRenderIntent("nodeCircle");
                vectorLayer.setSelectedVector(null);
            }
        });
        popups.put(vector, popup);
    }

}
