package org.opennms.features.poller.remote.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
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
import com.googlecode.gwtmapquest.transaction.MQATileMap;
import com.googlecode.gwtmapquest.transaction.event.ZoomEndEvent;
import com.googlecode.gwtmapquest.transaction.event.ZoomEndHandler;

public class MapQuestMapPanel extends Composite implements MapPanel {

    private class DefaultMarkerClickHandler implements ClickHandler {
        
        private GWTMarkerState m_markerState;
        
        public DefaultMarkerClickHandler(GWTMarkerState markerState) {
            setMarkerState(markerState);
        }

        public void onClick(ClickEvent event) {
            m_eventBus.fireEvent(new GWTMarkerClickedEvent(getMarkerState()));
        }

        public void setMarkerState(final GWTMarkerState markerState) {
            m_markerState = markerState;
        }

        public GWTMarkerState getMarkerState() {
            return m_markerState;
        }

    }

    private static MapQuestMapPanelUiBinder uiBinder = GWT.create(MapQuestMapPanelUiBinder.class);
    
    @UiField
    SimplePanel m_mapHolder;
    
    private MQATileMap m_map;
    private Map<String, MQAPoi> m_markers = new HashMap<String, MQAPoi>();
    private HandlerManager m_eventBus;
    
    interface MapQuestMapPanelUiBinder extends UiBinder<Widget, MapQuestMapPanel> {}

    public MapQuestMapPanel(final HandlerManager eventBus) {
        m_eventBus = eventBus;
        initWidget(uiBinder.createAndBindUi(this));
        m_map = MQATileMap.newInstance(getMapHolder().getElement());
        
        initializeMap();
        
        m_map.addZoomEndHandler(new ZoomEndHandler() {
            
            public void onZoomEnd(ZoomEndEvent event) {
                m_eventBus.fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
                
            }
        });
    }
    
    

    @Override
    protected void onLoad() {
        super.onLoad();
        syncMapSizeWithParent();
    }



    public void initializeMap() {
        getMapHolder().setSize("100%", "100%");
        m_map.addControl(MQALargeZoomControl.newInstance());
        m_map.setZoomLevel(2);
        
        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent event) {
                syncMapSizeWithParent();
            }
        });
    }

    public void showLocationDetails(String name, String htmlTitle, String htmlContent) {
        final MQAPoi point = getMarker(name);
    	
    	m_map.setCenter(point.getLatLng());
    	if (point != null) {
    		point.setInfoTitleHTML(htmlTitle);
    		point.setInfoContentHTML(htmlContent);
    		MQAInfoWindow window = m_map.getInfoWindow();
    		window.hide();
    		point.showInfoWindow();
    	}
    }



    private MQAPoi createMarker(GWTMarkerState marker) {
        
        final MQALatLng latLng = toMQALatLng(marker.getLatLng());
        final MQAIcon icon = createIcon(marker);
        final MQAPoi point = MQAPoi.newInstance(latLng, icon);
        point.setIconOffset(MQAPoint.newInstance(-16, -32));
        point.addClickHandler(new DefaultMarkerClickHandler(marker));
        
        return point;
    }

    private MQAIcon createIcon(GWTMarkerState marker) {
        return MQAIcon.newInstance(marker.getImageURL(), 32, 32);
    }

    public GWTBounds getBounds() {
        return toGWTBounds(m_map.getBounds());
    }
    
    public void setBounds(GWTBounds b) {
        m_map.zoomToRect(toMQARectLL(b));
    }
    
    @SuppressWarnings("unused")
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

    private SimplePanel getMapHolder() {
        return m_mapHolder;
    }

    private void syncMapSizeWithParent() {
        m_map.setSize();
        //getMapWidget().setSize(MQASize.newInstance(getMapHolder().getOffsetWidth(), getMapHolder().getOffsetHeight()));
    }

    public void placeMarker(GWTMarkerState marker) {
        MQAPoi m = getMarker(marker.getName());
        
        if(m == null) {
        	m = createMarker(marker);
        	m_markers.put(marker.getName(), m);
        	m_map.addShape(m);
        }else {
            updateMarker(m, marker);
        }
        
    }

    private void updateMarker(MQAPoi m, GWTMarkerState marker) {
        m.setIcon(createIcon(marker));
        m.setVisible(marker.isVisible());
    }

    private MQAPoi getMarker(String name) {
        return m_markers.get(name);
    }

    public Widget getWidget() {
        return this;
    }

}
