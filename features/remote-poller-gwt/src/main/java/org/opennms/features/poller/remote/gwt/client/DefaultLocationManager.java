package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.FilterPanel.Filters;
import org.opennms.features.poller.remote.gwt.client.FilterPanel.FiltersChangedEvent;
import org.opennms.features.poller.remote.gwt.client.FilterPanel.StatusSelectionChangedEvent;
import org.opennms.features.poller.remote.gwt.client.InitializationCommand.DataLoader;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagClearedEvent;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagSelectedEvent;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationSelectedEvent;
import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEvent;
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
import com.google.gwt.user.client.Window;
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
	private final Set<ApplicationInfo> m_applications = new TreeSet<ApplicationInfo>(new Comparator<ApplicationInfo>() {
	    public int compare(ApplicationInfo a, ApplicationInfo b) {
	        return a.getName().compareTo(b.getName());
	    }
	});

	private String m_selectedTag = null;
	
	private final Set<Status> m_selectedStatuses = new HashSet<Status>();
	
	private final MapPanel m_mapPanel;

	private LocationPanel m_locationPanel;

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
		m_eventBus.addHandler(TagClearedEvent.TYPE, this);
		m_eventBus.addHandler(StatusSelectionChangedEvent.TYPE, this);
		m_eventBus.addHandler(ApplicationSelectedEvent.TYPE, this);
		m_eventBus.addHandler(GWTMarkerClickedEvent.TYPE, this);

		// Add some test data
		m_applications.addAll(getApplicationInfoTestData());
		ArrayList<ApplicationInfo> applicationList = new ArrayList<ApplicationInfo>();
		applicationList.addAll(m_applications);
		getLocationPanel().updateApplicationList(applicationList);
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
                    // Append the map panel to the main SplitPanel
                    getPanel().add(getMapPanel().getWidget());
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

    public Set<String> getAllLocationNames() {
        Set<String> retval = new TreeSet<String>();
        for (Location location : this.getLocations().values()) {
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
        getLocationPanel().updateApplicationNames(this.getAllApplicationNames());
        return location;
    }

    public void createOrUpdateApplication(ApplicationInfo info) {
        m_applications.remove(info);
        m_applications.add(info);
        getLocationPanel().updateApplicationNames(this.getAllApplicationNames());
    }

    public void reportError(final String errorMessage, final Throwable throwable) {
    	// FIXME: implement error reporting in UI
    }

    public void fitMapToLocations() {
    	getMapPanel().setBounds(getLocationBounds());
    }

    /**
     * TODO: Figure out if this public function is necessary or if we can get by just responding to
     * incoming events.
     */
    public ArrayList<Location> getVisibleLocations() {
        ArrayList<Location> visibleLocations = new ArrayList<Location>();
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

    public List<String> getTagsOnVisibleLocations() {
        List<String> retval = new ArrayList<String>();
        for (Location location : this.getVisibleLocations()) {
            retval.addAll(location.getLocationInfo().getTags());
        }
        return retval;
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
        
        //TODO: Implement the content string for this method
        showLocationDetails(locationName, location);
    }

    private void showLocationDetails(String locationName, final Location location) {
        getMapPanel().showLocationDetails(locationName, locationName + "(" + location.getLocationInfo().getArea() + ")", "Need to implement content");
    }

    /**
     * Refresh the list of locations whenever the map panel boundaries change.
     */
    public void onBoundsChanged(final MapPanelBoundsChangedEvent e) {
        // Update the contents of the tag panel
        getLocationPanel().clearTagPanel();
        getLocationPanel().addAllTags(this.getTagsOnVisibleLocations());

        // Update the list of objects in the LHN
        getLocationPanel().updateLocationList(this.getVisibleLocations());

        // TODO: Update the application list based on map boundries?? 
        // TODO: Update the list of selectable applications based on the visible locations??
    }

    /**
     * Refresh the list of locations whenever they are updated.
     */
    public void onLocationsUpdated(final LocationsUpdatedEvent e) {
        // Update the contents of the tag panel
        getLocationPanel().clearTagPanel();
        getLocationPanel().addAllTags(this.getTagsOnVisibleLocations());

        getLocationPanel().updateApplicationNames(this.getAllApplicationNames());
        getLocationPanel().updateLocationList(this.getVisibleLocations());
    }

    /**
     * Invoked by the {@link LocationUpdatedRemoteEvent} and {@link LocationsUpdatedRemoteEvent}
     * events.
     */
    public void updateLocation(final LocationInfo info) {
        if (info == null) return;

        // Update the location information in the model
        Location location = createOrUpdateLocation(info);

        // Update the icon/caption in the LHN
        // Use an ArrayList so that it has good random-access efficiency
        // since the pageable lists use get() to fetch based on index.
        ArrayList<Location> locationList = new ArrayList<Location>();
        locationList.addAll(m_locations.values());
        getLocationPanel().updateLocationList(locationList);

        // Update the icon in the map
        GWTMarker m = new GWTMarker(location);
        getMapPanel().placeMarker(m);
    }

    /**
     * Invoked by the {@link ApplicationUpdatedRemoteEvent} and {@link ApplicationsUpdatedRemoteEvent}
     * events.
     */
    public void updateApplication(final ApplicationInfo info) {
        if (info == null) return;

        // Update the location information in the model
        createOrUpdateApplication(info);

        // Update the icon/caption in the LHN
        // Use an ArrayList so that it has good random-access efficiency
        // since the pageable lists use get() to fetch based on index.
        ArrayList<ApplicationInfo> applicationList = new ArrayList<ApplicationInfo>();
        applicationList.addAll(m_applications);
        getLocationPanel().updateApplicationList(applicationList);

        // TODO: Update the icon in the map
        // Pseudocode:
        // for (Location location : info.getAllLocations()) {
        //    GWTMarker m = new GWTMarker(location);
        //    getMapPanel().placeMarker(m);
        // }
    }

    /**
     * Invoked by the {@link UpdateCompleteRemoteEvent} event.
     */
    public void updateComplete() {
    	if (!updated) {
    		DeferredCommand.addPause();
    		DeferredCommand.addCommand(new IncrementalCommand() {
    			public boolean execute() {
    				fitMapToLocations();
    				updated = true;
    				return false;
    			}
    		});
    	}
    }

    public void onFiltersChanged(Filters filters) {
        // TODO: Update state inside of this object to track the filter state (if necessary)
        // TODO: Update markers on the map panel
        // TODO: Update the list of objects in the LHN
    }

    public void onTagSelected(String tagName) {
        // Update state inside of this object to track the selected tag
        m_selectedTag = tagName;
        getLocationPanel().updateLocationList(this.getVisibleLocations());
    }

    public void onTagCleared() {
        // Update state inside of this object to track the selected tag
        m_selectedTag = null;
        // TODO: Update markers on the map panel
        // Update the list of objects in the LHN
        getLocationPanel().updateLocationList(this.getVisibleLocations());
    }

    /**
     * Fetch a list of all application names.
     */
    public Set<String> getAllApplicationNames() {
        Set<String> retval = new TreeSet<String>();
        for (ApplicationInfo application : m_applications) {
            retval.add(application.getName());
        }
        return retval;
    }

    public ApplicationInfo getApplicationInfo(String name) {
        if (name == null) {
            return null;
        }

        for (ApplicationInfo app : m_applications) {
            if (name.equals(app.getName())) {
                return app;
            }
        }
        return null;
    }

    public Location getLocation(String locationName) {
        return getLocations().get(locationName);
    }

    /**
     * TODO: Figure out if this public function is necessary or if we can get by just responding to
     * incoming events.
     */
    public ArrayList<ApplicationInfo> getVisibleApplications() {
        // TODO: Apply selected sorting
        ArrayList<ApplicationInfo> retval = new ArrayList<ApplicationInfo>();
        for (Location location : getVisibleLocations()) {
            // retval.add();
        }
        return retval;
    }

    /**
     * Remove this function once we have real data.
     */
    @Deprecated
    private static List<ApplicationInfo> getApplicationInfoTestData() {
        List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();

        for(int i = 0; i < 10 ; i++) {
            ApplicationInfo application = new ApplicationInfo();
            application.setId(i);
            application.setName("name: " + i);
            application.setStatus(Status.UP);
            application.setLocations(getLocationSetTestData());
            application.setServices(getGWTMonitoredServiceTestData());
            apps.add(application);
        }

        return apps;
    }

    /**
     * Remove this function once we have real data.
     */
    @Deprecated
    private static Set<GWTMonitoredService> getGWTMonitoredServiceTestData() {
        Set<GWTMonitoredService> services = new HashSet<GWTMonitoredService>();
        GWTMonitoredService service = new GWTMonitoredService();
        service.setServiceName("HTTP");
        services.add(service);
        return services;
    }

    /**
     * Remove this function once we have real data.
     */
    @Deprecated
    private static Set<String> getLocationSetTestData() {
        Set<String> locations = new HashSet<String>();
        locations.add("19");
        return locations;
    }

    public void onGWTMarkerClicked(GWTMarkerClickedEvent event) {
        GWTMarker marker = event.getMarker();
        showLocationDetails(marker.getName(), marker.getLocation());
    }

    LocationPanel getLocationPanel() {
        return m_locationPanel;
    }

    public void onStatusSelectionChanged(Status status, boolean selected) {
        if (selected) {
            m_selectedStatuses.add(status);
        } else {
            m_selectedStatuses.remove(status);
        }
        // TODO: Call function to update relevant UI elements
    }

    public void onApplicationSelected(ApplicationSelectedEvent event) {
        // TODO: Add the application to the selected application list
        // m_locationPanel.filterPanel.SOMETHING
        
        Window.alert("YOU CLICKED ON " + event.getAppInfo().getName());
    }
}
