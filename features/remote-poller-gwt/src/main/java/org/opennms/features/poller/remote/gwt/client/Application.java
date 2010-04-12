package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint
{
	interface Binder extends UiBinder<SplitLayoutPanel, Application> { }
	private static final Binder BINDER = GWT.create(Binder.class);

	private LocationManager m_locationManager;
	private final HandlerManager m_eventBus = new HandlerManager(null);

	@UiField protected LocationPanel locationPanel;
	@UiField protected SplitLayoutPanel splitPanel;

  /**
   * This is the entry point method.
   */
	public void onModuleLoad()
	{
		Log.setUncaughtExceptionHandler();

		BINDER.createAndBindUi(this);

		Window.setTitle("OpenNMS - Remote Monitor");
		Window.enableScrolling(false);
		Window.setMargin("0px");

		locationPanel.setEventBus(m_eventBus);
		
		m_locationManager = new DefaultLocationManager(m_eventBus, splitPanel, createMapPanel());
		
		m_locationManager.addLocationManagerInitializationCompleteEventHandler(new LocationManagerInitializationCompleteEventHander() {
            
            public void onInitializationComplete(LocationManagerInitializationCompleteEvent event) {
                splitPanel.setWidgetMinSize(locationPanel, 200);
                splitPanel.setSize("100%", "100%");
                RootPanel.get("remotePollerMap").add(splitPanel);
            }
        });
		
		m_locationManager.initialize();

		
	}

private MapPanel createMapPanel() {
    MapPanel mapPanel;
    if (getMapImplementationType().equals("Mapquest")) {
        mapPanel = new MapQuestMapPanel();
    } else if (getMapImplementationType().equals("GoogleMaps")) {
        mapPanel = new GoogleMapsPanel();
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
