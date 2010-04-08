package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwtmapquest.transaction.MQAIcon;
import com.googlecode.gwtmapquest.transaction.MQAInfoWindow;
import com.googlecode.gwtmapquest.transaction.MQALargeZoomControl;
import com.googlecode.gwtmapquest.transaction.MQALatLng;
import com.googlecode.gwtmapquest.transaction.MQAPoi;
import com.googlecode.gwtmapquest.transaction.MQAPoint;
import com.googlecode.gwtmapquest.transaction.MQASize;
import com.googlecode.gwtmapquest.transaction.MQATileMap;

public class MapQuestMapPanel extends Composite {

    private static MapQuestMapPanelUiBinder uiBinder = GWT.create(MapQuestMapPanelUiBinder.class);
    
    @UiField
    SimplePanel m_mapHolder;
    
    private MQATileMap m_map;
    
    interface MapQuestMapPanelUiBinder extends UiBinder<Widget, MapQuestMapPanel> {}

    public MapQuestMapPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        setMapWidget(MQATileMap.newInstance(m_mapHolder.getElement()));
        
        initializeMap();
    }

    private void setMapWidget(MQATileMap map) {
        m_map = map;
    }

    public MQATileMap getMapWidget() {
        return m_map;
    }

    void setSize(int offsetWidth, int offsetHeight) {
        getMapWidget().setSize(MQASize.newInstance(offsetWidth, offsetHeight));
    }

    void removeOverlay(MQAPoi marker) {
        getMapWidget().removeShape(marker);
    }

    void addOverlay(final MQAPoi point) {
        getMapWidget().addShape(point);
    }

    void setCenter(final GWTLatLng latLng) {
        getMapWidget().setCenter(MQALatLng.newInstance(latLng.getLatitude(), latLng.getLongitude()));
    }

    MQAInfoWindow getInfoWindow() {
        return getMapWidget().getInfoWindow();
    }

    void addControl(MQALargeZoomControl zoomControl) {
        getMapWidget().addControl(zoomControl);
    }

    void setZoomLevel(int level) {
        getMapWidget().setZoomLevel(level);
    }

    public void initializeMap() {
        m_mapHolder.setSize("100%", "100%");
        addControl(MQALargeZoomControl.newInstance());
        setZoomLevel(2);
    
        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent event) {
                
                setSize(m_mapHolder.getOffsetWidth(), m_mapHolder.getOffsetHeight());
            }
        });
    }

    public void updateSize() {
        setSize(m_mapHolder.getOffsetWidth(), m_mapHolder.getOffsetHeight());
    }

    void showLocationDetails(final MapQuestLocation location) {
        final MQAPoi point = location.getMarker();
    	
    	final GWTLatLng latLng = location.getLocationInfo().getLatLng();
    	setCenter(latLng);
    	if (point != null) {
    		point.setInfoTitleHTML(location.getLocationInfo().getName() + " (" + location.getLocationInfo().getArea() + ")");
    		point.setInfoContentHTML("Status = " + location.getLocationInfo().getMonitorStatus().toString());
    		final MQAInfoWindow window = getInfoWindow();
    		window.hide();
    		point.showInfoWindow();
    	}
    }

    MQAPoi createMarker(MapQuestLocation location) {
        final LocationInfo locationInfo = location.getLocationInfo();
        
        final GWTLatLng gLatLng = locationInfo.getLatLng();
        final MQALatLng latLng = MQALatLng.newInstance(gLatLng.getLatitude(), gLatLng.getLongitude());
        final MQAIcon icon = MQAIcon.newInstance("images/icon-" + locationInfo.getMonitorStatus() + ".png", 32, 32);
        final MQAPoi point = MQAPoi.newInstance(latLng, icon);
        point.setIconOffset(MQAPoint.newInstance(-16, -32));
        location.setMarker(point);
        
        return location.getMarker();
    }

}
