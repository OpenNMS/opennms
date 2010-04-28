package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint
{
	interface Binder extends UiBinder<DockLayoutPanel, Application> { }
	private static final Binder BINDER = GWT.create(Binder.class);

	private LocationManager m_locationManager;
	private final HandlerManager m_eventBus = new HandlerManager(null);

    interface LinkStyles extends CssResource {
        String activeLink();
    }
    
	@UiField protected LocationPanel locationPanel;
	@UiField protected DockLayoutPanel rootPanel;
    @UiField protected SplitLayoutPanel splitPanel;
    @UiField protected Hyperlink locationLink;
    @UiField protected Hyperlink applicationLink;
    @UiField protected LinkStyles linkStyles;

  /**
   * This is the entry point method.
   */
	public void onModuleLoad()
	{
		// Log.setUncaughtExceptionHandler();

		BINDER.createAndBindUi(this);

		Window.setTitle("OpenNMS - Remote Monitor");
		Window.enableScrolling(false);
		Window.setMargin("0px");

		m_locationManager = new DefaultLocationManager(m_eventBus, splitPanel, locationPanel, createMapPanel());
		
		m_locationManager.addLocationManagerInitializationCompleteEventHandler(new LocationManagerInitializationCompleteEventHander() {
            
            public void onInitializationComplete(LocationManagerInitializationCompleteEvent event) {
                splitPanel.setWidgetMinSize(locationPanel, 220);
                // Show the location list by default 
                rootPanel.setSize("100%", "100%");
                RootPanel.get("remotePollerMap").add(rootPanel);
                onLocationClick(null);
            }
        });
		locationPanel.setEventBus(m_eventBus);
		locationPanel.filterPanel.setLocationManager(m_locationManager);
		
		m_locationManager.initialize();
	}
	
	@UiHandler("locationLink")
	public void onLocationClick(ClickEvent event) {
	    if (locationLink.getStyleName().contains(linkStyles.activeLink())) {
	        // This link is already selected, do nothing
	    } else {
	    	m_locationManager.locationClicked();
	        locationLink.addStyleName(linkStyles.activeLink());
            applicationLink.removeStyleName(linkStyles.activeLink());
	        locationPanel.showLocationList();
	        locationPanel.showApplicationFilters(false);
	        locationPanel.resizeDockPanel();
	    }
	}

    @UiHandler("applicationLink")
    public void onApplicationClick(ClickEvent event) {
        if (applicationLink.getStyleName().contains(linkStyles.activeLink())) {
            // This link is already selected, do nothing
        } else {
        	m_locationManager.applicationClicked();
            applicationLink.addStyleName(linkStyles.activeLink());
            locationLink.removeStyleName(linkStyles.activeLink());
            locationPanel.showApplicationList();
            locationPanel.showApplicationFilters(true);
            locationPanel.resizeDockPanel();
        }
    }

    private MapPanel createMapPanel() {
    MapPanel mapPanel;
    if (getMapImplementationType().equals("Mapquest")) {
        mapPanel = new MapQuestMapPanel(m_eventBus);
    } else if (getMapImplementationType().equals("GoogleMaps")) {
        mapPanel = new GoogleMapsPanel(m_eventBus);
    } else if (getMapImplementationType().equals("OpenLayers")) {
    	mapPanel = new OpenLayersMapPanel(m_eventBus);
    } else {
    	Window.alert("unknown map implementation: " + getMapImplementationType());
    	throw new RuntimeException("unknown map implementation: " + getMapImplementationType());
    }
    return mapPanel;
}

	public native String getMapImplementationType() /*-{
		return $wnd.mapImplementation;
	}-*/;
}
