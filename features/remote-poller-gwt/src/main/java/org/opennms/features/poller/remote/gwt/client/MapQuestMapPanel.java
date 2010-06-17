package org.opennms.features.poller.remote.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.utils.BoundsBuilder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwtmapquest.transaction.MQAIcon;
import com.googlecode.gwtmapquest.transaction.MQALargeZoomControl;
import com.googlecode.gwtmapquest.transaction.MQALatLng;
import com.googlecode.gwtmapquest.transaction.MQAPoi;
import com.googlecode.gwtmapquest.transaction.MQAPoint;
import com.googlecode.gwtmapquest.transaction.MQARectLL;
import com.googlecode.gwtmapquest.transaction.MQATileMap;
import com.googlecode.gwtmapquest.transaction.event.MoveEndEvent;
import com.googlecode.gwtmapquest.transaction.event.MoveEndHandler;
import com.googlecode.gwtmapquest.transaction.event.ZoomEndEvent;
import com.googlecode.gwtmapquest.transaction.event.ZoomEndHandler;

public class MapQuestMapPanel extends Composite implements MapPanel, HasDoubleClickHandlers, HasClickHandlers {

    private class DefaultMarkerClickHandler implements ClickHandler {

        private GWTMarkerState m_markerState;

        public DefaultMarkerClickHandler(final GWTMarkerState markerState) {
            setMarkerState(markerState);
        }

        public void onClick(final ClickEvent event) {
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

    interface MapQuestMapPanelUiBinder extends UiBinder<Widget, MapQuestMapPanel> {
    }

    public MapQuestMapPanel(final HandlerManager eventBus) {
        m_eventBus = eventBus;
        initWidget(uiBinder.createAndBindUi(this));
        m_map = MQATileMap.newInstance(getMapHolder().getElement());

        initializeMap();

        m_map.addMoveEndHandler(new MoveEndHandler() {
            public void onMoveEnd(final MoveEndEvent event) {
                m_eventBus.fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
            }
        });
        
        addDoubleClickHandler(new DoubleClickHandler() {

            public void onDoubleClick(DoubleClickEvent arg0) {
                m_map.zoomIn();
            }
        });
        
//        addClickHandler(new ClickHandler() {
//
//            public void onClick(ClickEvent event) {
//                MQAPoint point = MQAPoint.newInstance(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
//                MQALatLng latLng = m_map.pixToLL(point);
//                m_map.panToLatLng(latLng);
//                
//            }
//            
//        });
        
                
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
        m_map.setZoomLevel(1);
        m_map.setCenter(MQALatLng.newInstance("0,0"));

        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent event) {
                syncMapSizeWithParent();
            }
        });
    }

    public void showLocationDetails(final String name, final String htmlTitle, final String htmlContent) {
        final MQAPoi point = getMarker(name);
        if (point != null) {
            final MQALatLng latLng = point.getLatLng();
            m_map.setCenter(latLng);
            m_map.getInfoWindow().hide();

            point.setInfoTitleHTML(htmlTitle);
            point.setInfoContentHTML(htmlContent);
            point.showInfoWindow();
            final NodeList<Element> elements = Document.get().getElementsByTagName("div");
            for (int i = 0; i < elements.getLength(); i++) {
                final Element e = elements.getItem(i);
                if (e.getClassName().equals("mqpoicontenttext")) {
                    final Style s = e.getStyle();
                    s.setOverflow(Overflow.HIDDEN);
                    break;
                }
            }
        }
    }

    private MQAPoi createMarker(final GWTMarkerState marker) {
        final MQALatLng latLng = toMQALatLng(marker.getLatLng());
        final MQAIcon icon = createIcon(marker);
        final MQAPoi point = MQAPoi.newInstance(latLng, icon);
        point.setIconOffset(MQAPoint.newInstance(-16, -32));
        point.addClickHandler(new DefaultMarkerClickHandler(marker));
        point.setMaxZoomLevel(16);
        point.setMinZoomLevel(1);

        return point;
    }

    private MQAIcon createIcon(final GWTMarkerState marker) {
        return MQAIcon.newInstance(marker.getImageURL(), 32, 32);
    }

    public GWTBounds getBounds() {
        return toGWTBounds(m_map.getBounds());
    }

    public void setBounds(final GWTBounds b) {
        m_map.zoomToRect(toMQARectLL(b));
    }

    private static MQALatLng toMQALatLng(final GWTLatLng latLng) {
        return MQALatLng.newInstance(latLng.getLatitude(), latLng.getLongitude());
    }

    private static GWTBounds toGWTBounds(final MQARectLL bounds) {
        final BoundsBuilder bldr = new BoundsBuilder();
        bldr.extend(bounds.getUpperLeft().getLatitude(), bounds.getUpperLeft().getLongitude());
        bldr.extend(bounds.getLowerRight().getLatitude(), bounds.getLowerRight().getLongitude());
        return bldr.getBounds();
    }

    private static MQARectLL toMQARectLL(final GWTBounds bounds) {
        final MQALatLng ne = toMQALatLng(bounds.getNorthEastCorner());
        final MQALatLng sw = toMQALatLng(bounds.getSouthWestCorner());
        final MQARectLL mqBounds = MQARectLL.newInstance(ne, sw);
        return mqBounds;
    }

    private SimplePanel getMapHolder() {
        return m_mapHolder;
    }

    private void syncMapSizeWithParent() {
        m_map.setSize();
    }

    public void placeMarker(final GWTMarkerState marker) {
        MQAPoi m = getMarker(marker.getName());

        if (m == null) {
            m = createMarker(marker);
            m_markers.put(marker.getName(), m);
            m_map.addShape(m);
        } else {
            updateMarker(m, marker);
        }

    }

    private void updateMarker(final MQAPoi m, final GWTMarkerState marker) {
        m.setIcon(createIcon(marker));
        m.setVisible(marker.isVisible());
    }

    private MQAPoi getMarker(final String name) {
        return m_markers.get(name);
    }

    public Widget getWidget() {
        return this;
    }

    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return addDomHandler(handler, DoubleClickEvent.getType());
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

}
