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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.TagPanel.TagResizeEvent;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagResizeEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
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
public class LocationPanel extends Composite implements LocationPanelSelectEventHandler, TagResizeEventHandler, RequiresResize, ResizeHandler {
    
	interface Binder extends UiBinder<Widget, LocationPanel> { }

	private static final Binder BINDER = GWT.create(Binder.class);
	private transient HandlerManager m_eventBus;
	private transient List<HandlerRegistration> eventRegistrations = new ArrayList<HandlerRegistration>();
	
	@UiField FlowPanel locationPanel;
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
		
		Window.addResizeHandler(this);
		listsPanel.getElement().setId("listsPanel");
		locationList.getElement().setId("locationList");
		applicationList.getElement().setId("applicationList");
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
        @Override
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
        Collections.sort(appList, new Comparator<ApplicationInfo>() {

            @Override
            public int compare(ApplicationInfo o1, ApplicationInfo o2) {
                return -1 * o1.compareTo(o2);
            }
            
        });
        
        applicationList.updateList(appList);
    }

    /**
     * <p>updateLocationList</p>
     *
     * @param visibleLocations a {@link java.util.ArrayList} object.
     */
    public void updateLocationList(final ArrayList<LocationInfo> visibleLocations) {
        Collections.sort(visibleLocations, new Comparator<LocationInfo>() {
            @Override
            public int compare(LocationInfo o1, LocationInfo o2) {
                return -1 * o1.compareTo(o2);
            }
            
        });
        locationList.updateList(visibleLocations);
        resizeDockPanel();
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
        Element element = listsPanel.getElement();
        
        if(getUserAgent().contains("msie")) {
            int newHeight = 100;
            
            if(locationPanel.getOffsetHeight() > 1) {
                newHeight = locationPanel.getOffsetHeight() - (tagPanel.getOffsetHeight() + filterPanel.getOffsetHeight() + verticalSpacer);
            }
            
            element.getStyle().setHeight(newHeight, Unit.PX);
        }else {
            int newTop = tagPanel.getOffsetHeight() + filterPanel.getOffsetHeight() + verticalSpacer;
            element.getStyle().setTop(newTop, Unit.PX);
        }
        
    }

    /**
     * <p>onTagPanelResize</p>
     */
        @Override
    public void onTagPanelResize() {
        resizeDockPanel();
    }

    /**
     * <p>onResize</p>
     */
        @Override
    public void onResize() {
        if(applicationList.isVisible()) {
            applicationList.refreshApplicationListResize();
        }else if(locationList.isVisible()) {
            locationList.refreshLocationListResize();
        }
    }

        @Override
    public void onResize(ResizeEvent event) {
        resizeDockPanel();
    }
    
    public static native String getUserAgent() /*-{
        return navigator.userAgent.toLowerCase();
    }-*/;

}
