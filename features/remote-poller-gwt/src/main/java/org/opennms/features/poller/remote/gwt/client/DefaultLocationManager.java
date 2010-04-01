package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.utility.client.mapiconmaker.MapIconMaker;
import com.google.gwt.maps.utility.client.mapiconmaker.MarkerIconOptions;
import com.google.gwt.maps.utility.client.markermanager.MarkerManager;

public class DefaultLocationManager extends AbstractLocationManager implements LocationManager {
	private static final int MARKER_MAX_ZOOM = 18;
	protected static final LatLng DEFAULT_LATLNG = LatLng.fromUrlValue("35.7174,-79.1619"); // OpenNMS World HQ
	private final MapWidget m_mapWidget;
	private final MarkerManager m_markerManager;
	private final Map<String,Location> m_locations = new HashMap<String,Location>();
	private final Geocoder m_geocoder = new Geocoder();
	private transient HandlerManager m_eventBus;
	private boolean updated = false;

	public DefaultLocationManager(final HandlerManager eventBus, final MapWidget mapWidget, final MarkerManager markerManager) {
		m_eventBus = eventBus;
		setupEventHandlers();
		m_mapWidget = mapWidget;
		m_markerManager = markerManager;
	}

	private void setupEventHandlers() {
        getEventBus().addHandler(LocationPanelSelectEvent.TYPE, new LocationPanelSelectEventHandler() {
            
            public void onLocationSelected(final LocationPanelSelectEvent event) {
                selectLocation(event.getLocationName());
            }
        });
        
    }

	@Override
    public void updateLocations(final List<Location> locations) {
		for (final Location location : locations) {
			if (location == null) {
				continue;
			}

			if (location.getLatLng() != null) {
				placeMarker(location);
			} else {
				m_geocoder.getLatLng(location.getGeolocation(), new LatLngMarkerPlacer(location));
			}
		}
		
	}

	@Override
	public void removeLocations(final List<Location> locations) {
		for (Location location : locations) {
			if (location == null) {
				continue;
			}
			if (location.getMarker() != null) {
				removeMarker(location.getMarker().getLatLng());
			} else if (location.getLatLng() != null) {
				removeMarker(location.getLatLng());
			} else {
				m_geocoder.getLatLng(location.getGeolocation(), new LatLngMarkerRemover(location));
			}
			m_locations.remove(location.getName());
		}
		
		getEventBus().fireEvent(new LocationsUpdatedEvent(this));
	}

	@Override
	public void updateComplete() {
		if (!updated) {
			fitToMap();
			updated = true;
		}
	}

	@Override
	public Location getLocation(final int index) {
		final String[] locations = m_locations.keySet().toArray(new String[0]);
		return m_locations.get(locations[index]);
	}

	@Override
	public List<Location> getAllLocations() {
		return new ArrayList<Location>(m_locations.values());
	}

	@Override
	public List<Location> getLocations(final int startIndex, final int maxRows) {
		final List<String> keys = Arrays.asList(m_locations.keySet().toArray(new String[0]));
		final List<Location> locations = new ArrayList<Location>(maxRows);
		for (String key : keys.subList(startIndex, checkOutOfBounds(keys.size(), (startIndex + maxRows) ) )) {
			locations.add(m_locations.get(key));
		}
		return locations;
	}

	@Override
    public void selectLocation(String locationName) {
    	final Location location = m_locations.get(locationName);
    	if (location == null) {
    		return;
    	}
		final Marker m = location.getMarker();
		m_mapWidget.savePosition();
		m_mapWidget.setCenter(location.getLatLng());
		if (m != null) {
			m_mapWidget.getInfoWindow().open(m, Utils.getInfoWindowForLocation(location));
		}
	}

	@Override
    public void fitToMap() {
    	final LatLngBounds bounds = LatLngBounds.newInstance();
    	for (Location l : m_locations.values()) {
    		if (l.getLatLng() != null) {
    			bounds.extend(l.getLatLng());
    		} else if (l.getMarker() != null) {
    			bounds.extend(l.getMarker().getLatLng());
    		}
    	}
    	m_mapWidget.setCenter(bounds.getCenter(), m_mapWidget.getBoundsZoomLevel(bounds));
    }

	private int checkOutOfBounds(int size, int maxRows) {
		return maxRows > size? size : maxRows;
    }

	private void placeMarker(final Location location) {
		if (location == null) {
			return;
		}
		LatLng latLng = location.getLatLng();
		if (latLng == null) {
			return;
		}

		boolean replaceMarker = false;
		
		Location oldLocation = m_locations.get(location.getName());
		if (oldLocation == null) {
			replaceMarker = true;
		}
		if (oldLocation != null && !oldLocation.getLocationMonitorState().getStatus().equals(location.getLocationMonitorState().getStatus())) {
			replaceMarker = true;
		}

		if (replaceMarker) {
			final Marker m = createMarker(location);
			m_markerManager.removeMarker(m_markerManager.getMarker(latLng.getLatitude(), latLng.getLongitude(), MARKER_MAX_ZOOM));
			m_markerManager.addMarker(m, 0, MARKER_MAX_ZOOM);
		}

		m_locations.put(location.getName(), mergeLocations(oldLocation, location));

		getEventBus().fireEvent(new LocationsUpdatedEvent(this));
	}

	private Location mergeLocations(final Location oldLocation, final Location newLocation) {
		if (newLocation.getLocationMonitorState() == null)
			newLocation.setLocationMonitorState(oldLocation.getLocationMonitorState());
		if (newLocation.getLatLng() == null)
			newLocation.setLatLng(oldLocation.getLatLng());
		if (newLocation.getMarker() == null)
			newLocation.setMarker(oldLocation.getMarker());
		return newLocation;
	}

	private void removeMarker(LatLng latLng) {
		final Marker m = m_markerManager.getMarker(latLng.getLatitude(), latLng.getLongitude(), MARKER_MAX_ZOOM);
		m_markerManager.removeMarker(m);
	}
	
	private Marker createMarker(final Location location) {
		final LocationMonitorState state = location.getLocationMonitorState();
		final MarkerIconOptions mio = MarkerIconOptions.newInstance();
		mio.setPrimaryColor("#00ff00");
		if (state != null && state.getStatus() != null) {
			mio.setPrimaryColor(state.getStatus().getColor());
		}
		Icon icon = MapIconMaker.createMarkerIcon(mio);

		final MarkerOptions markerOptions = MarkerOptions.newInstance();
		markerOptions.setAutoPan(true);
		markerOptions.setClickable(true);
		markerOptions.setTitle(location.getName());
		markerOptions.setIcon(icon);
		final Marker m = new Marker(location.getLatLng(), markerOptions);
		m.addMarkerClickHandler(new DefaultMarkerClickHandler(location.getName()));
		location.setMarker(m);

		return m;
	}

	public void setEventBus(final HandlerManager eventBus) {
		m_eventBus = eventBus;
	}

	public HandlerManager getEventBus() {
		return m_eventBus;
	}

	@Override
	public void reportError(final String errorMessage, final Throwable throwable) {
		// FIXME: implement error reporting in UI
	}

	private final class LatLngMarkerPlacer implements LatLngCallback {
		private final Location m_location;

		private LatLngMarkerPlacer(final Location location) {
			m_location = location;
		}

		public void onSuccess(final LatLng point) {
			m_location.setLatLng(point);
			placeMarker(m_location);
		}

		public void onFailure() {
			m_location.setLatLng(DefaultLocationManager.DEFAULT_LATLNG);
			reportError("unable to retrieve latitude and longitude for " + m_location.getName(), null);
			placeMarker(m_location);
		}
	}

	private final class LatLngMarkerRemover implements LatLngCallback {
		private LatLngMarkerRemover(final Location location) {
		}

		public void onSuccess(final LatLng point) {
			removeMarker(point);
		}

		// do nothing on failure
		public void onFailure() {
		}
	}

	private final class DefaultMarkerClickHandler implements MarkerClickHandler {
		private final String m_locationName;

		private DefaultMarkerClickHandler(final String locationName) {
			m_locationName = locationName;
		}

		public void onClick(final MarkerClickEvent mke) {
			selectLocation(m_locationName);
		}
	}

}
