package org.opennms.features.poller.remote.gwt.client;

import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toGWTBounds;
import static org.opennms.features.poller.remote.gwt.client.GoogleMapsUtils.toLatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.event.MapMoveEndHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

public class GoogleMapsLocationManager extends AbstractLocationManager implements LocationManager {
    
 	private final SplitLayoutPanel m_panel;
	private GoogleMapsPanel m_mapPanel = new GoogleMapsPanel();
	
	private final Map<String,GoogleMapsLocation> m_locations = new HashMap<String,GoogleMapsLocation>();
    private boolean updated = false;

	public GoogleMapsLocationManager(Application application, final HandlerManager eventBus, final SplitLayoutPanel panel) {
		super(application, eventBus);
		m_panel = panel;
	}
	
	@Override
    protected void initializeMapWidget() {
        m_mapPanel.getMapWidget().setSize("100%", "100%");
        m_mapPanel.getMapWidget().setUIToDefault();
        m_mapPanel.getMapWidget().addControl(new LargeMapControl());
      //              m_mapWidget.setZoomLevel(10);
        m_mapPanel.getMapWidget().setContinuousZoom(true);
        m_mapPanel.getMapWidget().setScrollWheelZoomEnabled(true);
      
        m_mapPanel.getMapWidget().addMapMoveEndHandler(new MapMoveEndHandler() {
        
            public void onMoveEnd(MapMoveEndEvent event) {
                m_eventBus.fireEvent(new LocationsUpdatedEvent(GoogleMapsLocationManager.this));
            }
            
        });
        
        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(final ResizeEvent resizeEvent) {
                if (m_mapPanel.getMapWidget() != null) {
                    m_mapPanel.getMapWidget().checkResizeAndCenter();
                }
            }
        });
        
        m_panel.add(m_mapPanel.getMapWidget());

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
		GoogleMapsLocation loc = m_locations.get(location.getName());
		if (loc.getMarker() != null) {
			m_mapPanel.getMapWidget().removeOverlay(loc.getMarker());
		}
		m_locations.remove(location.getName());
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
	    for(GoogleMapsLocation location : m_locations.values()) {
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
        final LatLngBounds bnds = LatLngBounds.newInstance();
    	for (GoogleMapsLocation l : m_locations.values()) {
    		if (l.getLatLng() != null) {
    			bnds.extend(toLatLng(l.getLatLng()));
    		} else if (l.getMarker() != null) {
    			bnds.extend(l.getMarker().getLatLng());
    		}
    	}
    	GWTBounds b = toGWTBounds(bnds);
        return b;
    }

	protected void updateMarker(final Location location) {
        GoogleMapsLocation oldLocation = m_locations.get(location.getName());
        addAndMergeLocation(oldLocation, new GoogleMapsLocation(location));

        if (oldLocation == null) {
            placeMarker(m_locations.get(location.getName()));
        } else if (!oldLocation.getLocationMonitorState().getStatus().equals(location.getLocationMonitorState().getStatus())) {
            placeMarker(m_locations.get(location.getName()));
        }

        locationUpdateComplete(location);
	}

	private void placeMarker(final GoogleMapsLocation location) {
	    final Marker oldMarker = location.getMarker();
	    final Marker newMarker = createMarker(location);

	    if (oldMarker != null) {
	    	m_mapPanel.getMapWidget().removeOverlay(oldMarker);
	    }
	    m_mapPanel.getMapWidget().addOverlay(newMarker);
	}

    private void addAndMergeLocation(final GoogleMapsLocation oldLocation, final GoogleMapsLocation newLocation) {
        if(oldLocation != null) {
            m_locations.put(newLocation.getName(), mergeLocations(oldLocation, newLocation));
        }else {
            m_locations.put(newLocation.getName(), newLocation);
        }
        
    }

	private GoogleMapsLocation mergeLocations(final GoogleMapsLocation oldLocation, final GoogleMapsLocation newLocation) {
		if (newLocation.getLocationMonitorState() == null)
			newLocation.setLocationMonitorState(oldLocation.getLocationMonitorState());
		if (newLocation.getLatLng() == null)
			newLocation.setLatLng(oldLocation.getLatLng());
		if (newLocation.getMarker() == null)
			newLocation.setMarker(oldLocation.getMarker());
		return newLocation;
	}

	private Marker createMarker(final GoogleMapsLocation location) {
		Icon icon = Icon.newInstance();
		icon.setIconSize(Size.newInstance(32, 64));
		icon.setImageURL("images/icon-" + location.getLocationMonitorState().getStatus().toString() + ".png");

		final MarkerOptions markerOptions = MarkerOptions.newInstance();
		markerOptions.setAutoPan(true);
		markerOptions.setClickable(true);
		markerOptions.setTitle(location.getName());
		markerOptions.setIcon(icon);
		final GWTLatLng latLng = location.getLatLng();
		final Marker m = new Marker(toLatLng(latLng), markerOptions);
		m.addMarkerClickHandler(new DefaultMarkerClickHandler(location.getName()));

		//Ben's Edits
//		Marker m = location.getMarker();
//		if (m == null) {
//			Icon icon = Icon.newInstance();
//			icon.setIconSize(Size.newInstance(32, 32));
//			icon.setIconAnchor(Point.newInstance(16, 32));
//			icon.setImageURL("images/icon-" + location.getLocationMonitorState().getStatus().toString() + ".png");
//	
//			final MarkerOptions markerOptions = MarkerOptions.newInstance();
//			markerOptions.setAutoPan(true);
//			markerOptions.setClickable(true);
//			markerOptions.setTitle(location.getName());
//			markerOptions.setIcon(icon);
//			final GWTLatLng latLng = location.getLatLng();
//			m = new Marker(transformLatLng(latLng), markerOptions);
//			m.addMarkerClickHandler(new DefaultMarkerClickHandler(location.getName()));
//		} else {
//			m.setImage("images/icon-" + location.getLocationMonitorState().getStatus().toString() + ".png");
//		}
		location.setMarker(m);

		return m;
	}

	@Override
	public void reportError(final String errorMessage, final Throwable throwable) {
		// FIXME: implement error reporting in UI
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
