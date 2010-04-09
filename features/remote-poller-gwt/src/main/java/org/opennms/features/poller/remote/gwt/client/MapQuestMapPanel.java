package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEventHandler;
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
import com.googlecode.gwtmapquest.transaction.MQARectLL;
import com.googlecode.gwtmapquest.transaction.MQASize;
import com.googlecode.gwtmapquest.transaction.MQATileMap;
import com.googlecode.gwtmapquest.transaction.event.ZoomEndEvent;
import com.googlecode.gwtmapquest.transaction.event.ZoomEndHandler;

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
        
        m_map.addZoomEndHandler(new ZoomEndHandler() {
            
            public void onZoomEnd(ZoomEndEvent event) {
                fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
                
            }
        });
    }

    private void setMapWidget(MQATileMap map) {
        m_map = map;
    }

    public MQATileMap getMapWidget() {
        return m_map;
    }

    public void initializeMap() {
        m_mapHolder.setSize("100%", "100%");
        getMapWidget().addControl(MQALargeZoomControl.newInstance());
        getMapWidget().setZoomLevel(2);
    
        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent event) {
                
                getMapWidget().setSize(MQASize.newInstance(m_mapHolder.getOffsetWidth(), m_mapHolder.getOffsetHeight()));
            }
        });
    }

    void showLocationDetails(final MapQuestLocation location) {
        final MQAPoi point = location.getMarker();
    	
    	getMapWidget().setCenter(toMQALatLng(location.getLocationInfo().getLatLng()));
    	if (point != null) {
    		point.setInfoTitleHTML(location.getLocationInfo().getName() + " (" + location.getLocationInfo().getArea() + ")");
    		point.setInfoContentHTML("Status = " + location.getLocationInfo().getMonitorStatus().toString());
    		final MQAInfoWindow window = getMapWidget().getInfoWindow();
    		window.hide();
    		point.showInfoWindow();
    	}
    }

    MQAPoi createMarker(MapQuestLocation location) {
        final LocationInfo locationInfo = location.getLocationInfo();
        
        final MQALatLng latLng = toMQALatLng(locationInfo.getLatLng());
        final MQAIcon icon = MQAIcon.newInstance(locationInfo.getImageURL(), 32, 32);
        final MQAPoi point = MQAPoi.newInstance(latLng, icon);
        point.setIconOffset(MQAPoint.newInstance(-16, -32));
        location.setMarker(point);
        
        return location.getMarker();
    }

    public void addMapPanelBoundsChangedEventHandler(MapPanelBoundsChangedEventHandler handler) {
        addHandler(handler, MapPanelBoundsChangedEvent.TYPE);
    }

    private GWTBounds getBounds() {
        return toGWTBounds(m_map.getBounds());
    }
    
    
    private static GWTLatLng toGWTLatLng(MQALatLng latLng) {
        return new GWTLatLng(latLng.getLatitude(), latLng.getLongitude());
    }
    
    private static MQALatLng toMQALatLng(GWTLatLng latLng) {
        return MQALatLng.newInstance(latLng.getLatitude(), latLng.getLongitude());
    }
    
    private static GWTBounds toGWTBounds(MQARectLL bounds) {
        BoundsBuilder bldr = new BoundsBuilder();
        bldr.extend(bounds.getUpperLeft().getLatitude(), bounds.getUpperLeft().getLongitude());
        bldr.extend(bounds.getLowerRight().getLatitude(), bounds.getLowerRight().getLongitude());
        
        return bldr.getBounds();
    }
    
    private static MQARectLL toMQARectLL(GWTBounds bounds) {
        MQALatLng latLng = toMQALatLng(bounds.getNorthEastCorner());
        
        MQARectLL mqBounds = MQARectLL.newInstance(latLng, latLng);
        mqBounds.extend(toMQALatLng(bounds.getSouthWestCorner()));
        
        return mqBounds;
    }

}
