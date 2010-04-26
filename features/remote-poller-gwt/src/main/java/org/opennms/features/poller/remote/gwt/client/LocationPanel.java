package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

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
	@UiField FilterPanel filterPanel;
	@UiField TagPanel tagPanel;
	
	public LocationPanel() {
		super();
		initWidget(BINDER.createAndBindUi(this));
		locationList.addLocationPanelSelectEventHandler(this);
		tagPanel.setWidth(String.valueOf(this.getOffsetWidth()));
	}

    public void setEventBus(final HandlerManager eventBus) {
	    // Remove any existing handler registrations
	    for (HandlerRegistration registration : eventRegistrations) {
	        registration.removeHandler();
	    }
	    m_eventBus = eventBus;
	    
	    filterPanel.setEventBus(eventBus);
	    tagPanel.setEventBus(eventBus);
	    applicationList.setEventBus(eventBus);
	    // eventRegistrations.add(m_eventBus.addHandler(MapPanelBoundsChangedEvent.TYPE, this));
	    // eventRegistrations.add(m_eventBus.addHandler(LocationsUpdatedEvent.TYPE, this));
	}

    public void onLocationSelected(final LocationPanelSelectEvent event) {
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

    public void updateApplicationNames(final Set<String> allApplicationNames) {
        filterPanel.updateApplicationNames(allApplicationNames);
    }

    public void updateApplicationList(final ArrayList<ApplicationInfo> appList) {
        applicationList.updateList(appList);
    }

    public void updateLocationList(final ArrayList<LocationInfo> visibleLocations) {
        locationList.updateList(visibleLocations);
    }

    public void clearTagPanel() {
        tagPanel.clear();
    }

    public boolean addAllTags(final Collection<String> tags) {
        return tagPanel.addAll(tags);
    }
}
