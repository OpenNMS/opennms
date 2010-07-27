package org.opennms.features.poller.remote.gwt.client;

import java.util.Date;

import org.opennms.features.poller.remote.gwt.client.FilterPanel.StatusSelectionChangedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEventHandler;

import com.google.gwt.core.client.EntryPoint;
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

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Application implements EntryPoint, LocationsUpdatedEventHandler {
    interface Binder extends UiBinder<DockLayoutPanel, Application> {
    }

    private static final DateTimeFormat UPDATE_TIMESTAMP_FORMAT = DateTimeFormat.getMediumDateTimeFormat();

    private static final Binder BINDER = GWT.create(Binder.class);

    private LocationManager m_locationManager;
    private final HandlerManager m_eventBus = new HandlerManager(null);

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

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // Log.setUncaughtExceptionHandler();

        BINDER.createAndBindUi(this);

        Window.setTitle("OpenNMS - Remote Monitor");
        Window.enableScrolling(false);
        Window.setMargin("0px");
        Window.addResizeHandler(new ResizeHandler() {
			public void onResize(final ResizeEvent event) {
				mainPanel.setHeight(getAppHeight().toString());
			}
        });

        // Register for all relevant events thrown by the UI components
        m_eventBus.addHandler(LocationsUpdatedEvent.TYPE, this);

        final DefaultLocationManager dlm = new DefaultLocationManager(m_eventBus, splitPanel, locationPanel, createMapPanel());
        m_locationManager = dlm;

        locationPanel.setEventBus(m_eventBus);

        for (final Widget w : statusesPanel) {
            if (w instanceof CheckBox) {
                final CheckBox cb = (CheckBox)w;
                final Status s = Status.valueOf(cb.getFormValue());
                dlm.onStatusSelectionChanged(s, cb.getValue());
            }
        }

        m_locationManager.initialize();
        
        splitPanel.setWidgetMinSize(locationPanel, 255);
        mainPanel.setSize("100%", "100%");
        RootPanel.get("remotePollerMap").add(mainPanel);
        
        updateTimestamp();
        onLocationClick(null);
        
    }

    Integer getAppHeight() {
    	final com.google.gwt.user.client.Element e = mainPanel.getElement();
		int extraHeight = e.getAbsoluteTop();
		return Window.getClientHeight() - extraHeight;
    }

    /**
     * <p>onLocationClick</p>
     *
     * @param event a {@link com.google.gwt.event.dom.client.ClickEvent} object.
     */
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

    /**
     * <p>onApplicationClick</p>
     *
     * @param event a {@link com.google.gwt.event.dom.client.ClickEvent} object.
     */
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

    /**
     * <p>getMapImplementationType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public native String getMapImplementationType() /*-{
        return $wnd.mapImplementation;
    }-*/;

    /** {@inheritDoc} */
    public void onLocationsUpdated(LocationsUpdatedEvent e) {
        updateTimestamp();
    }
    
    /**
     * <p>updateTimestamp</p>
     */
    public void updateTimestamp() {
        updateTimestamp.setText("Last update: " + UPDATE_TIMESTAMP_FORMAT.format(new Date()));
    }

    @UiHandler("statusDown")
    public void onDownClicked(final ClickEvent event) {
        m_eventBus.fireEvent(new StatusSelectionChangedEvent(Status.DOWN, statusDown.getValue()));
    }

    @UiHandler("statusDisconnected")
    public void onDisconnectedClicked(final ClickEvent event) {
        m_eventBus.fireEvent(new StatusSelectionChangedEvent(Status.DISCONNECTED, statusDisconnected.getValue()));
    }

    @UiHandler("statusMarginal")
    public void onMarginalClicked(final ClickEvent event) {
        m_eventBus.fireEvent(new StatusSelectionChangedEvent(Status.MARGINAL, statusMarginal.getValue()));
    }

    @UiHandler("statusUp")
    public void onUpClicked(final ClickEvent event) {
        m_eventBus.fireEvent(new StatusSelectionChangedEvent(Status.UP, statusUp.getValue()));
    }

    @UiHandler("statusStopped")
    public void onStoppedClicked(final ClickEvent event) {
        m_eventBus.fireEvent(new StatusSelectionChangedEvent(Status.STOPPED, statusStopped.getValue()));
    }

    @UiHandler("statusUnknown")
    public void onUnknownClicked(final ClickEvent event) {
        m_eventBus.fireEvent(new StatusSelectionChangedEvent(Status.UNKNOWN, statusUnknown.getValue()));
    }
}
