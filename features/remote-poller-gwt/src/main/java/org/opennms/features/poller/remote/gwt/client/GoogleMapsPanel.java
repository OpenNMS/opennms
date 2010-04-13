package org.opennms.features.poller.remote.gwt.client;

import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toGWTBounds;
import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toLatLng;
import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toLatLngBounds;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEventHandler;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
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

    public GoogleMapsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        initializeMapPanel();
        
        m_mapWidget.addMapMoveEndHandler(new MapMoveEndHandler() {

            public void onMoveEnd(MapMoveEndEvent event) {
                fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
            }
            
        });
    }
    
    private MapWidget getMapWidget() {
        return m_mapWidget;
    }

    public void showLocationDetails(final Location location) {
        final Marker m = getMarker(location);
        final GWTLatLng latLng = location.getLocationInfo().getLatLng();
        getMapWidget().setCenter(toLatLng(latLng));
        if (m != null) {
            //InfoWindowContent content = getInfoWindowForLocation(location);
            InfoWindowContent content = new InfoWindowContent(location.getLocationInfo().getName() + " Wahoo!");
            getMapWidget().getInfoWindow().open(m, content);
        }
    }

    private Marker getMarker(final Location location) {
        return m_markers.get(location.getLocationInfo().getName());
    }
    
    private void setMarker(final Location location, Marker m) {
        m_markers.put(location.getLocationInfo().getName(), m);
    }

    public GWTBounds getBounds() {
        return toGWTBounds(getMapWidget().getBounds());
    }

    public void setBounds(GWTBounds b) {
        LatLngBounds bounds = toLatLngBounds(b);
    	getMapWidget().setCenter(bounds.getCenter(), getMapWidget().getBoundsZoomLevel(bounds));
    }
    
    public void addMapPanelBoundsChangedEventHandler( MapPanelBoundsChangedEventHandler handler) {
        addHandler(handler, MapPanelBoundsChangedEvent.TYPE);
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

    private Marker createMarker(final Location location) {
        final LocationInfo locationInfo = location.getLocationInfo();
        
        final Icon icon = Icon.newInstance();
        icon.setIconSize(Size.newInstance(32, 32));
        icon.setIconAnchor(Point.newInstance(16, 32));
        icon.setImageURL(locationInfo.getImageURL());
        
        final MarkerOptions markerOptions = MarkerOptions.newInstance();
        markerOptions.setAutoPan(true);
        markerOptions.setClickable(true);
        markerOptions.setTitle(locationInfo.getName());
        markerOptions.setIcon(icon);
        
        Marker m = new Marker(toLatLng(locationInfo.getLatLng()), markerOptions);
        m.addMarkerClickHandler(new DefaultMarkerClickHandler(location));
        return m;
    }

    public void placeMarker(final Location location) {
        Marker m = getMarker(location);
        if (m == null) {
            addMarker(location);
        } else {
        	updateMarker(location, m);
        }
        
    }

    private void addMarker(final Location location) {
        Marker m = createMarker(location);
        setMarker(location, m);
        addOverlay(m);
    }

    private void updateMarker(final Location location, Marker m) {
        m.setImage(location.getLocationInfo().getImageURL());
    }

    private final class DefaultMarkerClickHandler implements MarkerClickHandler {
        private final Location m_location;
    
        DefaultMarkerClickHandler(final Location location) {
            m_location = location;
        }
    
        public void onClick(final MarkerClickEvent mke) {
            showLocationDetails(m_location);
        }
    }
    
    public Widget getWidget() {
        return this;
    }

}

    
