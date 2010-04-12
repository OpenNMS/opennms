package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.InitializationCommand.DataLoader;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEventHandler;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SplitLayoutPanel;


public class DefaultLocationManager implements LocationManager {

	protected final HandlerManager m_eventBus;

	private final HandlerManager m_handlerManager = new HandlerManager(this);

	private final LocationStatusServiceAsync m_remoteService = GWT.create(LocationStatusService.class);
	
	private final Map<String,BaseLocation> m_locations = new HashMap<String,BaseLocation>();
	
	private final MapPanel m_mapPanel;

    private final SplitLayoutPanel m_panel;

    private boolean updated = false;

	public DefaultLocationManager(final HandlerManager eventBus, final SplitLayoutPanel panel, MapPanel mapPanel) {
		m_eventBus = eventBus;
		m_panel = panel;
		m_mapPanel = mapPanel;
	}

    public void initialize() {
        DeferredCommand.addCommand(new InitializationCommand(this, createFinisher(), createDataLoaders()));
    }
    
    public MapPanel getMapPanel() {
        return m_mapPanel;
    }

    public Map<String, BaseLocation> getLocations() {
        return m_locations;
    }

    private Runnable createFinisher() {
        return new Runnable() {
            public void run() {
                initializationComplete();
            }
        };
    }

    protected DataLoader[] createDataLoaders() {
        return new DataLoader[] {
            new DataLoader() {
                @Override
                public void onLoaded() {
                    initializeMapWidget();
                }
            },
             new EventServiceInitializer(this)
        };
    }

    protected void initializationComplete() {
        m_handlerManager.fireEvent(new LocationManagerInitializationCompleteEvent());
    }
    
    protected LocationStatusServiceAsync getRemoteService() {
        return m_remoteService;
    }
    
    public void addLocationManagerInitializationCompleteEventHandler(LocationManagerInitializationCompleteEventHander handler) {
        m_handlerManager.addHandler(LocationManagerInitializationCompleteEvent.TYPE, handler);
    };
    
    protected void displayDialog(final String title, final String contents) {
    	final DialogBox db = new DialogBox();
    	db.setAutoHideEnabled(true);
    	db.setModal(true);
    	db.setText(title);
    	db.setWidget(new Label(contents, true));
    	db.show();
    }

    protected SplitLayoutPanel getPanel() {
        return m_panel;
    }

    public List<Location> getAllLocations() {
    	final List<Location> locations = new ArrayList<Location>(getLocations().values());
    	Collections.sort(locations);
    	return locations;
    }

    protected GWTBounds getLocationBounds() {
        BoundsBuilder bldr = new BoundsBuilder();
        for (Location l : getLocations().values()) {
            bldr.extend(l.getLocationInfo().getLatLng());
        }
        return bldr.getBounds();
    }

    protected Location getLocation(final LocationInfo info) {
        BaseLocation location = getLocations().get(info.getName());
        if(location == null) {
            location = new BaseLocation(info);
            getLocations().put(info.getName(), location);
        }else {
            location.setLocationInfo(info);
        }
        return location;
    }

    public void reportError(final String errorMessage, final Throwable throwable) {
    	// FIXME: implement error reporting in UI
    }

    protected void initializeMapWidget() {
        getPanel().add(getMapPanel().getWidget());
        
        getMapPanel().addMapPanelBoundsChangedEventHandler(new MapPanelBoundsChangedEventHandler() {
            
            public void onBoundsChanged(MapPanelBoundsChangedEvent event) {
                m_eventBus.fireEvent(new LocationsUpdatedEvent(DefaultLocationManager.this));
            }
        });
        
        m_eventBus.addHandler(LocationPanelSelectEvent.TYPE, new LocationPanelSelectEventHandler() {
    
            public void onLocationSelected(final LocationPanelSelectEvent event) {
                selectLocation(event.getLocationName());
            }
        }); 
    }

    public void fitToMap() {
    	getMapPanel().setBounds(getLocationBounds());
    }

    public List<Location> getVisibleLocations() {
        List<Location> visibleLocations = new ArrayList<Location>();
        GWTBounds bounds = getMapPanel().getBounds();
        for(BaseLocation location : getLocations().values()) {
            if(location.isVisible(bounds)) {
                visibleLocations.add(location);
            }
        }
    
        return visibleLocations;
    }

    public void selectLocation(String locationName) {
        final Location location = getLocations().get(locationName);
        if (location == null) {
            return;
        }
        getMapPanel().showLocationDetails(location);
    }

    public void updateLocation(final LocationInfo info) {
        if (info == null) return;
        
        Location location = getLocation(info);
        
        getMapPanel().placeMarker(location);
        
    }

    protected void setUpdated(boolean updated) {
        this.updated = updated;
    }

    protected boolean isUpdated() {
        return updated;
    }

    public void updateComplete() {
    	if (!isUpdated()) {
    		DeferredCommand.addPause();
    		DeferredCommand.addCommand(new IncrementalCommand() {
    			public boolean execute() {
    				fitToMap();
    				setUpdated(true);
    				return false;
    			}
    		});
    	}
    }

 }
