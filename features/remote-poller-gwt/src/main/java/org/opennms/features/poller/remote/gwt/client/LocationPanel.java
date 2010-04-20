package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LocationPanel extends Composite implements LocationPanelSelectEventHandler {

	interface Binder extends UiBinder<Widget, LocationPanel> { }

	private static final Binder BINDER = GWT.create(Binder.class);
	private transient HandlerManager m_eventBus;
	private transient List<HandlerRegistration> eventRegistrations = new ArrayList<HandlerRegistration>();
	
	@UiField PageableLocationList locationList;
	@UiField PageableApplicationList applicationList;
	
	public LocationPanel() {
		super();
		initWidget(BINDER.createAndBindUi(this));
		locationList.addLocationPanelSelectEventHandler(this);
		setVisible(applicationList.getElement(), false);
	}

	public void update(final LocationManager locationManager) {
		if (locationManager == null) {
			throw new IllegalStateException("No LocationManager available inside LocationPanel");
		}
		
		List<Location> visibleLocations = locationManager.getVisibleLocations();
		
		locationList.updateList(visibleLocations);
		applicationList.updateList(getApplicationInfoTestData());
			
	}

	private List<ApplicationInfo> getApplicationInfoTestData() {
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

    private Set<GWTMonitoredService> getGWTMonitoredServiceTestData() {
        Set<GWTMonitoredService> services = new HashSet<GWTMonitoredService>();
        GWTMonitoredService service = new GWTMonitoredService();
        service.setServiceName("HTTP");
        services.add(service);
        return services;
    }

    private Set<String> getLocationSetTestData() {
        Set<String> locations = new HashSet<String>();
        locations.add("19");
        return locations;
    }

    public void setEventBus(final HandlerManager eventBus) {
	    // Remove any existing handler registrations
	    for (HandlerRegistration registration : eventRegistrations) {
	        registration.removeHandler();
	    }
	    m_eventBus = eventBus;
	    // eventRegistrations.add(m_eventBus.addHandler(MapPanelBoundsChangedEvent.TYPE, this));
	    // eventRegistrations.add(m_eventBus.addHandler(LocationsUpdatedEvent.TYPE, this));
	}

    public void onLocationSelected(LocationPanelSelectEvent event) {
        m_eventBus.fireEvent(event);
      
    }
    /**
     * Switches view to Pageable Location List
     */
    public void showLocationList() {
        setVisible(locationList.getElement(), true);
        setVisible(applicationList.getElement(), false);
    }
    
    /**
     * Switches view to the Pageable Application List
     */
    public void showApplicationList() {
        setVisible(locationList.getElement(), false);
        setVisible(applicationList.getElement(), true);
    }
}
