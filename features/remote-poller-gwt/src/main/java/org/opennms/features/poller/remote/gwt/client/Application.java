package org.opennms.features.poller.remote.gwt.client;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

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

//		m_locationManager = new MapquestLocationManager(this, m_eventBus, splitPanel);
		m_locationManager = new GoogleMapsLocationManager(this, m_eventBus, splitPanel);
		m_locationManager.initialize();
	}

	public void finished() {
		splitPanel.setWidgetMinSize(locationPanel, 200);
		splitPanel.setSize("100%", "100%");
		RootPanel.get("remotePollerMap").add(splitPanel);
	}
}
