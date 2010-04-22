package org.opennms.features.poller.remote.gwt.client;

import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toGWTBounds;
import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toLatLng;
import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toLatLngBounds;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.event.MapMoveEndHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class GoogleMapsPanel extends Composite implements MapPanel {

    private static GoogleMapsPanelUiBinder uiBinder = GWT.create(GoogleMapsPanelUiBinder.class);

    interface GoogleMapsPanelUiBinder extends
            UiBinder<Widget, GoogleMapsPanel> {
    }
    
    @UiField
    MapWidget m_mapWidget;
    
    private Map<String, Marker> m_markers = new HashMap<String, Marker>();
    private HandlerManager m_eventBus;

    public GoogleMapsPanel(final HandlerManager eventBus) {
        m_eventBus = eventBus;
        initWidget(uiBinder.createAndBindUi(this));
        
        initializeMapPanel();
        
        m_mapWidget.addMapMoveEndHandler(new MapMoveEndHandler() {

            public void onMoveEnd(MapMoveEndEvent event) {
                m_eventBus.fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
            }
            
        });
    }
    
    private MapWidget getMapWidget() {
        return m_mapWidget;
    }

    public void showLocationDetails(String name, String htmlTitle, String htmlContent) {
        //TODO: You are here we need to implements a String/HTML for content window
        final Marker m = getMarker(name);

        getMapWidget().setCenter(m.getLatLng());
        if (m != null) {
            //InfoWindowContent content = getInfoWindowForLocation(location);
            InfoWindowContent content = new InfoWindowContent(name + " Wahoo!");
            getMapWidget().getInfoWindow().open(m, content);
        }
    }

    private Marker getMarker(String name) {
        return m_markers.get(name);
    }
    
    private void setMarker(Marker m, GWTMarker marker) {
        m_markers.put(marker.getName(), m);
    }

    public GWTBounds getBounds() {
        return toGWTBounds(getMapWidget().getBounds());
    }

    public void setBounds(GWTBounds b) {
        LatLngBounds bounds = toLatLngBounds(b);
    	getMapWidget().setCenter(bounds.getCenter(), getMapWidget().getBoundsZoomLevel(bounds));
    }
    
    private void initializeMapPanel() {
        getMapWidget().setSize("100%", "100%");
        getMapWidget().setUIToDefault();
        getMapWidget().addControl(new LargeMapControl());
        getMapWidget().setContinuousZoom(true);
        getMapWidget().setScrollWheelZoomEnabled(true);
      
        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(final ResizeEvent resizeEvent) {
                if (getMapWidget() != null) {
                    getMapWidget().checkResizeAndCenter();
                }
            }
        });
    }

    private void addOverlay(final Marker newMarker) {
        getMapWidget().addOverlay(newMarker);
    }

    private Marker createMarker(final GWTMarker marker) {
        final Icon icon = Icon.newInstance();
        icon.setIconSize(Size.newInstance(32, 32));
        icon.setIconAnchor(Point.newInstance(16, 32));
        String markerImageURL = marker.getImageURL();
        icon.setImageURL(markerImageURL);
        
        final MarkerOptions markerOptions = MarkerOptions.newInstance();
        markerOptions.setAutoPan(true);
        markerOptions.setClickable(true);
        markerOptions.setTitle(marker.getName());
        markerOptions.setIcon(icon);
        
        Marker m = new Marker(toLatLng(marker.getLatLng()), markerOptions);
        m.addMarkerClickHandler(new DefaultMarkerClickHandler(marker));
        return m;
    }

    public void placeMarker(final GWTMarker marker) {
        
        Marker m = getMarker(marker.getName());
        if (m == null) {
            addMarker(marker);
        } else {
        	updateMarker(m, marker);
        }
        
    }

    private void addMarker(GWTMarker marker) {
        Marker m = createMarker(marker);
        setMarker(m, marker);
        addOverlay(m);
    }

    private void updateMarker(Marker m, GWTMarker marker) {
        m.setImage(marker.getImageURL());
    }

    private final class DefaultMarkerClickHandler implements MarkerClickHandler {
        private final GWTMarker m_marker;
    
        DefaultMarkerClickHandler(GWTMarker marker) {
            m_marker = marker;
        }
    
        public void onClick(final MarkerClickEvent mke) {
            //showLocationDetails(m_marker);
            m_eventBus.fireEvent(new GWTMarkerClickedEvent(m_marker));
        }
    }
    
    public Widget getWidget() {
        return this;
    }

}

    
