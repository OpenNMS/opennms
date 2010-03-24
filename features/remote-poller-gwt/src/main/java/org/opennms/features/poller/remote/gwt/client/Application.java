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
		Window.setTitle("OpenNMS - Remote Monitor");
		BINDER.createAndBindUi(this);

		locationPanel.setEventBus(m_eventBus);

		m_locationManager = new GoogleMapsLocationManager(this, m_eventBus, splitPanel);
		m_locationManager.initialize();
	}

	public void finished() {
		splitPanel.setSize("100%", "100%");
		splitPanel.setWidgetMinSize(locationPanel, 200);
		RootPanel.get("remotePollerMap").add(splitPanel);
	}
}
