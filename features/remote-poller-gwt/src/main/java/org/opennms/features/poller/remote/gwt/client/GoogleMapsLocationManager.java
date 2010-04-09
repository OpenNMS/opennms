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

	private final Map<String,BaseLocation> m_locations = new HashMap<String,BaseLocation>();
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
        final Location location = m_locations.get(locationName);
        if (location == null) {
            return;
        }
        m_mapPanel.showLocationDetails(location);
    }

    @Override
	public void updateComplete() {
		if (!updated) {
			DeferredCommand.addPause();
			DeferredCommand.addCommand(new IncrementalCommand() {
				public boolean execute() {
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
        for (Location l : m_locations.values()) {
            bldr.extend(l.getLocationInfo().getLatLng());
        }
        return bldr.getBounds();
    }

    @Override
    public void updateLocation(final LocationInfo info) {
        if (info == null) return;
        
        Location location = getLocation(info);
        
        m_mapPanel.placeMarker(location);
        
    }

    private Location getLocation(final LocationInfo info) {
        BaseLocation location = m_locations.get(info.getName());
        if(location == null) {
            location = new BaseLocation(info);
            m_locations.put(info.getName(), location);
        }else {
            location.setLocationInfo(info);
        }
        return location;
    }
    
	@Override
	public void reportError(final String errorMessage, final Throwable throwable) {
		// FIXME: implement error reporting in UI
	}
	
	

}
