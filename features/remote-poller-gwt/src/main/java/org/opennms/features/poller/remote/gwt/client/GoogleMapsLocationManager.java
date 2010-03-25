package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.event.MapDragEndHandler;
import com.google.gwt.maps.client.event.MapMoveEndHandler;
import com.google.gwt.maps.client.event.MapZoomEndHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.utility.client.DefaultPackage;
import com.google.gwt.maps.utility.client.GoogleMapsUtility;
import com.google.gwt.maps.utility.client.mapiconmaker.MapIconMaker;
import com.google.gwt.maps.utility.client.mapiconmaker.MarkerIconOptions;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.RemoteEventServiceFactory;

public class GoogleMapsLocationManager extends AbstractLocationManager implements LocationManager {
//	private static final int MARKER_MAX_ZOOM = 18;

	private final Application m_application;
	private final SplitLayoutPanel m_panel;

	private MapWidget m_mapWidget;
//	private MarkerManager m_markerManager;
	private LocationListener m_locationListener;
	private LocationManager m_locationManager;
	private Geocoder m_geocoder;

	private final Map<String,GoogleMapsLocation> m_locations = new HashMap<String,GoogleMapsLocation>();
	private boolean updated = false;
	private final HashMap<String, Location> m_visibleLocations = new HashMap<String, Location>();

	public GoogleMapsLocationManager(Application application, final HandlerManager eventBus, final SplitLayoutPanel panel) {
		super(eventBus);
    	m_application = application;
		m_panel = panel;
		m_locationManager = this;
	}

    private void setupEventHandlers() {
        m_eventBus.addHandler(LocationPanelSelectEvent.TYPE, new LocationPanelSelectEventHandler() {
            
            public void onLocationSelected(final LocationPanelSelectEvent event) {
                selectLocation(event.getLocationName());
            }
        });
        
        m_mapWidget.addMapDragEndHandler(new MapDragEndHandler() {

            public void onDragEnd(MapDragEndEvent event) {
                checkAllVisibleLocations();
            }

            
        });
        
        m_mapWidget.addMapZoomEndHandler(new MapZoomEndHandler() {

            public void onZoomEnd(MapZoomEndEvent event) {
                checkAllVisibleLocations();
            }
            
        });
        
        m_mapWidget.addMapMoveEndHandler(new MapMoveEndHandler() {

            public void onMoveEnd(MapMoveEndEvent event) {
                checkAllVisibleLocations();
            }
            
        });
        
    }

	private void checkAllVisibleLocations() {
	    for(GoogleMapsLocation location : m_locations.values()) {
	        if(checkIfLocationIsVisibleOnMap(location)) {
	            m_visibleLocations.put(location.getName(), location);
	        } else {
	            if(m_visibleLocations.containsKey(location.getName())) {
	                m_visibleLocations.remove(location.getName());
	            }
	        }
	    }
	    m_eventBus.fireEvent(new LocationsUpdatedEvent(this));
	}

	@Override
	public void initialize() {
		DeferredCommand.addCommand(new InitializationCommand() {
			private boolean m_inProgress = false;

			@Override
			protected void loadMapApi() throws InitializationException {
				m_inProgress = true;
				AjaxLoader.init(getApiKey());
				AjaxLoader.loadApi("maps", "2.x", new Runnable() {
					public void run() {
						m_inProgress = false;
					}
					
				}, null);
			}

			@Override
			protected boolean mapApiLoaded() throws InitializationException {
				if (m_inProgress) {
					return false;
				}

				m_mapWidget = new MapWidget();
				m_mapWidget.setSize("100%", "100%");
				m_mapWidget.setUIToDefault();
				m_mapWidget.addControl(new LargeMapControl());
//				m_mapWidget.setZoomLevel(10);
				m_mapWidget.setContinuousZoom(true);
				m_mapWidget.setScrollWheelZoomEnabled(true);

				m_panel.add(m_mapWidget);

				m_geocoder = new GoogleMapsGeocoder();
				
				Window.addResizeHandler(new ResizeHandler() {
					public void onResize(final ResizeEvent resizeEvent) {
						if (m_mapWidget != null) {
							m_mapWidget.checkResizeAndCenter();
						}
					}
				});

				return true;
			}

			@Override
			protected void loadMarkerApi() throws InitializationException {
				m_inProgress = true;
				GoogleMapsUtility.loadUtilityApi(new Runnable() {
					public void run() {
						m_inProgress = false;
					}
				}, DefaultPackage.MARKER_MANAGER);
			}

			@Override
			protected boolean markerApiLoaded() throws InitializationException {
				return (!m_inProgress);
			}
			
			@Override
			protected void loadIconApi() throws InitializationException {
				m_inProgress = true;
				GoogleMapsUtility.loadUtilityApi(new Runnable() {
					public void run() {
						m_inProgress = false;
					}
				}, DefaultPackage.MAP_ICON_MAKER);
			}

			@Override
			protected boolean iconApiLoaded() throws InitializationException {
				return (!m_inProgress);
			}

			@Override
			protected void loadEventBackend() throws InitializationException {
				m_inProgress = true;
				m_locationListener = new DefaultLocationListener(m_locationManager);
				final RemoteEventService eventService = RemoteEventServiceFactory.getInstance().getRemoteEventService();
				eventService.addListener(LocationManager.LOCATION_EVENT_DOMAIN, m_locationListener);

				setupEventHandlers();

				m_remoteService.start(new AsyncCallback<Void>() {
					public void onFailure(Throwable throwable) {
						Log.debug("unable to start location even service backend", throwable);
						throw new InitializationException("remote service start failed", throwable);
					}

					public void onSuccess(Void voidArg) {
						m_inProgress = false;
					}
				});
			}

			@Override
			protected boolean eventBackendLoaded() throws InitializationException {
				return (!m_inProgress);
			}

			@Override
			protected void finished() throws InitializationException {
				m_application.finished();
			}
		});
	}

	@Override
	public void updateLocation(final Location location) {
		if (location == null) return;
		GWTLatLng latLng = getGeolocation(location);
		if (latLng != null) {
			GoogleMapsLocation loc = new GoogleMapsLocation(location);
			loc.setLatLng(latLng);
			updateMarker(loc);
		} else {
			m_geocoder.getLatLng(location.getGeolocation(), new LatLngMarkerPlacer(location));
		}
	}
	
	@Override
	public void removeLocation(final Location location) {
		if (location == null) return;
		GoogleMapsLocation loc = m_locations.get(location.getName());
		if (loc.getMarker() != null) {
			m_mapWidget.removeOverlay(loc.getMarker());
		}
		m_locations.remove(location.getName());
		
		if(m_visibleLocations.containsKey(location.getName())) {
		    m_visibleLocations.remove(location.getName());
		}
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
    public List<Location> getVisibleLocations() {
		final List<Location> locations = new ArrayList<Location>();
		final List<String> keys = new ArrayList<String>(m_visibleLocations.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			locations.add(m_visibleLocations.get(key));
		}
		return locations;
    }

	@Override
    public void selectLocation(String locationName) {
    	final GoogleMapsLocation location = m_locations.get(locationName);
    	if (location == null) {
    		return;
    	}
		final Marker m = location.getMarker();
		final GWTLatLng latLng = location.getLatLng();
		m_mapWidget.savePosition();
		m_mapWidget.setCenter(transformLatLng(latLng));
		if (m != null) {
			m_mapWidget.getInfoWindow().open(m, Utils.getInfoWindowForLocation(location));
		}
	}

	private LatLng transformLatLng(final GWTLatLng latLng) {
		return LatLng.newInstance(latLng.getLatitude(), latLng.getLongitude());
	}

	@Override
    public void fitToMap() {
    	final LatLngBounds bounds = LatLngBounds.newInstance();
    	for (GoogleMapsLocation l : m_locations.values()) {
    		if (l.getLatLng() != null) {
    			bounds.extend(transformLatLng(l.getLatLng()));
    		} else if (l.getMarker() != null) {
    			bounds.extend(l.getMarker().getLatLng());
    		}
    	}
    	m_mapWidget.setCenter(bounds.getCenter(), m_mapWidget.getBoundsZoomLevel(bounds));
    }

	private int checkOutOfBounds(int size, int maxRows) {
		return maxRows > size? size : maxRows;
    }
	
	private void updateMarker(final GoogleMapsLocation location) {
	    if (location == null) {
            return;
        }

	    if (location.getLatLng() == null) {
            return;
        }
	    
        GoogleMapsLocation oldLocation = m_locations.get(location.getName());
        addAndMergeLocation(oldLocation, location);

        if (oldLocation == null) {
            placeMarker(m_locations.get(location.getName()));
        } else if (!oldLocation.getLocationMonitorState().getStatus().equals(location.getLocationMonitorState().getStatus())) {
            placeMarker(m_locations.get(location.getName()));
        }

        locationUpdateComplete(location);
        if (!isLocationUpdateInProgress()) {
        	checkAllVisibleLocations();
        }
	}

	private void placeMarker(final GoogleMapsLocation location) {
	    final Marker oldMarker = location.getMarker();
	    final Marker newMarker = createMarker(location);

	    if (oldMarker != null) {
	    	m_mapWidget.removeOverlay(oldMarker);
	    }
	    m_mapWidget.addOverlay(newMarker);
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
		final GWTLatLng latLng = location.getLatLng();
		final Marker m = new Marker(transformLatLng(latLng), markerOptions);
		m.addMarkerClickHandler(new DefaultMarkerClickHandler(location.getName()));
		location.setMarker(m);

		return m;
	}

	@Override
	public void reportError(final String errorMessage, final Throwable throwable) {
		// FIXME: implement error reporting in UI
	}
	
	private boolean checkIfLocationIsVisibleOnMap(GoogleMapsLocation location) {
        return m_mapWidget.getBounds().containsLatLng(transformLatLng(location.getLatLng()));
    }

	private final class LatLngMarkerPlacer implements AsyncCallback<GWTLatLng> {
		private final GoogleMapsLocation m_location;

		private LatLngMarkerPlacer(final Location location) {
			if (location instanceof GoogleMapsLocation) {
				m_location = (GoogleMapsLocation)location;
			} else {
				m_location = new GoogleMapsLocation(location);
			}
		}

		public void onSuccess(final GWTLatLng point) {
			cacheGeolocation(m_location.getGeolocation(), point);
			m_location.setLatLng(point);
			updateMarker(m_location);
		}

		public void onFailure(Throwable throwable) {
			m_location.setLatLng(GWTLatLng.getDefault());
			reportError("unable to retrieve latitude and longitude for " + m_location.getName(), throwable);
			updateMarker(m_location);
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
