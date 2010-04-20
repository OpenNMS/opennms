package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.FilterPanel.Filters;
import org.opennms.features.poller.remote.gwt.client.FilterPanel.FiltersChangedEvent;
import org.opennms.features.poller.remote.gwt.client.InitializationCommand.DataLoader;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagSelectedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

/**
 * <p>This class implements both {@link LocationManager} (the model portion of the webapp) and
 * {@link RemotePollerPresenter} (the controller portion of the webapp code). It is responsible
 * for maintaining the knowledgebase of {@link Location} objects and responding to events triggered when:</p>
 * <ul>
 * <li>{@link Location} instances are added or updated</li>
 * <li>the UI elements are clicked on by the user</li>
 * </ul>
 * 
 * <p>If this class ever grows too large, we can split it into separate model and controller classes.</p>
 */
public class DefaultLocationManager implements LocationManager, RemotePollerPresenter {

	protected final HandlerManager m_eventBus;

	private final HandlerManager m_handlerManager = new HandlerManager(this);

	private final LocationStatusServiceAsync m_remoteService = GWT.create(LocationStatusService.class);
	
	private final Map<String,BaseLocation> m_locations = new HashMap<String,BaseLocation>();
	
	private String m_selectedTag = null;
	
	private final MapPanel m_mapPanel;

	private final LocationPanel m_locationPanel;

	private final SplitLayoutPanel m_panel;

	private boolean updated = false;

	public DefaultLocationManager(final HandlerManager eventBus, final SplitLayoutPanel panel, final LocationPanel locationPanel, MapPanel mapPanel) {
		m_eventBus = eventBus;
		m_panel = panel;
		m_locationPanel = locationPanel;
		m_mapPanel = mapPanel;

		// Register for all relevant events thrown by the UI components
		m_eventBus.addHandler(LocationPanelSelectEvent.TYPE, this); 
		m_eventBus.addHandler(LocationsUpdatedEvent.TYPE, this); 
		m_eventBus.addHandler(MapPanelBoundsChangedEvent.TYPE, this); 
        m_eventBus.addHandler(FiltersChangedEvent.TYPE, this); 
        m_eventBus.addHandler(TagSelectedEvent.TYPE, this); 
	}

    public void initialize() {
        DeferredCommand.addCommand(new InitializationCommand(this, createFinisher(), createDataLoaders()));
    }
    
    protected MapPanel getMapPanel() {
        return m_mapPanel;
    }

    protected Map<String, BaseLocation> getLocations() {
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

    public List<String> getAllLocationNames() {
        List<String> retval = new ArrayList<String>();
        for (Location location : this.getAllLocations()) {
            retval.add(location.getLocationInfo().getName());
        }
        return retval;
    }

    protected GWTBounds getLocationBounds() {
        BoundsBuilder bldr = new BoundsBuilder();
        for (Location l : getLocations().values()) {
            bldr.extend(l.getLocationInfo().getLatLng());
        }
        return bldr.getBounds();
    }

    public Location createOrUpdateLocation(final LocationInfo info) {
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
    }

    public void fitMapToLocations() {
    	getMapPanel().setBounds(getLocationBounds());
    }

    public List<Location> getVisibleLocations() {
        List<Location> visibleLocations = new ArrayList<Location>();
        GWTBounds bounds = getMapPanel().getBounds();
        for(BaseLocation location : getLocations().values()) {
            if(location.isVisible(bounds)) {
                if (m_selectedTag == null) {
                    visibleLocations.add(location);
                } else {
                    if (
                            location.getLocationInfo().getTags() != null && 
                            location.getLocationInfo().getTags().contains(m_selectedTag)
                    ) {
                        visibleLocations.add(location);
                    }
                }
            }
        }
    
        return visibleLocations;
    }

    /**
     * Handler triggered when a user clicks on a specific location record.
     */
    public void onLocationSelected(final LocationPanelSelectEvent event) {
        String locationName = event.getLocationName();
        final Location location = getLocations().get(locationName);
        if (location == null) {
            return;
        }
        getMapPanel().showLocationDetails(location);
    }

    /**
     * Refresh the list of locations whenever the map panel boundaries change.
     */
    public void onBoundsChanged(final MapPanelBoundsChangedEvent e) {
        m_locationPanel.update(this);
    }

    /**
     * Refresh the list of locations whenever they are updated.
     */
    public void onLocationsUpdated(final LocationsUpdatedEvent e) {
        m_locationPanel.update(this);
    }

    /**
     * Invoked by the {@link LocationUpdatedRemoteEvent} and {@link LocationsUpdatedRemoteEvent}
     * events.
     */
    public void updateLocation(final LocationInfo info) {
        if (info == null) return;
        
        Location location = createOrUpdateLocation(info);
        
        getMapPanel().placeMarker(location);
        
    }

    /**
     * Invoked by the {@link ApplicationUpdatedRemoteEvent} and {@link ApplicationsUpdatedRemoteEvent}
     * events.
     */
    public void updateApplication(final ApplicationInfo info) {
    	if (info == null) return;

    	// FIXME: implement
    }
    
    protected void setUpdated(boolean updated) {
        this.updated = updated;
    }

    protected boolean isUpdated() {
        return updated;
    }

    /**
     * Invoked by the {@link UpdateCompleteRemoteEvent} event.
     */
    public void updateComplete() {
    	if (!isUpdated()) {
    		DeferredCommand.addPause();
    		DeferredCommand.addCommand(new IncrementalCommand() {
    			public boolean execute() {
    				fitMapToLocations();
    				setUpdated(true);
    				return false;
    			}
    		});
    	}
    }

    public void onFiltersChanged(Filters filters) {
        // TODO: Update the map panel and LHN with filtered items
    }

    public void onTagSelected(String tagName) {
        m_selectedTag = tagName;
        m_locationPanel.update(this);
    }

    public void onTagCleared() {
        m_selectedTag = null;
        m_locationPanel.update(this);
    }
 }
