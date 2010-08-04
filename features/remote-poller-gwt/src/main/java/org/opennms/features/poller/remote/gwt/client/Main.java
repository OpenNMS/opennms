package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.Window;

import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.RemoteEventServiceFactory;

public class Main implements EntryPoint {
    
    private class DeferredCommandExecutor implements CommandExecutor{

        public void schedule(IncrementalCommand command) {
            DeferredCommand.addCommand(command);
        }

        public void schedule(Command command) {
            DeferredCommand.addCommand(command);
        }
        
    }
    
    private HandlerManager m_eventBus;

    public void onModuleLoad() {
        m_eventBus = new HandlerManager(null);
        Application application = new Application(getEventBus());
        MapPanel mapPanel = createMap(application);
        
        LocationStatusServiceAsync remoteService = GWT.create(LocationStatusService.class);
        RemoteEventService remoteEventService = RemoteEventServiceFactory.getInstance().getRemoteEventService();
        application.initialize(new DefaultApplicationView(application, getEventBus(), mapPanel), remoteService, remoteEventService, new DeferredCommandExecutor());

    }

    private MapPanel createMap(Application application) {
        MapPanel mapPanel;
        if (getMapType().equals("Mapquest")) {
            mapPanel = new MapQuestMapPanel(getEventBus());
        } else if (getMapType().equals("GoogleMaps")) {
            mapPanel = new GoogleMapsPanel(getEventBus());
        } else if (getMapType().equals("OpenLayers")) {
            mapPanel = new OpenLayersMapPanel(getEventBus());
        } else {
            Window.alert("unknown map implementation: " + getMapType());
            throw new RuntimeException("unknown map implementation: " + getMapType());
        }
        return mapPanel;
    }

    /**
     * <p>getMapImplementationType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public native String getMapType() /*-{
        return $wnd.mapImplementation;
    }-*/;

    public HandlerManager getEventBus() {
        return m_eventBus;
    }

}
