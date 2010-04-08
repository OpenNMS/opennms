package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.SplitLayoutPanel;


public class MapquestLocationManager extends AbstractLocationManager {
	private MapQuestMapPanel m_mapPanel = new MapQuestMapPanel();

	private final Map<String,MapQuestLocation> m_locations = new HashMap<String,MapQuestLocation>();

	public MapquestLocationManager(HandlerManager eventBus, SplitLayoutPanel splitPanel) {
		super(eventBus, splitPanel);
	}
	
	@Override
    protected void initializeMapWidget() {
        getPanel().add(m_mapPanel);
    }
	
    @Override
    protected void initializationComplete() {
        super.initializationComplete();
        m_mapPanel.updateSize();
    }
    
    @Override
    public void updateMarker(final Location location) {
		final LocationInfo locationInfo = location.getLocationInfo();
		final MapQuestLocation oldLocation = m_locations.get(locationInfo.getName());
		addAndMergeLocation(oldLocation, new MapQuestLocation(location));
		
		if (oldLocation == null) {
			placeMarker(m_locations.get(locationInfo.getName()));
		}else if(!oldLocation.getLocationInfo().getMonitorStatus().equals(locationInfo.getMonitorStatus())) {
		    placeMarker(m_locations.get(locationInfo.getName()));
		}


		locationUpdateComplete(location);
        if (!isLocationUpdateInProgress()) {
        	checkAllVisibleLocations();
        }
	}

    private void placeMarker(MapQuestLocation location) {
        if(location.getMarker() == null) {
            m_mapPanel.addOverlay(m_mapPanel.createMarker(location));
        }else {
            m_mapPanel.createMarker(location);
        }
    }

    private void addAndMergeLocation( final MapQuestLocation oldLocation, final MapQuestLocation newLocation) {
        if(oldLocation != null) {
            m_locations.put(newLocation.getLocationInfo().getName(), mergeLocation(oldLocation, newLocation));
        }else {
            m_locations.put(newLocation.getLocationInfo().getName(), newLocation);
        }
        
    }

    private MapQuestLocation mergeLocation(MapQuestLocation oldLocation, MapQuestLocation newLocation) {
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


    private void checkAllVisibleLocations() {
	    m_eventBus.fireEvent(new LocationsUpdatedEvent(this));
	}

	@Override
	public void removeLocation(final Location location) {
		if (location == null) return;
		GWTLatLng latLng = location.getLocationInfo().getLatLng();
		if (latLng == null) {
			Log.warn("no lat/long for location " + location.getLocationInfo().getName());
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
		if (location == null) {
		    return;
		}
		m_mapPanel.showLocationDetails(location);
	}


    @Override
	public void updateComplete() {
	}

	@Override
	public void reportError(String string, Throwable t) { }


	



}
