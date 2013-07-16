/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.Icon;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Marker;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Size;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.control.PanZoomBar;
import org.gwtopenmaps.openlayers.client.event.MapMoveListener;
import org.gwtopenmaps.openlayers.client.event.MapZoomListener;
import org.gwtopenmaps.openlayers.client.event.MarkerBrowserEventListener;
import org.gwtopenmaps.openlayers.client.layer.Markers;
import org.gwtopenmaps.openlayers.client.layer.XYZ;
import org.gwtopenmaps.openlayers.client.layer.XYZOptions;
import org.gwtopenmaps.openlayers.client.popup.Popup;
import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.utils.BoundsBuilder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>OpenLayersMapPanel class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class OpenLayersMapPanel extends Composite implements MapPanel {

    private class DefaultMarkerClickHandler implements MarkerBrowserEventListener {
        
        private GWTMarkerState m_markerState;
        
        public DefaultMarkerClickHandler(GWTMarkerState markerState) {
            setMarkerState(markerState);
        }

        public void setMarkerState(GWTMarkerState markerState) {
            m_markerState = markerState;
        }

        public GWTMarkerState getMarkerState() {
            return m_markerState;
        }

        @Override
        public void onBrowserEvent(final MarkerBrowserEvent markerBrowserEvent) {
            m_eventBus.fireEvent(new GWTMarkerClickedEvent(getMarkerState()));
        }

    }

    interface OpenLayersMapPanelUiBinder extends UiBinder<Widget, OpenLayersMapPanel> {}
    private static OpenLayersMapPanelUiBinder uiBinder = GWT.create(OpenLayersMapPanelUiBinder.class);
    
    @UiField
    SimplePanel m_mapHolder;

    private MapWidget m_mapWidget;
    private org.gwtopenmaps.openlayers.client.Map m_map;
    private Markers m_markersLayer;

    private Map<String, Marker> m_markers = new HashMap<String, Marker>();
    private HandlerManager m_eventBus;

    private static final Projection PROJECTION_SPHERICAL_MERCATOR = new Projection("EPSG:900913");

    private static final Projection PROJECTION_LAT_LON = new Projection("EPSG:4326");
    
    /**
     * <p>Constructor for OpenLayersMapPanel.</p>
     *
     * @param eventBus a {@link com.google.gwt.event.shared.HandlerManager} object.
     */
    public OpenLayersMapPanel(final HandlerManager eventBus) {
        m_eventBus = eventBus;
        initWidget(uiBinder.createAndBindUi(this));

        initializeMap();

        m_map.addMapMoveListener(new MapMoveListener() {
            @Override
            public void onMapMove(final MapMoveEvent eventObject) {
                m_eventBus.fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
            }
            
        });
        m_map.addMapZoomListener(new MapZoomListener() {
            @Override
            public void onMapZoom(final MapZoomEvent eventObject) {
                m_eventBus.fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
            }
        });
    }
    
    

    /** {@inheritDoc} */
    @Override
    protected void onLoad() {
        super.onLoad();
        syncMapSizeWithParent();
    }

    /**
     * <p>initializeMap</p>
     */
    private void initializeMap() {
        final MapOptions mo = new MapOptions();
        mo.setProjection(PROJECTION_SPHERICAL_MERCATOR.getProjectionCode());
        mo.setDisplayProjection(PROJECTION_LAT_LON);
        mo.setMaxExtent(new Bounds(-180, -90, 180, 90).transform(PROJECTION_LAT_LON,PROJECTION_SPHERICAL_MERCATOR));
        m_mapWidget = new MapWidget("100%", "100%", mo);
        m_mapHolder.add(m_mapWidget);

        m_map = m_mapWidget.getMap();
        m_map.addControl(new PanZoomBar());
        m_map.addControl(new MousePosition());
        m_map.zoomTo(2);

        initializeImageError();

        XYZOptions xyzOptions = new XYZOptions();
        xyzOptions.setSphericalMercator(true);
        xyzOptions.setAttribution("Default tiles courtesy of <a href=\"http://open.mapquest.co.uk/\">MapQuest</a>");
        XYZ x = new XYZ("OpenStreetMap", getLayerUrl(), xyzOptions);
        x.setIsBaseLayer(true);
        x.setIsVisible(true);
        m_map.addLayer(x);

        m_markersLayer = new Markers("Remote Pollers");
        m_markersLayer.setIsVisible(true);
        m_markersLayer.setIsBaseLayer(false);
        m_map.addLayer(m_markersLayer);

        /*
        final LayerSwitcher switcher = new LayerSwitcher();
        m_map.addControl(switcher);
        */

        m_map.zoomToMaxExtent();

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                syncMapSizeWithParent();
            }
        });
    }

    private static native void initializeImageError() /*-{
        $wnd.OpenLayers.Util.onImageLoadError = function() {
            this.style.display = "";
            this.src = "images/nodata.png";
        };
    }-*/;

    /** {@inheritDoc} */
    @Override
    public void showLocationDetails(String name, String htmlTitle, String htmlContent) {
        final Marker marker = getMarker(name);

        if (marker != null) {
            m_map.setCenter(marker.getLonLat());
            final VerticalPanel panel = new VerticalPanel();
            panel.add(new Label(htmlTitle));
            panel.add(new HTML(htmlContent));
            Popup p = new Popup(name, marker.getLonLat(), new Size(300, 300), panel.toString(), true);
            // p.setAutoSize(true);
            p.getJSObject().setProperty("autoSize", true);
            m_map.addPopupExclusive(p);
        }
    }



    private Marker createMarker(final GWTMarkerState marker) {
        final LonLat lonLat = toLonLat(marker.getLatLng());
        final Icon icon = createIcon(marker);
        final Marker m = new Marker(lonLat, icon);
        m.addBrowserEventListener("click", new DefaultMarkerClickHandler(marker));
        return m;
    }

    private Icon createIcon(final GWTMarkerState marker) {
        return new Icon(marker.getImageURL(), new Size(32, 32), new Pixel(-16, -32));
    }

    /**
     * <p>getBounds</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    @Override
    public GWTBounds getBounds() {
        try {
            return toGWTBounds(m_map.getExtent());
        } catch (final Exception e) {
            return new GWTBounds(-180, -90, 180, 90);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setBounds(final GWTBounds b) {
        m_map.zoomToExtent(toBounds(b));
    }

    private static LonLat toLonLat(final GWTLatLng latLng) {
        final LonLat ll = new LonLat(latLng.getLongitude(), latLng.getLatitude());
        ll.transform(PROJECTION_LAT_LON.getProjectionCode(), PROJECTION_SPHERICAL_MERCATOR.getProjectionCode());
        return ll;
    }

    private static GWTBounds toGWTBounds(final Bounds fromBounds) {
        if (fromBounds == null) {
            return new GWTBounds(-180, -90, 180, 90);
        }
        final Bounds bounds = fromBounds.transform(PROJECTION_SPHERICAL_MERCATOR, PROJECTION_LAT_LON);
        BoundsBuilder bldr = new BoundsBuilder();
        bldr.extend(Math.max(-90, bounds.getLowerLeftY()), Math.max(-180, bounds.getLowerLeftX()));
        bldr.extend(Math.min(90, bounds.getUpperRightY()), Math.min(180, bounds.getUpperRightX()));
        return bldr.getBounds();
    }

    private static Bounds toBounds(final GWTBounds bounds) {
        Bounds b = null;
        if (bounds == null) {
            b = new Bounds(-180, -90, 180, 90);
        } else {
            final GWTLatLng nec = bounds.getNorthEastCorner();
            final GWTLatLng swc = bounds.getSouthWestCorner();
            b = new Bounds(swc.getLongitude(), swc.getLatitude(), nec.getLongitude(), nec.getLatitude());
        }
        return b.transform(PROJECTION_LAT_LON, PROJECTION_SPHERICAL_MERCATOR);
    }

    private void syncMapSizeWithParent() {
        m_map.updateSize();
    }

    /** {@inheritDoc} */
    @Override
    public void placeMarker(final GWTMarkerState marker) {
        Marker m = getMarker(marker.getName());

        if(m == null) {
        	m = createMarker(marker);
        	m_markers.put(marker.getName(), m);
        }
        updateMarker(m, marker);
    }

    private void updateMarker(final Marker m, final GWTMarkerState marker) {
        if (marker.isVisible()) {
            m.setImageUrl(marker.getImageURL());
            m_markersLayer.addMarker(m);
        } else {
            m_markersLayer.removeMarker(m);
        }
    }

    private Marker getMarker(final String name) {
        return m_markers.get(name);
    }

    private native String getLayerUrl() /*-{
        return $wnd.openlayersUrl;
    }-*/;

    /**
     * <p>getWidget</p>
     *
     * @return a {@link com.google.gwt.user.client.ui.Widget} object.
     */
    @Override
    public Widget getWidget() {
        return this;
    }

    /** {@inheritDoc} */
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return addDomHandler(handler, DoubleClickEvent.getType());
    }

    /** {@inheritDoc} */
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }
}
