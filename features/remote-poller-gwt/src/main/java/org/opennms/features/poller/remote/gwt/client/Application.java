package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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

		// Defer the rest of initialization so the uncaught exception handler can catch it
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				initialize();
			}
			
		});
	}

	public void initialize() {
		BINDER.createAndBindUi(this);

		Window.setTitle("OpenNMS - Remote Monitor");
		Window.enableScrolling(false);
		Window.setMargin("0px");

		locationPanel.setEventBus(m_eventBus);
		
		// no reflection in GWT  :(
		if (getMapImplementationType() == null) {
			Window.alert("unable to determine gwt.maptype setting from opennms.properties!");
			throw new RuntimeException("unable to determine gwt.maptype setting from opennms.properties!");
		} else if (getMapImplementationType().equals("Mapquest")) {
			m_locationManager = new MapquestLocationManager(m_eventBus, splitPanel);
		} else if (getMapImplementationType().equals("GoogleMaps")) {
			m_locationManager = new GoogleMapsLocationManager(m_eventBus, splitPanel);
		} else {
			Window.alert("unknown map implementation: " + getMapImplementationType());
			throw new RuntimeException("unknown map implementation: " + getMapImplementationType());
		}
		
		m_locationManager.addLocationManagerInitializationCompleteEventHandler(new LocationManagerInitializationCompleteEventHander() {
            
            public void onInitializationComplete(LocationManagerInitializationCompleteEvent event) {
                finished();
            }
        });
		
		m_locationManager.initialize();
	}

	public native String getMapImplementationType() /*-{
		return $wnd.mapImplementation;
	}-*/;

	public void finished() {
		splitPanel.setWidgetMinSize(locationPanel, 200);
		splitPanel.setSize("100%", "100%");
		RootPanel.get("remotePollerMap").add(splitPanel);
	}
}
