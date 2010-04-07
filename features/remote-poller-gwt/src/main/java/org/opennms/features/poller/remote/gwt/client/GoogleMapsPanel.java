package org.opennms.features.poller.remote.gwt.client;

import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toGWTBounds;
import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toLatLng;
import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toLatLngBounds;

import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEventHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.Control;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.event.MapMoveEndHandler;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class GoogleMapsPanel extends Composite {

    private static GoogleMapsPanelUiBinder uiBinder = GWT.create(GoogleMapsPanelUiBinder.class);

    interface GoogleMapsPanelUiBinder extends
            UiBinder<Widget, GoogleMapsPanel> {
    }
    
    @UiField
    MapWidget m_mapWidget;
    
    HandlerManager m_handlerManager;

    public GoogleMapsPanel() {
        m_handlerManager = new HandlerManager(this);
        initWidget(uiBinder.createAndBindUi(this));
        
        initializeMapPanel();
        
        m_mapWidget.addMapMoveEndHandler(new MapMoveEndHandler() {

            public void onMoveEnd(MapMoveEndEvent event) {
                m_handlerManager.fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
            }
            
        });
    }
    
    public MapWidget getMapWidget() {
        return m_mapWidget;
    }

    void showLocationDetails(final GoogleMapsLocation location) {
        final Marker m = location.getMarker();
        final GWTLatLng latLng = location.getLocationInfo().getLatLng();
        getMapWidget().setCenter(toLatLng(latLng));
        if (m != null) {
            //InfoWindowContent content = getInfoWindowForLocation(location);
            InfoWindowContent content = new InfoWindowContent(location.getLocationInfo().getName() + " Wahoo!");
            getMapWidget().getInfoWindow().open(m, content);
        }
    }

    GWTBounds getBounds() {
        return toGWTBounds(getMapWidget().getBounds());
    }

    void setBounds(GWTBounds b) {
        LatLngBounds bounds = toLatLngBounds(b);
    	getMapWidget().setCenter(bounds.getCenter(), getMapWidget().getBoundsZoomLevel(bounds));
    }
    
    public void addMapPanelBoundsChangedEventHandler( MapPanelBoundsChangedEventHandler handler) {
        m_handlerManager.addHandler(MapPanelBoundsChangedEvent.TYPE, handler);
    }
    
    public void addControl(Control control) {
        getMapWidget().addControl(control);
    }

    void initializeMapPanel() {
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

    void addOverlay(final Marker newMarker) {
        getMapWidget().addOverlay(newMarker);
    }

    public void removeOverlay(Marker marker) {
        getMapWidget().removeOverlay(marker);
    }

}
