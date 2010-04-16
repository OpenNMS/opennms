package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
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

	@UiField protected LocationPanel locationPanel;
	@UiField protected DockLayoutPanel rootPanel;
    @UiField protected SplitLayoutPanel splitPanel;
    @UiField protected Hyperlink locationLink;
    @UiField protected Hyperlink applicationLink;

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
                splitPanel.setWidgetMinSize(locationPanel, 200);
                rootPanel.setSize("100%", "100%");
                RootPanel.get("remotePollerMap").add(rootPanel);
            }
        });
		locationPanel.setEventBus(m_eventBus);
		
		m_locationManager.initialize();
	}
	
	@UiHandler("locationLink")
	public void onLocationClick(ClickEvent event) {
	    Window.alert("Show location panel");
	}

    @UiHandler("applicationLink")
    public void onApplicationClick(ClickEvent event) {
        Window.alert("Show application panel");
    }

private MapPanel createMapPanel() {
    MapPanel mapPanel;
    if (getMapImplementationType().equals("Mapquest")) {
        mapPanel = new MapQuestMapPanel(m_eventBus);
    } else if (getMapImplementationType().equals("GoogleMaps")) {
        mapPanel = new GoogleMapsPanel(m_eventBus);
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
