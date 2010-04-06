package org.opennms.features.poller.remote.gwt.client;

import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.getInfoWindowForLocation;
import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toGWTBounds;
import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toLatLng;
import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toLatLngBounds;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class GoogleMapsPanel extends Composite {

    private static GoogleMapsPanelUiBinder uiBinder = GWT.create(GoogleMapsPanelUiBinder.class);

    interface GoogleMapsPanelUiBinder extends
            UiBinder<Widget, GoogleMapsPanel> {
    }
    
    @UiField
    MapWidget m_mapWidget;

    public GoogleMapsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public MapWidget getMapWidget() {
        return m_mapWidget;
    }

    void showLocationDetails(final GoogleMapsLocation location) {
        final Marker m = location.getMarker();
        final GWTLatLng latLng = location.getLatLng();
        getMapWidget().setCenter(toLatLng(latLng));
        if (m != null) {
            InfoWindowContent content = getInfoWindowForLocation(location);
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

}
