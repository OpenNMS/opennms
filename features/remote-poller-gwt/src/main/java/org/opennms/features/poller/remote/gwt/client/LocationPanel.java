package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.TagPanel.TagResizeEvent;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagResizeEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>LocationPanel class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationPanel extends Composite implements LocationPanelSelectEventHandler, TagResizeEventHandler, RequiresResize {
    
	interface Binder extends UiBinder<Widget, LocationPanel> { }

	private static final Binder BINDER = GWT.create(Binder.class);
	private transient HandlerManager m_eventBus;
	private transient List<HandlerRegistration> eventRegistrations = new ArrayList<HandlerRegistration>();
	
	@UiField PageableLocationList locationList;
	@UiField PageableApplicationList applicationList;
	@UiField FilterPanel filterPanel;
	@UiField TagPanel tagPanel;
	@UiField HTMLPanel filterOptionsPanel;
	@UiField FlowPanel listsPanel;
	
	/**
	 * <p>Constructor for LocationPanel.</p>
	 */
	public LocationPanel() {
		super();
		initWidget(BINDER.createAndBindUi(this));
		locationList.addLocationPanelSelectEventHandler(this);

		// Blank out the selected applications list
		this.updateSelectedApplications(new TreeSet<ApplicationInfo>());
		
	}

    /**
     * <p>setEventBus</p>
     *
     * @param eventBus a {@link com.google.gwt.event.shared.HandlerManager} object.
     */
    public void setEventBus(final HandlerManager eventBus) {
	    // Remove any existing handler registrations
	    for (HandlerRegistration registration : eventRegistrations) {
	        registration.removeHandler();
	    }
	    m_eventBus = eventBus;
	    m_eventBus.addHandler(TagResizeEvent.TYPE, this);
	    
	    filterPanel.setEventBus(eventBus);
	    tagPanel.setEventBus(eventBus);
	    applicationList.setEventBus(eventBus);
	    // eventRegistrations.add(m_eventBus.addHandler(MapPanelBoundsChangedEvent.TYPE, this));
	    // eventRegistrations.add(m_eventBus.addHandler(LocationsUpdatedEvent.TYPE, this));
	}

    /** {@inheritDoc} */
    public void onLocationSelected(final LocationPanelSelectEvent event) {
        m_eventBus.fireEvent(event);
      
    }
    /**
     * Switches view to Pageable Location List
     */
    public void showLocationList() {
        setVisible(locationList.getElement(), true);
        setVisible(applicationList.getElement(), false);
        locationList.refreshLocationListResize();
    }
    
    /**
     * Switches view to the Pageable Application List
     */
    public void showApplicationList() {
        setVisible(locationList.getElement(), false);
        setVisible(applicationList.getElement(), true);
        applicationList.refreshApplicationListResize();
    }

    /**
     * <p>updateSelectedApplications</p>
     *
     * @param selectedApplications a {@link java.util.Set} object.
     */
    public void updateSelectedApplications(final Set<ApplicationInfo> selectedApplications) {
        filterPanel.updateSelectedApplications(selectedApplications);
        applicationList.updateSelectedApplications(selectedApplications);
        //Trigger the resize of the panel
        resizeDockPanel();
    }
    
    /**
     * <p>updateApplicationNames</p>
     *
     * @param allApplicationNames a {@link java.util.Set} object.
     */
    public void updateApplicationNames(final Set<String> allApplicationNames) {
        filterPanel.updateApplicationNames(allApplicationNames);
    }

    /**
     * <p>updateApplicationList</p>
     *
     * @param appList a {@link java.util.ArrayList} object.
     */
    public void updateApplicationList(final ArrayList<ApplicationInfo> appList) {
        applicationList.updateList(appList);
    }

    /**
     * <p>updateLocationList</p>
     *
     * @param visibleLocations a {@link java.util.ArrayList} object.
     */
    public void updateLocationList(final ArrayList<LocationInfo> visibleLocations) {
        Collections.sort(visibleLocations, new Comparator<LocationInfo>() {
            public int compare(LocationInfo o1, LocationInfo o2) {
                return -1 * o1.compareTo(o2);
            }
            
        });
        locationList.updateList(visibleLocations);
    }

    /**
     * <p>selectTag</p>
     *
     * @param tag a {@link java.lang.String} object.
     */
    public void selectTag(String tag) {
        tagPanel.selectTag(tag);
    }

    /**
     * <p>clearTagPanel</p>
     */
    public void clearTagPanel() {
        tagPanel.clear();
    }

    /**
     * <p>addAllTags</p>
     *
     * @param tags a {@link java.util.Collection} object.
     * @return a boolean.
     */
    public boolean addAllTags(final Collection<String> tags) {
        return tagPanel.addAll(tags);
    }

    /**
     * <p>showApplicationFilters</p>
     *
     * @param isApplicationView a boolean.
     */
    public void showApplicationFilters(boolean isApplicationView) {
        filterPanel.showApplicationFilters(isApplicationView);
    }

    /**
     * <p>resizeDockPanel</p>
     */
    public void resizeDockPanel() {

        int verticalSpacer = 3;
        int newSize = tagPanel.getOffsetHeight() + filterPanel.getOffsetHeight() + verticalSpacer;
        
        Element element = listsPanel.getElement();
        element.setAttribute("style", "position: absolute; top: " + newSize + "px; left: 0px; right: 0px; bottom: 0px;");
    }

    /**
     * <p>onTagPanelResize</p>
     */
    public void onTagPanelResize() {
        resizeDockPanel();
    }

    /**
     * <p>onResize</p>
     */
    public void onResize() {
        if(applicationList.isVisible()) {
            applicationList.refreshApplicationListResize();
        }else if(locationList.isVisible()) {
            locationList.refreshLocationListResize();
        }
    }

}
