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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.FilterPanel.StatusSelectionChangedEvent;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class DefaultApplicationView implements ApplicationView, ResizeHandler {
    
    interface Binder extends UiBinder<DockLayoutPanel, DefaultApplicationView> {}
    
    private static final Binder BINDER = GWT.create(Binder.class);
    
    interface LinkStyles extends CssResource {
        String activeLink();
    }
    
    @UiField
    protected LocationPanel locationPanel;
    

    @UiField
    protected DockLayoutPanel mainPanel;
    @UiField
    protected SplitLayoutPanel splitPanel;
    @UiField
    protected Hyperlink locationLink;
    @UiField
    protected Hyperlink applicationLink;
    @UiField
    protected Label updateTimestamp;
    @UiField
    protected LinkStyles linkStyles;
    @UiField
    protected HorizontalPanel statusesPanel;
    @UiField
    protected CheckBox statusDown;
    @UiField
    protected CheckBox statusDisconnected;
    @UiField
    protected CheckBox statusMarginal;
    @UiField
    protected CheckBox statusUp;
    @UiField
    protected CheckBox statusStopped;
    @UiField
    protected CheckBox statusUnknown;
    
    private final MapPanel m_mapPanel;
    
    private final HandlerManager m_eventBus;


    private Application m_presenter;


    static final DateTimeFormat UPDATE_TIMESTAMP_FORMAT = DateTimeFormat.getMediumDateTimeFormat();
    
    
    public DefaultApplicationView(Application presenter, HandlerManager eventBus, MapPanel mapPanel) {
        m_presenter = presenter;
        m_eventBus = eventBus;
        m_mapPanel = mapPanel;
        BINDER.createAndBindUi(this);
        
        locationPanel.setEventBus(eventBus);
        setupWindow();
        
    }
    
    @UiHandler("statusDown")
    public void onDownClicked(final ClickEvent event) {
        getEventBus().fireEvent(new StatusSelectionChangedEvent(Status.DOWN, getStatusDown().getValue()));
    }

    @UiHandler("statusDisconnected")
    public void onDisconnectedClicked(final ClickEvent event) {
        getEventBus().fireEvent(new StatusSelectionChangedEvent(Status.DISCONNECTED, getStatusDisconnected().getValue()));
    }

    @UiHandler("statusMarginal")
    public void onMarginalClicked(final ClickEvent event) {
        getEventBus().fireEvent(new StatusSelectionChangedEvent(Status.MARGINAL, getStatusMarginal().getValue()));
    }

    @UiHandler("statusUp")
    public void onUpClicked(final ClickEvent event) {
        getEventBus().fireEvent(new StatusSelectionChangedEvent(Status.UP, getStatusUp().getValue()));
    }

    @UiHandler("statusStopped")
    public void onStoppedClicked(final ClickEvent event) {
        getEventBus().fireEvent(new StatusSelectionChangedEvent(Status.STOPPED, getStatusStopped().getValue()));
    }

    @UiHandler("statusUnknown")
    public void onUnknownClicked(final ClickEvent event) {
        getEventBus().fireEvent(new StatusSelectionChangedEvent(Status.UNKNOWN, getStatusUnknown().getValue()));
    }

    private HandlerManager getEventBus() {
        return m_eventBus;
    }
    
    private DockLayoutPanel getMainPanel() {
        return mainPanel;
    }

    private SplitLayoutPanel getSplitPanel() {
        return splitPanel;
    }

    private HorizontalPanel getStatusesPanel() {
        return statusesPanel;
    }

    private CheckBox getStatusDown() {
        return statusDown;
    }

    private CheckBox getStatusDisconnected() {
        return statusDisconnected;
    }

    private CheckBox getStatusMarginal() {
        return statusMarginal;
    }

    private CheckBox getStatusUp() {
        return statusUp;
    }

    private CheckBox getStatusStopped() {
        return statusStopped;
    }

    private CheckBox getStatusUnknown() {
        return statusUnknown;
    }
    
    private LocationPanel getLocationPanel() {
        return locationPanel;
    }
    
    private Hyperlink getLocationLink() {
        return locationLink;
    }

    private Hyperlink getApplicationLink() {
        return applicationLink;
    }

    private LinkStyles getLinkStyles() {
        return linkStyles;
    }

    private Label getUpdateTimestamp() {
        return updateTimestamp;
    }
    
    private Application getPresenter() {
        return m_presenter;
    }

    /**
     * <p>onApplicationClick</p>
     *
     * @param event a {@link com.google.gwt.event.dom.client.ClickEvent} object.
     */
    @UiHandler("applicationLink")
    public void onApplicationClick(ClickEvent event) {
        if (getApplicationLink().getStyleName().contains(getLinkStyles().activeLink())) {
            // This link is already selected, do nothing
        } else {
            getPresenter().onApplicationViewSelected();
            getApplicationLink().addStyleName(getLinkStyles().activeLink());
            getLocationLink().removeStyleName(getLinkStyles().activeLink());
            getLocationPanel().showApplicationList();
            getLocationPanel().showApplicationFilters(true);
            getLocationPanel().resizeDockPanel();
        }
    }

    /**
     * <p>onLocationClick</p>
     *
     * @param event a {@link com.google.gwt.event.dom.client.ClickEvent} object.
     */
    @UiHandler("locationLink")
    public void onLocationClick(ClickEvent event) {
        if (getLocationLink().getStyleName().contains(getLinkStyles().activeLink())) {
            // This link is already selected, do nothing
        } else {
            getPresenter().onLocationViewSelected();
            getLocationLink().addStyleName(getLinkStyles().activeLink());
            getApplicationLink().removeStyleName(getLinkStyles().activeLink());
            getLocationPanel().showLocationList();
            getLocationPanel().showApplicationFilters(false);
            getLocationPanel().resizeDockPanel();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#updateTimestamp()
     */
    @Override
    public void updateTimestamp() {
        getUpdateTimestamp().setText("Last update: " + UPDATE_TIMESTAMP_FORMAT.format(new Date()));
    }

    private Integer getAppHeight() {
    	final com.google.gwt.user.client.Element e = getMainPanel().getElement();
    	int extraHeight = e.getAbsoluteTop();
    	return Window.getClientHeight() - extraHeight;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#getSelectedStatuses()
     */
    @Override
    public Set<Status> getSelectedStatuses() {
        
        Set<Status> statuses = new HashSet<Status>();
        for (final Widget w : getStatusesPanel()) {
            if (w instanceof CheckBox) {
                final CheckBox cb = (CheckBox)w;
                if(cb.getValue()) {
                    statuses.add(Status.valueOf(cb.getFormValue()));
                }
            }
        }
        return statuses;
    }

    private void setupWindow() {
        Window.setTitle("OpenNMS - Remote Monitor");
        Window.enableScrolling(false);
        Window.setMargin("0px");
        Window.addResizeHandler(this);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#initialize()
     */
    @Override
    public void initialize() {
        getSplitPanel().add(getMapPanel().getWidget());
        getSplitPanel().setWidgetMinSize(getLocationPanel(), 255);
        getMainPanel().setSize("100%", "100%");
        RootPanel.get("map").add(getMainPanel());
        
        updateTimestamp();
        onLocationClick(null);
        
        onResize(null);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#updateSelectedApplications(java.util.Set)
     */
    @Override
    public void updateSelectedApplications(Set<ApplicationInfo> applications) {
        getLocationPanel().updateSelectedApplications(applications);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#updateLocationList(java.util.ArrayList)
     */
    @Override
    public void updateLocationList( ArrayList<LocationInfo> locationsForLocationPanel) {
        getLocationPanel().updateLocationList(locationsForLocationPanel);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#setSelectedTag(java.lang.String, java.util.List)
     */
    @Override
    public void setSelectedTag(String selectedTag, List<String> allTags) {
        getLocationPanel().clearTagPanel();
        getLocationPanel().addAllTags(allTags);
        getLocationPanel().selectTag(selectedTag);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#updateApplicationList(java.util.ArrayList)
     */
    @Override
    public void updateApplicationList(ArrayList<ApplicationInfo> applications) {
        getLocationPanel().updateApplicationList(applications);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#updateApplicationNames(java.util.TreeSet)
     */
    @Override
    public void updateApplicationNames(TreeSet<String> allApplicationNames) {
        getLocationPanel().updateApplicationNames(allApplicationNames);
    }

    private MapPanel getMapPanel() {
        return m_mapPanel;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#fitMapToLocations(org.opennms.features.poller.remote.gwt.client.GWTBounds)
     */
    @Override
    public void fitMapToLocations(GWTBounds locationBounds) {
        if (getMapPanel() instanceof SmartMapFit) {
            ((SmartMapFit)getMapPanel()).fitToBounds();
        } else {
            //TODO: Zoom in to visible locations on startup
            
            getMapPanel().setBounds(locationBounds);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#getMapBounds()
     */
    @Override
    public GWTBounds getMapBounds() {
        return getMapPanel().getBounds();
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#showLocationDetails(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void showLocationDetails(final String locationName, String htmlTitle, String htmlContent) {
        getMapPanel().showLocationDetails(locationName, htmlTitle, htmlContent);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.poller.remote.gwt.client.ApplicationView#placeMarker(org.opennms.features.poller.remote.gwt.client.GWTMarkerState)
     */
    @Override
    public void placeMarker(final GWTMarkerState markerState) {
        getMapPanel().placeMarker(markerState);
    }

    @Override
    public void setStatusMessage(String statusMessage) {
        //getUpdateTimestamp().setText(statusMessage);
    }

    @Override
    public void onResize(ResizeEvent event) {
        getMainPanel().setHeight(getAppHeight().toString());
    }
}
