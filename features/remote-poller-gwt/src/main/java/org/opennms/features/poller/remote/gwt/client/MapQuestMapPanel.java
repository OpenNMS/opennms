package org.opennms.features.poller.remote.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEvent;
import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerInfoWindowRefreshEvent;
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
import com.google.gwt.user.client.Timer;
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
import com.googlecode.gwtmapquest.transaction.event.DblClickEvent;
import com.googlecode.gwtmapquest.transaction.event.DblClickHandler;
import com.googlecode.gwtmapquest.transaction.event.MoveEndEvent;
import com.googlecode.gwtmapquest.transaction.event.MoveEndHandler;
import com.googlecode.gwtmapquest.transaction.event.ZoomEndEvent;
import com.googlecode.gwtmapquest.transaction.event.ZoomEndHandler;

/**
 * <p>MapQuestMapPanel class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class MapQuestMapPanel extends Composite implements MapPanel, HasDoubleClickHandlers, HasClickHandlers {

    public GWTLatLng m_currentInfoWindowLatLng = null;

    private class DefaultMarkerClickHandler implements ClickHandler {

        private GWTMarkerState m_markerState;

        public DefaultMarkerClickHandler(final GWTMarkerState markerState) {
            setMarkerState(markerState);
        }

        public void onClick(final ClickEvent event) {
            m_currentInfoWindowLatLng  = getMarkerState().getLatLng();
            m_eventBus.fireEvent(new GWTMarkerClickedEvent(getMarkerState()));
        }

        public void setMarkerState(final GWTMarkerState markerState) {
            m_markerState = markerState;
        }

        public GWTMarkerState getMarkerState() {
            return m_markerState;
        }

    }
    
    private class ClickCounter{
        
        private int m_incr = 0;
        private MQALatLng m_latlng = null;
        private Timer m_timer = new Timer() {

            @Override
            public void run() {
                if(m_incr == 1) {
                    m_map.panToLatLng(m_latlng);
                }else if(m_incr == 3) {
                    m_map.setCenter(m_latlng);
                    m_map.zoomIn();
                }
                
                m_incr = 0;
            }
            
        };
        
        public void incrementCounter(MQALatLng latLng) {
            m_incr++;
            m_latlng = latLng;
            m_timer.cancel();
            m_timer.schedule(300);
        }
        
    }

    private static MapQuestMapPanelUiBinder uiBinder = GWT.create(MapQuestMapPanelUiBinder.class);

    @UiField
    SimplePanel m_mapHolder;

    private MQATileMap m_map;

    private Map<String, MQAPoi> m_markers = new HashMap<String, MQAPoi>();

    private HandlerManager m_eventBus;
    
    private ClickCounter m_clickCounter = new ClickCounter();

    interface MapQuestMapPanelUiBinder extends UiBinder<Widget, MapQuestMapPanel> {
    }

    public MapQuestMapPanel(final HandlerManager eventBus) {
        m_eventBus = eventBus;
        m_map = MQATileMap.newInstance(m_mapHolder.getElement());
        
        initWidget(uiBinder.createAndBindUi(this));
        initializeMap();
    }
    /**
     * <p>Constructor for MapQuestMapPanel.</p>
     *
     * @param eventBus a {@link com.google.gwt.event.shared.HandlerManager} object.
     */
    public MapQuestMapPanel(final HandlerManager eventBus, MQATileMap map) {
        m_eventBus = eventBus;
        m_map = map;

        initWidget(uiBinder.createAndBindUi(this));
        initializeMap();
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
        m_mapHolder.setSize("100%", "100%");
        m_map.addControl(MQALargeZoomControl.newInstance());
        m_map.setZoomLevel(1);
        m_map.setCenter(MQALatLng.newInstance("0,0"));

        m_map.addMoveEndHandler(new MoveEndHandler() {
            public void onMoveEnd(final MoveEndEvent event) {
                m_eventBus.fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
            }
        });
        
        m_map.addClickHandler(new com.googlecode.gwtmapquest.transaction.event.ClickHandler() {
            
            public void onClicked(final com.googlecode.gwtmapquest.transaction.event.ClickEvent event) {
                m_clickCounter.incrementCounter(event.getLL());
            }
        });
        
        m_map.addDblClickHandler(new DblClickHandler() {
            
            public void onDblClicked(DblClickEvent event) {
                m_clickCounter.incrementCounter(event.getLL());
            }
        });
        
        m_map.addZoomEndHandler(new ZoomEndHandler() {
            public void onZoomEnd(ZoomEndEvent event) {
                m_eventBus.fireEvent(new MapPanelBoundsChangedEvent(getBounds()));
            }
        });

        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent event) {
                syncMapSizeWithParent();
            }
        });
    }

    /** {@inheritDoc} */
    public void showLocationDetails(final String name, final String htmlTitle, final String htmlContent) {
        final MQAPoi point = getMarker(name);
        if (point != null) {
            point.setInfoTitleHTML(htmlTitle);
            point.setInfoContentHTML(htmlContent);
            if(m_map.getInfoWindow().isHidden()) {
                point.showInfoWindow();
            }
            
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
        final MQAPoi point = (MQAPoi)MQAPoi.newInstance(latLng);
        point.setVisible(marker.isVisible());

        final MQAIcon icon = createIcon(marker);
        point.setIcon(icon);
        point.setIconOffset(MQAPoint.newInstance(-16, -32));

        point.addClickHandler(new DefaultMarkerClickHandler(marker));
        point.setMaxZoomLevel(16);
        point.setMinZoomLevel(1);
        point.setRolloverEnabled(true);

        return point;
    }

    private MQAIcon createIcon(final GWTMarkerState marker) {
        return MQAIcon.newInstance(marker.getImageURL(), 32, 32);
    }

    /**
     * <p>getBounds</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public GWTBounds getBounds() {
        return toGWTBounds(m_map.getBounds());
    }

    /** {@inheritDoc} */
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

    private void syncMapSizeWithParent() {
        m_map.setSize();
    }

    /** {@inheritDoc} */
    public void placeMarker(final GWTMarkerState marker) {
        MQAPoi m = getMarker(marker.getName());

        if (m == null) {
            m = createMarker(marker);
            m_markers.put(marker.getName(), m);
            m_map.addShape(m);
        } else {
            updateMarker(m, marker);
            GWTLatLng latLng = new GWTLatLng(m.getLatLng().getLatitude(), m.getLatLng().getLongitude());
            if(latLng.equals(m_currentInfoWindowLatLng) && !m_map.getInfoWindow().isHidden()) {
                m_eventBus.fireEvent(new GWTMarkerInfoWindowRefreshEvent(marker));
            }
        }

    }

    private void updateMarker(final MQAPoi m, final GWTMarkerState marker) {
        m.setIcon(createIcon(marker));
        m.setVisible(marker.isVisible());
    }

    private MQAPoi getMarker(final String name) {
        return m_markers.get(name);
    }

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
