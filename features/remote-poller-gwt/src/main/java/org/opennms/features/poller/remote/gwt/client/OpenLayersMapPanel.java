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
import org.gwtopenmaps.openlayers.client.Size;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.control.PanZoomBar;
import org.gwtopenmaps.openlayers.client.event.MapMoveListener;
import org.gwtopenmaps.openlayers.client.event.MapZoomListener;
import org.gwtopenmaps.openlayers.client.event.MarkerBrowserEventListener;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.gwtopenmaps.openlayers.client.layer.Markers;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;
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
            public void onMapMove(final MapMoveEvent eventObject) {
                m_eventBus.fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
            }
            
        });
        m_map.addMapZoomListener(new MapZoomListener() {
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
        mo.setProjection("EPSG:4326");
        m_mapWidget = new MapWidget("100%", "100%", mo);
        m_mapHolder.add(m_mapWidget);

        m_map = m_mapWidget.getMap();
        m_map.addControl(new PanZoomBar());
        m_map.addControl(new MousePosition());
        m_map.zoomTo(2);

        initializeImageError();

        WMSParams layerParams = null;
        WMSOptions layerOptions = null;

        layerOptions = new WMSOptions();
        layerOptions.setWrapDateLine(true);
        layerParams = new WMSParams();
        layerParams.setLayers(getLayerName());
        Layer layer = new WMS("OpenStreetMaps", getLayerUrl(), layerParams, layerOptions);
        layer.setIsBaseLayer(true);
        layer.setIsVisible(true);
        m_map.addLayer(layer);

        layerOptions = new WMSOptions();
        layerOptions.setWrapDateLine(true);
        layerParams = new WMSParams();
        layerParams.setLayers("basic");
        layer = new WMS("MetaCarta (Basic)", new String[] {"http://labs.metacarta.com/wms-c/Basic.py?", "http://t2.labs.metacarta.com/wms-c/Basic.py?", "http://t1.labs.metacarta.com/wms-c/Basic.py?" }, layerParams, layerOptions);
        layer.setIsBaseLayer(true);
        layer.setIsVisible(false);
        m_map.addLayer(layer);

        layerOptions = new WMSOptions();
        layerOptions.setWrapDateLine(true);
        layerParams = new WMSParams();
        layerParams.setLayers("satellite");
        layer = new WMS("MetaCarta (Satellite)", new String[] {"http://labs.metacarta.com/wms-c/Basic.py?", "http://t2.labs.metacarta.com/wms-c/Basic.py?", "http://t1.labs.metacarta.com/wms-c/Basic.py?" }, layerParams, layerOptions);
        layer.setIsBaseLayer(true);
        layer.setIsVisible(false);
        m_map.addLayer(layer);

        m_markersLayer = new Markers("Remote Pollers");
        m_markersLayer.setIsVisible(true);
        m_markersLayer.setIsBaseLayer(false);
        m_map.addLayer(m_markersLayer);

        final LayerSwitcher switcher = new LayerSwitcher();
        m_map.addControl(switcher);

        m_map.zoomToMaxExtent();

        Window.addResizeHandler(new ResizeHandler() {
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
    public void showLocationDetails(String name, String htmlTitle, String htmlContent) {
    	final Marker marker = getMarker(name);

    	m_map.setCenter(marker.getLonLat());
    	if (marker != null) {
            final VerticalPanel panel = new VerticalPanel();
            panel.add(new Label(htmlTitle));
            panel.add(new HTML(htmlContent));
            Popup p = new Popup(name, marker.getLonLat(), new Size(300, 300), panel.toString(), true);
            p.setAutoSize(true);
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
    public GWTBounds getBounds() {
        try {
            return toGWTBounds(m_map.getExtent());
        } catch (final Exception e) {
            return new GWTBounds(-180, -90, 180, 90);
        }
    }

    /** {@inheritDoc} */
    public void setBounds(final GWTBounds b) {
        m_map.zoomToExtent(toBounds(b));
    }

    private static LonLat toLonLat(final GWTLatLng latLng) {
        return new LonLat(latLng.getLongitude(), latLng.getLatitude());
    }

    private static GWTBounds toGWTBounds(final Bounds bounds) {
        if (bounds == null) {
            return new GWTBounds(-180, -90, 180, 90);
        }
        BoundsBuilder bldr = new BoundsBuilder();
        bldr.extend(bounds.getLowerLeftX(), bounds.getLowerLeftY());
        bldr.extend(bounds.getUpperRightX(), bounds.getUpperRightY());
        return bldr.getBounds();
    }

    private static Bounds toBounds(final GWTBounds bounds) {
        final GWTLatLng nec = bounds.getNorthEastCorner();
        final GWTLatLng swc = bounds.getSouthWestCorner();
        return new Bounds(swc.getLongitude(), swc.getLatitude(), nec.getLongitude(), nec.getLatitude());
    }

    private void syncMapSizeWithParent() {
        m_map.updateSize();
    }

    /** {@inheritDoc} */
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

    private native String getLayerName() /*-{
        return $wnd.openlayersLayer;
    }-*/;

    /**
     * <p>getWidget</p>
     *
     * @return a {@link com.google.gwt.user.client.ui.Widget} object.
     */
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
