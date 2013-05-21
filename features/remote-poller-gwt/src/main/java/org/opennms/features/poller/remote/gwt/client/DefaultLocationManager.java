/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.FilterPanel.Filters;
import org.opennms.features.poller.remote.gwt.client.FilterPanel.FiltersChangedEvent;
import org.opennms.features.poller.remote.gwt.client.FilterPanel.StatusSelectionChangedEvent;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagClearedEvent;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagSelectedEvent;
import org.opennms.features.poller.remote.gwt.client.data.AndFilter;
import org.opennms.features.poller.remote.gwt.client.data.ApplicationFilter;
import org.opennms.features.poller.remote.gwt.client.data.DataManager;
import org.opennms.features.poller.remote.gwt.client.data.LocationFilter;
import org.opennms.features.poller.remote.gwt.client.data.StatusFilter;
import org.opennms.features.poller.remote.gwt.client.data.TagFilter;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationDeselectedEvent;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationDetailsRetrievedEvent;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationSelectedEvent;
import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEvent;
import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerInfoWindowRefreshEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEventHandler;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;

import de.novanic.eventservice.client.event.RemoteEventService;

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
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultLocationManager implements LocationManager, RemotePollerPresenter {

    public class ApplicationUpdater implements Command {
        
        private ApplicationInfo m_applicationInfo;

        public ApplicationUpdater(ApplicationInfo applicationInfo) {
            m_applicationInfo = applicationInfo;
        }

        public void schedule(CommandExecutor executor) {
            executor.schedule(this);
        }
        
        @Override
        public void execute() {
            doApplicationUpdate(m_applicationInfo);
        }

    }

    public class LocationUpdater implements Scheduler.RepeatingCommand {

        private Queue<LocationInfo> m_locations;
        private int m_count = 0;
        private int m_updated = 0;

        public LocationUpdater(Collection<LocationInfo> locations) {
            m_locations = new LinkedList<LocationInfo>(locations);
            m_count = m_locations.size();
        }
        
        public void schedule(CommandExecutor executor) {
            executor.schedule(this);
        }

        @Override
        public boolean execute() {
            if(m_locations.isEmpty()) {
                return false;
            }else {
                LocationInfo location = m_locations.remove();
                updateLocation(location);
                m_updated++;
                m_view.setStatusMessage("Updated " + m_updated + " of " + m_count);
                return !m_locations.isEmpty();
            }
            
        }

    }

    private final DataManager m_dataManager = new DataManager();
    
    private final ApplicationFilter m_applicationFilter = new ApplicationFilter();
    private final StatusFilter m_statusFilter = new StatusFilter();
    private final TagFilter m_tagFilter = new TagFilter();
    private final LocationFilter m_locationViewFilter = m_tagFilter;
    private final LocationFilter m_applicationViewFilter = new AndFilter(m_applicationFilter, m_tagFilter);
    private LocationFilter m_selectedFilter = m_locationViewFilter;
    
    private final HandlerManager m_eventBus;

    private final HandlerManager m_handlerManager = new HandlerManager(this);

    private final LocationStatusServiceAsync m_remoteService;
    
    private final RemoteEventService m_remoteEventService;

    private boolean m_updated = false;

    private final ApplicationView m_view;

    private final CommandExecutor m_executor;

    private final AndFilter m_selectedVisibleFilter = new AndFilter(m_selectedFilter, m_statusFilter);

    /**
     * <p>Constructor for DefaultLocationManager.</p>
     *
     * @param eventBus a {@link com.google.gwt.event.shared.HandlerManager} object.
     * @param view 
     * @param mapPanel a {@link org.opennms.features.poller.remote.gwt.client.MapPanel} object.
     * @param panel a {@link com.google.gwt.user.client.ui.SplitLayoutPanel} object.
     * @param locationPanel a {@link org.opennms.features.poller.remote.gwt.client.LocationPanel} object.
     */
    public DefaultLocationManager(final HandlerManager eventBus, ApplicationView view, LocationStatusServiceAsync remoteService, RemoteEventService remoteEventService, CommandExecutor commandExecutor) {
        m_eventBus = eventBus;
        m_view = view;
        m_remoteService = remoteService;
        m_remoteEventService = remoteEventService;
        m_executor = commandExecutor;

        // Register for all relevant events thrown by the UI components
        m_eventBus.addHandler(LocationPanelSelectEvent.TYPE, this);
        m_eventBus.addHandler(MapPanelBoundsChangedEvent.TYPE, this);
        m_eventBus.addHandler(FiltersChangedEvent.TYPE, this);
        m_eventBus.addHandler(TagSelectedEvent.TYPE, this);
        m_eventBus.addHandler(TagClearedEvent.TYPE, this);
        m_eventBus.addHandler(StatusSelectionChangedEvent.TYPE, this);
        m_eventBus.addHandler(ApplicationDeselectedEvent.TYPE, this);
        m_eventBus.addHandler(ApplicationSelectedEvent.TYPE, this);
        m_eventBus.addHandler(GWTMarkerClickedEvent.TYPE, this);
        m_eventBus.addHandler(GWTMarkerInfoWindowRefreshEvent.TYPE, this);
        
        initialize(view.getSelectedStatuses());
    }

    @Override
    public void initialize(Set<Status> statuses) {
        m_statusFilter.setStatuses(statuses);
        
        initializeEventService();
        
        startStatusEvents();
    }

    private void initializeEventService() {
        LocationListener locationListener = new DefaultLocationListener(this);
        
        final RemoteEventService eventService = getRemoteEventService();
        eventService.addListener(MapRemoteEventHandler.LOCATION_EVENT_DOMAIN, locationListener);
        eventService.addListener(null, locationListener);
    }

    private void startStatusEvents() {
        getRemoteService().start(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                // Log.debug("unable to start location even service backend", throwable);
                Window.alert("unable to start location event service backend: " + throwable.getMessage());
                throw new InitializationException("remote service start failed", throwable);
            }
        
            @Override
            public void onSuccess(Void voidArg) {
              
            }
        });
    }

    /**
     * <p>initializationComplete</p>
     */
    protected void initializationComplete() {
        m_handlerManager.fireEvent(new LocationManagerInitializationCompleteEvent());
    }

    /**
     * <p>getRemoteService</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.LocationStatusServiceAsync} object.
     */
    protected LocationStatusServiceAsync getRemoteService() {
        return m_remoteService;
    }

    /** {@inheritDoc} */
    @Override
    public void addLocationManagerInitializationCompleteEventHandler(LocationManagerInitializationCompleteEventHander handler) {
        m_handlerManager.addHandler(LocationManagerInitializationCompleteEvent.TYPE, handler);
    };

    /**
     * <p>displayDialog</p>
     *
     * @param title a {@link java.lang.String} object.
     * @param contents a {@link java.lang.String} object.
     */
    protected void displayDialog(final String title, final String contents) {
        final DialogBox db = new DialogBox();
        db.setAutoHideEnabled(true);
        db.setModal(true);
        db.setText(title);
        db.setWidget(new Label(contents, true));
        db.show();
    }

    /**
     * <p>getAllLocationNames</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getAllLocationNames() {
        return m_dataManager.getAllLocationNames();
    }

    /** {@inheritDoc} */
    public void reportError(final String errorMessage, final Throwable throwable) {
        // FIXME: implement error reporting in UI
    }

    /**
     * <p>fitMapToLocations</p>
     */
    public void fitMapToLocations() {
        m_view.fitMapToLocations(m_dataManager.getLocationBounds());
    }

    /**
     * TODO: Figure out if this public function is necessary or if we can get
     * by just responding to incoming events.
     *
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<LocationInfo> getLocationsForLocationPanel() {
        
        // Use an ArrayList so that it has good random-access efficiency
        // since the pageable lists use get() to fetch based on index.
        final List<LocationInfo> visibleLocations = m_dataManager.getMatchingLocations(m_selectedVisibleFilter);
        
        GWTBounds mapBounds = m_view.getMapBounds();
        final ArrayList<LocationInfo> inBounds = new ArrayList<LocationInfo>();
        for (final LocationInfo location : visibleLocations) {
            final GWTMarkerState markerState = location.getMarkerState();
            
            if ( markerState.isWithinBounds(mapBounds)) {
                inBounds.add(location);
            }
        }

        // TODO: this should use the current filter set eventually, for now
        // sort by priority, then name
        // for now, LocationInfo is Comparable and has a natural sort ordering
        // based on status, priority, and name
        Collections.sort(inBounds, new Comparator<LocationInfo>() {
            @Override
            public int compare(LocationInfo o1, LocationInfo o2) {
                return o1.compareTo(o2);
            }
        });

        return inBounds;
    }

    private void updateAllMarkerStates() {
        for (final LocationInfo location : m_dataManager.getLocations()) {
            placeMarker(location);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Handler triggered when a user clicks on a specific location record.
     */
    @Override
    public void onLocationSelected(final LocationPanelSelectEvent event) {
        showLocationDetails(event.getLocationName());
    }

    private void showLocationDetails(final String locationName) {
        // TODO: this needs a callback to get the location details, and fill
        // in the content
        final LocationInfo loc = m_dataManager.getLocation(locationName);
        m_remoteService.getLocationDetails(locationName, new AsyncCallback<LocationDetails>() {
            @Override
            public void onFailure(final Throwable t) {
                String htmlTitle = "Error Getting Location Details";
                String htmlContent = "<p>An error occurred getting the location details.</p>" + "<pre>"
                           + URL.encode(t.getMessage()) + "</pre>";
                m_view.showLocationDetails(locationName, htmlTitle, htmlContent);
            }

            @Override
            public void onSuccess(final LocationDetails locationDetails) {
                m_view.showLocationDetails(
                    locationName,
                    locationName + " (" + loc.getArea() + ")",
                    getLocationInfoDetails(loc, locationDetails)
                );
            }

        });
    }

    /**
     * {@inheritDoc}
     *
     * Refresh the list of locations whenever the map panel boundaries change.
     */
    @Override
    public void onBoundsChanged(final MapPanelBoundsChangedEvent e) {
        // make sure each location's marker is up-to-date
        updateAllMarkerStates();

        m_view.setSelectedTag(m_tagFilter.getSelectedTag(), m_dataManager.getAllTags());

        m_view.updateLocationList(getLocationsForLocationPanel());

    }

    /**
     * {@inheritDoc}
     *
     * Invoked by the {@link LocationUpdatedRemoteEvent} and
     * {@link LocationsUpdatedRemoteEvent} events.
     */
    @Override
    public void updateLocation(final LocationInfo newLocation) {
        if (newLocation != null) {
            LocationInfo oldLocation = m_dataManager.getLocation(newLocation.getName());

            // Update the location information in the model
            m_dataManager.updateLocation(newLocation);
            
            m_view.updateApplicationNames(m_dataManager.getAllApplicationNames());
            placeMarker(newLocation);
            
            if (m_updated) {
                // Update the icon/caption in the LHN
                if(oldLocation == null || (m_selectedVisibleFilter.matches(newLocation) != m_selectedVisibleFilter.matches(oldLocation)) || (oldLocation.getStatus() != newLocation.getStatus())) {
                    m_view.updateLocationList(getLocationsForLocationPanel());
                }
                
                m_eventBus.fireEvent(new LocationsUpdatedEvent());
            }
            
        }
    }

    private void placeMarker(final LocationInfo info) {
        final GWTMarkerState markerState = info.getMarkerState();
        
        markerState.setVisible(m_statusFilter.matches(info));
        
        markerState.setSelected(m_selectedFilter.matches(info));
        markerState.place(m_view);
    }

    /**
     * {@inheritDoc}
     *
     * Invoked by the {@link org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent} and
     * {@link org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent} events.
     */
    @Override
    public void updateApplication(final ApplicationInfo applicationInfo) {
        ApplicationUpdater appUpdater = new ApplicationUpdater(applicationInfo);
        appUpdater.schedule(m_executor);
    }

    private void doApplicationUpdate(final ApplicationInfo applicationInfo) {
        if (applicationInfo == null)
            return;

        // Update the application information in the model
        m_dataManager.updateApplication(applicationInfo);
        m_view.updateApplicationNames(m_dataManager.getAllApplicationNames());

        /*
         * Update the icon/caption in the LHN Use an ArrayList so that it has
         * good random-access efficiency since the pageable lists use get() to
         * fetch based on index. Note, that m_applications is a *HashSet* not
         * a *TreeSet* since TreeSets consider duplicates based on Comparable,
         * not equals, and thus duplicates them.
         */

        m_view.updateApplicationList(m_dataManager.getApplications());

        if (!m_updated) {
            return;
        }

        updateAllMarkerStates();

        m_eventBus.fireEvent(new LocationsUpdatedEvent());
    }

    /** {@inheritDoc} */
    @Override
    public void removeApplication(final String applicationName) {
        final ApplicationInfo info = m_dataManager.getApplicationInfo(applicationName);
        m_dataManager.removeApplication(applicationName);
        if (info != null) {
            m_applicationFilter.removeApplication(info);
            m_view.updateApplicationList(m_dataManager.getApplications());
        }

        m_eventBus.fireEvent(new LocationsUpdatedEvent());
    }

    /**
     * Invoked by the {@link UpdateCompleteRemoteEvent} event.
     */
    @Override
    public void updateComplete() {
        m_dataManager.updateComplete();
        if (!m_updated) {
            updateAllMarkerStates();
            fitMapToLocations();
            m_updated = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onFiltersChanged(Filters filters) {
        // TODO: Update state inside of this object to track the filter state
        // (if necessary)
        // TODO: Update markers on the map panel
        // TODO: Update the list of objects in the LHN
    }

    /** {@inheritDoc} */
    @Override
    public void onTagSelected(String tagName) {
        // Update state inside of this object to track the selected tag
        m_tagFilter.setSelectedTag(tagName);
        // make sure each location's marker is up-to-date
        updateAllMarkerStates();

        m_view.updateLocationList(getLocationsForLocationPanel());
    }

    /**
     * <p>onTagCleared</p>
     */
    @Override
    public void onTagCleared() {
        // Update state inside of this object to track the selected tag
        m_tagFilter.setSelectedTag(null);

        // make sure each location's marker is up-to-date
        updateAllMarkerStates();

        // TODO: Update markers on the map panel
        // Update the list of objects in the LHN
        m_view.updateLocationList(getLocationsForLocationPanel());
    }

    /**
     * Fetch a list of all application names.
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getAllApplicationNames() {
        //TODO: move this to ApplicationPresenter
        return m_dataManager.getAllApplicationNames();
    }

    /** {@inheritDoc} */
    public ApplicationInfo getApplicationInfo(final String name) {
        return m_dataManager.getApplicationInfo(name);
    }

    /** {@inheritDoc} */
    public LocationInfo getLocation(String locationName) {
        return m_dataManager.getLocation(locationName);
    }

    /** {@inheritDoc} */
    @Override
    public void onGWTMarkerClicked(GWTMarkerClickedEvent event) {
        GWTMarkerState markerState = event.getMarkerState();
        showLocationDetails(markerState.getName());
    }
    
    @Override
    public void onGWTMarkerInfoWindowRefresh(GWTMarkerInfoWindowRefreshEvent event) {
        refreshLocationInfoWindowDetails(event.getMarkerState().getName());
    }

    private void refreshLocationInfoWindowDetails(String name) {
        showLocationDetails(name);
    }

    /** {@inheritDoc} */
    @Override
    public void onStatusSelectionChanged(Status status, boolean selected) {
        if (selected) {
            m_statusFilter.addStatus(status);
        } else {
            m_statusFilter.removeStatus(status);
        }

        updateAllMarkerStates();

        m_view.updateLocationList(getLocationsForLocationPanel());
    }

    /** {@inheritDoc} */
    @Override
    public void onApplicationSelected(final ApplicationSelectedEvent event) {
        
        
        final String applicationName = event.getApplicationname();
        final ApplicationInfo app = m_dataManager.getApplicationInfo(applicationName);
        //App maybe null if the user types an invalid name
        if(app == null) {
            return;
        }

        // Add the application to the selected application list
        m_applicationFilter.addApplication(app);

        updateAllMarkerStates();

        m_view.updateSelectedApplications(m_applicationFilter.getApplications());
        
        m_remoteService.getApplicationDetails(applicationName, new AsyncCallback<ApplicationDetails>() {

            @Override
            public void onFailure(final Throwable t) {
                // TODO: Do something on failure.
            }

            @Override
            public void onSuccess(final ApplicationDetails applicationDetails) {
                m_eventBus.fireEvent(new ApplicationDetailsRetrievedEvent(applicationDetails));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onApplicationDeselected(ApplicationDeselectedEvent event) {
        // Remove the application from the selected application list
        m_applicationFilter.removeApplication(event.getAppInfo());

        updateAllMarkerStates();

        m_view.updateSelectedApplications(m_applicationFilter.getApplications());
    }

    /**
     * <p>locationClicked</p>
     */
    @Override
    public void locationClicked() {
        m_selectedFilter = m_locationViewFilter;
        updateAllMarkerStates();
    }

    /**
     * <p>applicationClicked</p>
     */
    @Override
    public void applicationClicked() {
        m_selectedFilter = m_applicationViewFilter;
        updateAllMarkerStates();
    }

    /**
     * <p>getLocationInfoDetails</p>
     *
     * @param locationInfo a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     * @param locationDetails a {@link org.opennms.features.poller.remote.gwt.client.location.LocationDetails} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getLocationInfoDetails(final LocationInfo locationInfo, final LocationDetails locationDetails) {
        final LocationMonitorState state = locationDetails.getLocationMonitorState();

        int pollersStarted      = state.getMonitorsStarted();
        int pollersStopped      = state.getMonitorsStopped();
        int pollersDisconnected = state.getMonitorsDisconnected();
        int services            = state.getServices().size();
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
            sb.append(servicesWithOutages).append(" outage").append(servicesWithOutages == 1? "" : "s");
            sb.append(" (of ").append(services).append(" service").append(services == 1? "" : "s").append(")").append("<br>\n");
            sb.append(monitorsWithOutages).append(" poller").append(monitorsWithOutages == 1? "" : "s").append(" reporting errors").append("\n");
            sb.append("</dd>\n");
        }

        sb.append("</div>");
        return sb.toString();
    }

    @Override
    public void updateLocations(Collection<LocationInfo> locations) {
        LocationUpdater locUpdater = new LocationUpdater(locations);
        locUpdater.schedule(m_executor);
    }

    private RemoteEventService getRemoteEventService() {
        return m_remoteEventService;
    }
        
}
