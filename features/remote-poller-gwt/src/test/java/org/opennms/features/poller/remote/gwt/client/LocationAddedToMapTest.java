package org.opennms.features.poller.remote.gwt.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationsUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.UpdateCompleteRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.utils.BoundsBuilder;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.IncrementalCommand;

import de.novanic.eventservice.client.event.RemoteEventService;


public class LocationAddedToMapTest {
    
    public class TestApplicationView implements ApplicationView {
        
        GWTBounds bounds;
        private Application m_application;
        private HandlerManager m_eventBus;
        private int m_marker = 0;
        
        public TestApplicationView(Application application, HandlerManager eventBus) {
            m_application = application;
            m_eventBus = eventBus;
        }

        public void updateTimestamp() {
            // TODO Auto-generated method stub

        }

        public Set<Status> getSelectedStatuses() {
            Set<Status> hashSet = new HashSet<Status>();
            Collections.addAll(hashSet, Status.DOWN, Status.MARGINAL, Status.STOPPED, Status.DISCONNECTED, Status.UP);
            return hashSet;
        }

        public void initialize() {
            m_application.onLocationViewSelected();
        }

        public void updateSelectedApplications(Set<ApplicationInfo> applications) {
            // TODO Auto-generated method stub

        }

        public void updateLocationList(
                ArrayList<LocationInfo> locationsForLocationPanel) {
            // TODO Auto-generated method stub

        }

        public void setSelectedTag(String selectedTag, List<String> allTags) {
            // TODO Auto-generated method stub

        }

        public void updateApplicationList(
                ArrayList<ApplicationInfo> applications) {
            // TODO Auto-generated method stub

        }

        public void updateApplicationNames(TreeSet<String> allApplicationNames) {
            // TODO Auto-generated method stub

        }

        public void fitMapToLocations(GWTBounds locationBounds) {
            bounds = locationBounds;
        }

        public GWTBounds getMapBounds() {
            return bounds;
        }

        public void showLocationDetails(String locationName, String htmlTitle, String htmlContent) {
            // TODO Auto-generated method stub

        }

        public void placeMarker(GWTMarkerState markerState) {
            m_marker++;
            //try { Thread.sleep(1); } catch (Exception e) {}
        }
        
        public int getMarkerCount() {
            return m_marker;
        }

        public void resetMarkerCount() {
            m_marker = 0;
        }
    }
    
    private class TestCommandExecutor implements CommandExecutor{
        
        private List<Object> m_commands = new LinkedList<Object>();
        public void schedule(IncrementalCommand command) {
            m_commands.add(command);
        }
        
        public void run() {
            while(true) {
                Iterator<Object> iterator = m_commands.iterator();
                if(!iterator.hasNext()) {
                    return;
                }
                
                while(iterator.hasNext()) {
                    Object o = iterator.next();
                    if(o instanceof Command) {
                        ((Command) o).execute();
                        iterator.remove();
                    }else {
                        IncrementalCommand command = (IncrementalCommand) o;
                        if(!command.execute()) {
                            iterator.remove();
                        }
                    }
                    
                }
            }
        }

        public void schedule(Command command) {
            m_commands.add(command);
        }
        
    }
    
    RemoteEventService m_remoteEventService;
    LocationStatusServiceAsync m_locationStatusService;
    private TestApplicationView m_testApplicationView;
    private TestServer m_testServer;
    private Random m_random;
    private TestCommandExecutor m_testExecutor = new TestCommandExecutor();
    
    @Before
    public void setUp() {
        m_testServer = new TestServer();
        m_remoteEventService = m_testServer;
        m_locationStatusService = m_testServer;
        m_random = new Random(System.currentTimeMillis());
    }

    @Test
    public void testAddLocation() {
        int numLocations = 3000;
        int numApps = 12;
        
        HandlerManager eventBus = new HandlerManager(null);
        Application application = new Application(eventBus);
        m_testApplicationView = createMockApplicationView(eventBus, application);
        application.initialize(m_testApplicationView, m_locationStatusService, m_remoteEventService, m_testExecutor);
        
        Set<LocationInfo> locations = new HashSet<LocationInfo>();
        GWTBounds bounds = createLocations(numLocations, locations);
        
        for( LocationInfo locationInfo : locations) {
            m_testServer.sendUserSpecificEvent(new LocationUpdatedRemoteEvent(locationInfo));
        }
        
        //create apps and update by sending event
        Set<ApplicationInfo> apps = createApps(numApps, locations);
        
        for(ApplicationInfo app : apps) {
            m_testServer.sendUserSpecificEvent(new ApplicationUpdatedRemoteEvent(app));
        }
        
        m_testServer.sendUserSpecificEvent(new UpdateCompleteRemoteEvent());
        
        m_testExecutor.run();
        
        assertNotNull(m_testApplicationView.getMapBounds());
        
        assertEquals(bounds, m_testApplicationView.getMapBounds());
        assertEquals(numLocations, m_testApplicationView.getMarkerCount());
        m_testApplicationView.resetMarkerCount();
        
        m_testServer.sendDomainEvent(new LocationsUpdatedRemoteEvent(locations));
        
        for(ApplicationInfo app : apps) {
            m_testServer.sendDomainEvent(new ApplicationUpdatedRemoteEvent(app));
        }
        
        m_testExecutor.run();
        
        assertEquals(0, m_testApplicationView.getMarkerCount());
    }
    

    private Set<ApplicationInfo> createApps(int numApps, Set<LocationInfo> locations) {
        Set<String> locNames = new HashSet<String>();
        for(LocationInfo location : locations) {
            locNames.add(location.getName());
        }
        
        Set<ApplicationInfo> apps = new HashSet<ApplicationInfo>();
        for(int i = 1; i <= numApps; i++) {
            
            apps.add(new ApplicationInfo(i, "app" + i, Collections.<GWTMonitoredService>emptySet(), locNames, new StatusDetails(Status.UP, "All things good here")));
        }
        return apps;
    }

    private GWTBounds createLocations(int num, Set<LocationInfo> locations) {
        BoundsBuilder boundsBldr = new BoundsBuilder();
        
        for(int i = 1; i <= num; i++) {
            double lat = m_random.nextDouble() * 22 + 27;
            double lng = m_random.nextDouble() * -57 - 67;
            boundsBldr.extend(lat, lng);
            LocationInfo location1 = new LocationInfo("location" + i, "area" + i, i + " Opennms Way", lat + "," + lng, 100L, null, new StatusDetails(Status.UP, "reason is that its up"), null);
            locations.add(location1);
        }
        
        
        return boundsBldr.getBounds();
    }

    private TestApplicationView createMockApplicationView( HandlerManager eventBus, Application application) {
        return new TestApplicationView(application, eventBus);
    }

}
