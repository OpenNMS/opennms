package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.opennms.features.poller.remote.gwt.client.events.ApplicationDeselectedEvent;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationDetailsRetrievedEvent;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationSelectedEvent;
import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.utils.BoundsBuilder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

/**
 * <p>
 * This class implements both {@link LocationManager} (the model portion of
 * the webapp) and {@link RemotePollerPresenter} (the controller portion of
 * the webapp code). It is responsible for maintaining the knowledgebase of
 * {@link Location} objects and responding to events triggered when:
 * </p>
 * <ul>
 * <li>{@link Location} instances are added or m_updated</li>
 * <li>the UI elements are clicked on by the user</li>
 * </ul>
 * <p>
 * If this class ever grows too large, we can split it into separate model and
 * controller classes.
 * </p>
 */
public class DefaultLocationManager implements LocationManager, RemotePollerPresenter {

    protected final HandlerManager m_eventBus;

    private final HandlerManager m_handlerManager = new HandlerManager(this);

    private final LocationStatusServiceAsync m_remoteService = GWT.create(LocationStatusService.class);

    private final Map<String, LocationInfo> m_locations = new HashMap<String, LocationInfo>();

    private final Set<ApplicationInfo> m_applications = new HashSet<ApplicationInfo>();

    private String m_selectedTag = null;

    private final Set<Status> m_selectedStatuses = new HashSet<Status>();

    private final MapPanel m_mapPanel;

    private LocationPanel m_locationPanel;

    private final SplitLayoutPanel m_panel;

    private boolean m_updated = false;

    private boolean m_locationViewActive = true;

    private Set<ApplicationInfo> m_selectedApplications = new HashSet<ApplicationInfo>();

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
        m_eventBus.addHandler(ApplicationDeselectedEvent.TYPE, this);
        m_eventBus.addHandler(ApplicationSelectedEvent.TYPE, this);
        m_eventBus.addHandler(GWTMarkerClickedEvent.TYPE, this);

        // by default, we select all statuses until the UI says otherwise
        for (final Status s : Status.values()) {
            m_selectedStatuses.add(s);
        }
    }

    public void initialize() {
        DeferredCommand.addCommand(new InitializationCommand(this, createFinisher(), createDataLoaders()));
    }

    private Runnable createFinisher() {
        return new Runnable() {
            public void run() {
                initializationComplete();
            }
        };
    }

    protected DataLoader[] createDataLoaders() {
        return new DataLoader[] { new DataLoader() {
            @Override
            public void onLoaded() {
                // Append the map panel to the main SplitPanel
                getPanel().add(m_mapPanel.getWidget());
            }
        }, new EventServiceInitializer(this) };
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
        return new TreeSet<String>(m_locations.keySet());
    }

    protected GWTBounds getLocationBounds() {
        BoundsBuilder bldr = new BoundsBuilder();
        for (LocationInfo l : m_locations.values()) {
            bldr.extend(l.getLatLng());
        }
        return bldr.getBounds();
    }

    public void createOrUpdateLocation(final LocationInfo locationInfo) {
        if (locationInfo.getMarkerState() == null) {
            locationInfo.setMarkerState(getMarkerForLocation(locationInfo));
        }
        m_locations.put(locationInfo.getName(), locationInfo);
        m_locationPanel.updateApplicationNames(getAllApplicationNames());
    }

    public void createOrUpdateApplication(final ApplicationInfo applicationInfo) {
        if (applicationInfo.getLocations().size() == 0) {
            applicationInfo.setPriority(Long.MAX_VALUE);
        } else {
            applicationInfo.setPriority(0L);
            for (final String location : applicationInfo.getLocations()) {
                final LocationInfo locationInfo = m_locations.get(location);
                if (locationInfo != null) {
                    applicationInfo.setPriority(applicationInfo.getPriority() + locationInfo.getPriority());
                }
            }
        }
        m_applications.add(applicationInfo);
        m_locationPanel.updateApplicationNames(getAllApplicationNames());
    }

    public void reportError(final String errorMessage, final Throwable throwable) {
        // FIXME: implement error reporting in UI
    }

    public void fitMapToLocations() {
        m_mapPanel.setBounds(getLocationBounds());
    }

    /**
     * TODO: Figure out if this public function is necessary or if we can get
     * by just responding to incoming events.
     */
    public ArrayList<LocationInfo> getVisibleLocations() {
        // Use an ArrayList so that it has good random-access efficiency
        // since the pageable lists use get() to fetch based on index.
        final ArrayList<LocationInfo> visibleLocations = new ArrayList<LocationInfo>();
        for (LocationInfo location : m_locations.values()) {
            final GWTMarkerState markerState = location.getMarkerState();
            if (markerState.isSelected() && markerState.isVisible()) {
                visibleLocations.add(location);
            }
        }

        // TODO: this should use the current filter set eventually, for now
        // sort by priority, then name
        // for now, LocationInfo is Comparable and has a natural sort ordering
        // based on status, priority, and name
        Collections.sort(visibleLocations, new Comparator<LocationInfo>() {
            public int compare(LocationInfo o1, LocationInfo o2) {
                return o1.compareTo(o2);
            }
        });

        return visibleLocations;
    }

    private void updateAllMarkerStates() {
        for (final LocationInfo location : m_locations.values()) {
            final GWTMarkerState markerState = location.getMarkerState();

            // if it's within the map bounds, it's visible
            markerState.setVisible(location.isVisible(m_mapPanel.getBounds()));
            // markerState.setVisible(true);
            if (markerState.isVisible()) {
                // unless it's not in the list of selected statuses
                markerState.setVisible(m_selectedStatuses.contains(location.getStatusDetails().getStatus()));
            }

            boolean selected = false;
            if (m_locationViewActive || m_selectedApplications.size() == 0) {
                selected = true;
            } else {
                for (final ApplicationInfo app : m_selectedApplications) {
                    if (app.getLocations().contains(location.getName())) {
                        selected = true;
                        break;
                    }
                }
            }
            if (selected) {
                if (m_selectedTag != null) {
                    selected = location.getTags() != null && location.getTags().contains(m_selectedTag);
                }
            }
            markerState.setSelected(selected);
            m_mapPanel.placeMarker(markerState);
        }
    }

    public List<String> getAllTags() {
        final List<String> retval = new ArrayList<String>();
        for (final LocationInfo location : m_locations.values()) {
            retval.addAll(location.getTags());
        }
        return retval;
    }

    public List<String> getTagsOnVisibleLocations() {
        List<String> retval = new ArrayList<String>();
        for (LocationInfo location : getVisibleLocations()) {
            retval.addAll(location.getTags());
        }
        return retval;
    }

    /**
     * Handler triggered when a user clicks on a specific location record.
     */
    public void onLocationSelected(final LocationPanelSelectEvent event) {
        showLocationDetails(event.getLocationName());
    }

    private void showLocationDetails(final String locationName) {
        // TODO: this needs a callback to get the location details, and fill
        // in the content
        final LocationInfo loc = m_locations.get(locationName);
        m_remoteService.getLocationDetails(locationName, new AsyncCallback<LocationDetails>() {
            public void onFailure(final Throwable t) {
                m_mapPanel.showLocationDetails(locationName, "Error Getting Location Details",
                                               "<p>An error occurred getting the location details.</p>" + "<pre>"
                                                       + URL.encode(t.getMessage()) + "</pre>");
            }

            public void onSuccess(final LocationDetails locationDetails) {
                m_mapPanel.showLocationDetails(
                    locationName,
                    locationName + " (" + loc.getArea() + ")",
                    getLocationInfoDetails(loc, locationDetails)
                );
            }

        });
    }

    /**
     * Refresh the list of locations whenever the map panel boundaries change.
     */
    public void onBoundsChanged(final MapPanelBoundsChangedEvent e) {
        // make sure each location's marker is up-to-date
        updateAllMarkerStates();

        // Update the contents of the tag panel
        m_locationPanel.clearTagPanel();
        m_locationPanel.addAllTags(getAllTags());
        m_locationPanel.selectTag(m_selectedTag);

        // Update the list of objects in the LHN
        m_locationPanel.updateLocationList(getVisibleLocations());

        // TODO: Update the application list based on map boundries??
        // TODO: Update the list of selectable applications based on the
        // visible locations??
    }

    /**
     * Refresh the list of locations whenever they are m_updated.
     */
    public void onLocationsUpdated(final LocationsUpdatedEvent e) {
        // make sure each location's marker is up-to-date
        updateAllMarkerStates();

        // Update the contents of the tag panel
        m_locationPanel.clearTagPanel();
        m_locationPanel.addAllTags(getAllTags());
        m_locationPanel.selectTag(m_selectedTag);

        m_locationPanel.updateApplicationNames(getAllApplicationNames());
        m_locationPanel.updateLocationList(getVisibleLocations());
    }

    /**
     * Invoked by the {@link LocationUpdatedRemoteEvent} and
     * {@link LocationsUpdatedRemoteEvent} events.
     */
    public void updateLocation(final LocationInfo info) {
        if (info == null)
            return;

        // Update the location information in the model
        createOrUpdateLocation(info);

        // Update the icon in the map
        GWTMarkerState m = getMarkerForLocation(info);
        m_mapPanel.placeMarker(m);

        if (!m_updated) {
            return;
        }

        updateAllMarkerStates();

        // Update the icon/caption in the LHN
        m_locationPanel.updateLocationList(getVisibleLocations());
    }

    /**
     * Invoked by the {@link ApplicationUpdatedRemoteEvent} and
     * {@link ApplicationsUpdatedRemoteEvent} events.
     */
    public void updateApplication(final ApplicationInfo applicationInfo) {
        if (applicationInfo == null)
            return;

        // Update the application information in the model
        createOrUpdateApplication(applicationInfo);

        /*
         * Update the icon/caption in the LHN Use an ArrayList so that it has
         * good random-access efficiency since the pageable lists use get() to
         * fetch based on index. Note, that m_applications is a *HashSet* not
         * a *TreeSet* since TreeSets consider duplicates based on Comparable,
         * not equals
         */

        ArrayList<ApplicationInfo> applicationList = new ArrayList<ApplicationInfo>();
        applicationList.addAll(m_applications);
        Collections.sort(applicationList);
        m_locationPanel.updateApplicationList(applicationList);

        if (!m_updated) {
            return;
        }

        updateAllMarkerStates();

        for (final String locationName : applicationInfo.getLocations()) {
            GWTMarkerState m = getMarkerForLocation(locationName);
            m_mapPanel.placeMarker(m);
        }
    }

    /**
     * Invoked by the {@link UpdateCompleteRemoteEvent} event.
     */
    public void updateComplete() {
        if (!m_updated) {
            updateAllMarkerStates();
            fitMapToLocations();
            m_updated = true;
        }
    }

    public void onFiltersChanged(Filters filters) {
        // TODO: Update state inside of this object to track the filter state
        // (if necessary)
        // TODO: Update markers on the map panel
        // TODO: Update the list of objects in the LHN
    }

    public void onTagSelected(String tagName) {
        // Update state inside of this object to track the selected tag
        m_selectedTag = tagName;
        // make sure each location's marker is up-to-date
        updateAllMarkerStates();

        m_locationPanel.updateLocationList(getVisibleLocations());
    }

    public void onTagCleared() {
        // Update state inside of this object to track the selected tag
        m_selectedTag = null;

        // make sure each location's marker is up-to-date
        updateAllMarkerStates();

        // TODO: Update markers on the map panel
        // Update the list of objects in the LHN
        m_locationPanel.updateLocationList(getVisibleLocations());
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

    public LocationInfo getLocation(String locationName) {
        return m_locations.get(locationName);
    }

    public void onGWTMarkerClicked(GWTMarkerClickedEvent event) {
        GWTMarkerState markerState = event.getMarkerState();
        showLocationDetails(markerState.getName());
    }

    public void onStatusSelectionChanged(Status status, boolean selected) {
        if (selected) {
            m_selectedStatuses.add(status);
        } else {
            m_selectedStatuses.remove(status);
        }

        updateAllMarkerStates();

        m_locationPanel.updateLocationList(getVisibleLocations());
    }

    private GWTMarkerState getMarkerForLocation(final String locationName) {
        final LocationInfo location = m_locations.get(locationName);
        if (location != null) {
            return getMarkerForLocation(location);
        }
        return null;
    }

    private GWTMarkerState getMarkerForLocation(final LocationInfo location) {
        if (location == null) {
            return null;
        }
        GWTMarkerState state = location.getMarkerState();
        if (state == null) {
            state = new GWTMarkerState(location.getName(), location.getLatLng(), location.getStatusDetails().getStatus());
            location.setMarkerState(state);
        }
        state.setVisible(m_selectedStatuses.contains(location.getStatusDetails().getStatus()));
        return state;
    }

    public void onApplicationSelected(ApplicationSelectedEvent event) {
        // Add the application to the selected application list
        m_selectedApplications.add(event.getAppInfo());

        updateAllMarkerStates();

        // Update the list of selected applications in the panel
        m_locationPanel.updateSelectedApplications(m_selectedApplications);
        m_remoteService.getApplicationDetails(event.getAppInfo().getName(), new AsyncCallback<ApplicationDetails>() {

            public void onFailure(Throwable arg0) {
                // TODO: Do something on failure.
            }

            public void onSuccess(ApplicationDetails applicationDetails) {
                m_eventBus.fireEvent(new ApplicationDetailsRetrievedEvent(applicationDetails));
            }
        });
    }

    public void onApplicationDeselected(ApplicationDeselectedEvent event) {
        // Remove the application from the selected application list
        m_selectedApplications.remove(event.getAppInfo());

        updateAllMarkerStates();

        // Update the list of selected applications in the panel
        m_locationPanel.updateSelectedApplications(m_selectedApplications);
    }

    public void locationClicked() {
        m_locationViewActive = true;
        updateAllMarkerStates();
    }

    public void applicationClicked() {
        m_locationViewActive = false;
        updateAllMarkerStates();
    }

    public static String getLocationInfoDetails(final LocationInfo locationInfo, final LocationDetails locationDetails) {
        final LocationMonitorState state = locationDetails.getLocationMonitorState();

        int pollersStarted = state.getMonitorsStarted();
        int pollersStopped = state.getMonitorsStopped();
        int pollersDisconnected = state.getMonitorsDisconnected();
        Collection<String> serviceNames = state.getServiceNames();
        int servicesWithOutages = state.getServicesDown().size();
        int monitorsWithOutages = state.getMonitorsWithServicesDown().size();

        StringBuilder sb = new StringBuilder();

        sb.append("<div id=\"locationStatus\">");
        sb.append("<dl class=\"statusContents\">\n");

        sb.append("<dt class=\"").append(state.getStatusDetails().getStatus().getStyle()).append(" statusDt\">").append("Monitors:").append("</dt>\n");
        sb.append("<dd class=\"").append(state.getStatusDetails().getStatus().getStyle()).append(" statusDd\">");
        sb.append(pollersStarted + " started").append("<br>\n");
        sb.append(pollersStopped + " stopped").append("<br>\n");
        sb.append(pollersDisconnected + " disconnected").append("\n");
        sb.append("</dd>\n");

        if (pollersStarted > 0) {
            // If pollers are started, add on service information
            String styleName = Status.UP.getStyle();
            if (servicesWithOutages > 0) {
                if (monitorsWithOutages == pollersStarted) {
                    styleName = Status.DOWN.getStyle();
                } else {
                    styleName = Status.MARGINAL.getStyle();
                }
            }
            sb.append("<dt class=\"").append(styleName).append(" statusDt\">").append("Services:").append("</dt>\n");
            sb.append("<dd class=\"").append(styleName).append(" statusDd\">");
            sb.append(servicesWithOutages + " outages (of " + serviceNames.size() + " services)").append("<br>\n");
            sb.append(monitorsWithOutages + " pollers reporting errors").append("\n");
            sb.append("</dd>\n");
        }

        sb.append("</div>");
        return sb.toString();
    }
}
