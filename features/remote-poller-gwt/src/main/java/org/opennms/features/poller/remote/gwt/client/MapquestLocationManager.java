package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.googlecode.gwtmapquest.transaction.MQAIcon;
import com.googlecode.gwtmapquest.transaction.MQAInfoWindow;
import com.googlecode.gwtmapquest.transaction.MQALargeZoomControl;
import com.googlecode.gwtmapquest.transaction.MQALatLng;
import com.googlecode.gwtmapquest.transaction.MQAPoi;
import com.googlecode.gwtmapquest.transaction.MQAPoint;
import com.googlecode.gwtmapquest.transaction.MQASize;
import com.googlecode.gwtmapquest.transaction.MQATileMap;



public class MapquestLocationManager extends AbstractLocationManager implements LocationManager {
	private final SplitLayoutPanel m_outerPanel;
	private SimplePanel m_panel;

	private MQATileMap m_map;

	private final Map<String,MapQuestLocation> m_locations = new HashMap<String,MapQuestLocation>();

	public MapquestLocationManager(Application application, HandlerManager eventBus, SplitLayoutPanel splitPanel) {
		super(application, eventBus);
		m_outerPanel = splitPanel;
	}

	
    @Override
    protected void initializationComplete() {
        super.initializationComplete();
        m_map.setSize(MQASize.newInstance(m_panel.getOffsetWidth(), m_panel.getOffsetHeight()));
    }


    public void updateMarker(final Location location) {
		final MapQuestLocation oldLocation = m_locations.get(location.getName());
		if (oldLocation != null) {
			m_map.removeShape(oldLocation.getMarker());
		}
		MapQuestLocation newLocation;
		if (location instanceof MapQuestLocation) {
			newLocation = (MapQuestLocation)location;
		} else {
			newLocation = new MapQuestLocation(location);
		}

		final MQALatLng latLng = MQALatLng.newInstance(location.getLatLng().getLatitude(), location.getLatLng().getLongitude());
		final MQAIcon icon = MQAIcon.newInstance("images/icon-" + location.getLocationMonitorState().getStatus().toString() + ".png", 32, 32);
		final MQAPoi point = MQAPoi.newInstance(latLng, icon);
//		Window.alert("current offset = " + point.getIconOffset().getX() + "," + point.getIconOffset().getY());
		point.setIconOffset(MQAPoint.newInstance(-16, -32));
		newLocation.setMarker(point);
		m_locations.put(location.getName(), newLocation);
		m_map.addShape(point);
        locationUpdateComplete(location);
        if (!isLocationUpdateInProgress()) {
        	checkAllVisibleLocations();
        }
	}

	private void checkAllVisibleLocations() {
	    m_eventBus.fireEvent(new LocationsUpdatedEvent(this));
	}

	@Override
	public void removeLocation(final Location location) {
		if (location == null) return;
		GWTLatLng latLng = location.getLatLng();
		if (latLng == null) {
			Log.warn("no lat/long for location " + location.getName());
			return;
		}
		MapQuestLocation loc = new MapQuestLocation(location);
		updateMarker(loc);
	}

	@Override
	public void fitToMap() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Location> getAllLocations() {
		final List<Location> locations = new ArrayList<Location>(m_locations.values());
		Collections.sort(locations);
		return locations;
	}

	@Override
	public List<Location> getVisibleLocations() {
		return getAllLocations();
	}

	@Override
	public void selectLocation(String locationName) {
		final MapQuestLocation location = m_locations.get(locationName);
		if (location == null) return;

		final MQAPoi point = location.getMarker();
		final GWTLatLng latLng = location.getLatLng();
		m_map.saveState();
		m_map.setCenter(MQALatLng.newInstance(latLng.getLatitude(), latLng.getLongitude()));
		if (point != null) {
			point.setInfoTitleHTML(location.getName() + " (" + location.getArea() + ")");
			point.setInfoContentHTML("Status = " + location.getLocationMonitorState().getStatus().toString());
			final MQAInfoWindow window = m_map.getInfoWindow();
			window.hide();
			point.showInfoWindow();
		}
	}

	@Override
	public void updateComplete() {
	}

	@Override
	public void reportError(String string, Throwable t) {
	}


	@Override
    protected void initializeMapWidget() {
        m_panel = new SimplePanel();
        m_panel.setSize("100%", "100%");
        m_outerPanel.add(m_panel);
        
        m_map = MQATileMap.newInstance(m_panel.getElement());
        m_map.addControl(MQALargeZoomControl.newInstance());
        m_map.setZoomLevel(2);
        
        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent event) {
                m_map.setSize(MQASize.newInstance(m_panel.getOffsetWidth(), m_panel.getOffsetHeight()));
            }
        });
    }



}
