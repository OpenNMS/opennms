package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.utility.client.DefaultPackage;
import com.google.gwt.maps.utility.client.GoogleMapsUtility;
import com.google.gwt.maps.utility.client.markermanager.MarkerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.RemoteEventServiceFactory;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint
{
	interface Binder extends UiBinder<SplitLayoutPanel, Application> { }
	private static final Binder BINDER = GWT.create(Binder.class);
	private static int utilityCounter = 0;
	private final HandlerManager m_eventBus = new HandlerManager(null);
	private final LocationStatusServiceAsync remoteService = GWT.create(LocationStatusService.class);

	@UiField protected LocationPanel locationPanel;
	@UiField protected SplitLayoutPanel splitPanel;
	@UiField protected MapWidget mapWidget;

  /**
   * This is the entry point method.
   */
	public void onModuleLoad()
	{
		Window.setTitle("OpenNMS - Remote Monitor");
		remoteService.getApiKey(new ConfigureMapsAsyncCallback(this));

	}

	private void init() {
		if (--utilityCounter == 0) {
			mapWidget.checkResizeAndCenter();

			final MarkerManager markerManager = MarkerManager.newInstance(mapWidget);
			final DefaultLocationManager manager = new DefaultLocationManager(m_eventBus, mapWidget, markerManager);
			locationPanel.setEventBus(m_eventBus);

			final DefaultLocationListener locationListener = new DefaultLocationListener(manager);

			final RemoteEventService eventService = RemoteEventServiceFactory.getInstance().getRemoteEventService();
			eventService.addListener(Location.LOCATION_EVENT_DOMAIN, locationListener);

			remoteService.start(new VoidAsyncCallback());
		}
	}

	private final class VoidAsyncCallback implements AsyncCallback<Void> {
		public void onFailure(final Throwable throwable) {
			GWT.log("failed to start location status service", throwable);
		}

		public void onSuccess(final Void arg) {
			GWT.log("started location status service");
		}
	}

	private final class ConfigureMapsAsyncCallback implements AsyncCallback<String> {
		private final String m_mapsUrl = "http://maps.google.com/maps?gwt=1&file=api&v=2";
		private Application m_application;

		public ConfigureMapsAsyncCallback(Application app) {
			m_application = app;
		}

		public void onFailure(Throwable throwable) {
			GWT.log("failed to get API key", throwable);
			doUpdate(null);
		}

		public void onSuccess(String apiKey) {
			doUpdate(apiKey);
		}
		
		public void doUpdate(String apiKey) {
			if (apiKey != null) {
				AjaxLoader.init(apiKey);
			}

			AjaxLoader.loadApi("maps", "2", new PostInitialization(m_application), null);
		}
	}

	private final class PostInitialization implements Runnable {
		private Application m_application;

		public PostInitialization(Application application) {
			m_application = application;
		}

		public void run() {
			SplitLayoutPanel outer = BINDER.createAndBindUi(m_application);

			// Set up maps
			mapWidget.setSize("100%", "100%");
			mapWidget.setUIToDefault();
			mapWidget.addControl(new LargeMapControl());
//			mapWidget.setZoomLevel(4);
			mapWidget.setContinuousZoom(true);
			mapWidget.setScrollWheelZoomEnabled(true);

			RootPanel.get("remotePollerMap").add(outer);

			UtilityApiInitializer markerInitializer = new UtilityApiInitializer();
			UtilityApiInitializer iconInitializer   = new UtilityApiInitializer();
			
			GoogleMapsUtility.loadUtilityApi(markerInitializer, DefaultPackage.MARKER_MANAGER);
			GoogleMapsUtility.loadUtilityApi(iconInitializer, DefaultPackage.MAP_ICON_MAKER);

			// Set the window to be full-sized and handle resizes
			outer.setSize("100%", "100%");
			outer.setWidgetMinSize(locationPanel, 200);

			Window.enableScrolling(false);
			Window.setMargin("0px");
			Window.addResizeHandler(new ResizeHandler() {
				public void onResize(final ResizeEvent resizeEvent) {
					if (mapWidget != null) {
						mapWidget.checkResizeAndCenter();
					}
				}
			});
		}
		
	}
	private final class UtilityApiInitializer implements Runnable {
		public UtilityApiInitializer() {
			utilityCounter++;
		}

		public void run() {
			init();
		}
	}

}
