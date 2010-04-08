package org.opennms.features.poller.remote.gwt.client;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEventHandler;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

public class GoogleMapsLocationManager extends AbstractLocationManager {

 	private GoogleMapsPanel m_mapPanel = new GoogleMapsPanel();

	private final Map<String,GoogleMapsLocation> m_locations = new HashMap<String,GoogleMapsLocation>();
    private boolean updated = false;

	public GoogleMapsLocationManager(final HandlerManager eventBus, final SplitLayoutPanel panel) {
		super(eventBus, panel);
	}

	@Override
    protected void initializeMapWidget() {
        
        getPanel().add(m_mapPanel);
        
        m_mapPanel.addMapPanelBoundsChangedEventHandler(new MapPanelBoundsChangedEventHandler() {
            
            public void onBoundsChanged(MapPanelBoundsChangedEvent event) {
                m_eventBus.fireEvent(new LocationsUpdatedEvent(GoogleMapsLocationManager.this));
            }
        });
        
        m_eventBus.addHandler(LocationPanelSelectEvent.TYPE, new LocationPanelSelectEventHandler() {

            public void onLocationSelected(final LocationPanelSelectEvent event) {
                selectLocation(event.getLocationName());
            }
        }); 
        
	}


	@Override
    public void selectLocation(String locationName) {
        final GoogleMapsLocation location = m_locations.get(locationName);
        if (location == null) {
            return;
        }
        m_mapPanel.showLocationDetails(location);
    }

    @Override
	public void removeLocation(final Location location) {
		if (location == null) return;
		GoogleMapsLocation loc = m_locations.get(location.getLocationInfo().getName());
		if (loc.getMarker() != null) {
			m_mapPanel.removeOverlay(loc.getMarker());
		}
		m_locations.remove(location.getLocationInfo().getName());
	}

    @Override
	public void updateComplete() {
		if (!updated) {
			DeferredCommand.addPause();
			DeferredCommand.addCommand(new IncrementalCommand() {
				public boolean execute() {
					if (isLocationUpdateInProgress())
						return true;
					fitToMap();
					updated = true;
					return false;
				}
			});
		}
	}

	@Override
	public List<Location> getAllLocations() {
		return new ArrayList<Location>(m_locations.values());
	}

	@Override
    public List<Location> getVisibleLocations() {
	    List<Location> visibleLocations = new ArrayList<Location>();
	    GWTBounds bounds = m_mapPanel.getBounds();
	    for(BaseLocation location : m_locations.values()) {
	        if(location.isVisible(bounds)) {
	            visibleLocations.add(location);
	        }
	    }

	    return visibleLocations;
    }

	@Override
    public void fitToMap() {
    	m_mapPanel.setBounds(getLocationBounds());
    }

    private GWTBounds getLocationBounds() {
        BoundsBuilder bldr = new BoundsBuilder();
        for (BaseLocation l : m_locations.values()) {
            bldr.extend(l.getLocationInfo().getLatLng());
        }
        return bldr.getBounds();
    }


	@Override
	protected void updateMarker(final Location location) {
		final LocationInfo locationInfo = location.getLocationInfo();
        final GoogleMapsLocation oldLocation = m_locations.get(locationInfo.getName());
        addAndMergeLocation(oldLocation, new GoogleMapsLocation(location));

        if (oldLocation == null) {
            placeMarker(m_locations.get(locationInfo.getName()));
        } else if (!oldLocation.getLocationInfo().getMonitorStatus().equals(locationInfo.getMonitorStatus())) {
            placeMarker(m_locations.get(locationInfo.getName()));
        }

        locationUpdateComplete(location);
	}

	private void placeMarker(final GoogleMapsLocation location) {
		if (location.getMarker() == null) {
		    m_mapPanel.addOverlay(m_mapPanel.createMarker(location));
		} else {
			m_mapPanel.createMarker(location);
		}
	}

    private void addAndMergeLocation(final GoogleMapsLocation oldLocation, final GoogleMapsLocation newLocation) {
        if(oldLocation != null) {
            m_locations.put(newLocation.getLocationInfo().getName(), mergeLocations(oldLocation, newLocation));
        }else {
            m_locations.put(newLocation.getLocationInfo().getName(), newLocation);
        }
    }

	private GoogleMapsLocation mergeLocations(final GoogleMapsLocation oldLocation, final GoogleMapsLocation newLocation) {
		if (newLocation.getLocationInfo().getMonitorStatus() == null)
			newLocation.getLocationInfo().setMonitorStatus(oldLocation.getLocationInfo().getMonitorStatus());
		if (newLocation.getLocationDetails().getLocationMonitorState() == null)
			newLocation.getLocationDetails().setLocationMonitorState(oldLocation.getLocationDetails().getLocationMonitorState());
		if (newLocation.getLocationInfo().getCoordinates() == null)
			newLocation.getLocationInfo().setCoordinates(oldLocation.getLocationInfo().getCoordinates());
		if (newLocation.getMarker() == null)
			newLocation.setMarker(oldLocation.getMarker());
		return newLocation;
	}

	@Override
	public void reportError(final String errorMessage, final Throwable throwable) {
		// FIXME: implement error reporting in UI
	}

}
