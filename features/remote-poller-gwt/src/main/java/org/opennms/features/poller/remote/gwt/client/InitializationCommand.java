
/**
 * <p>InitializationCommand class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.features.poller.remote.gwt.client;

import java.util.LinkedList;
import java.util.Queue;

import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEventHandler;

import com.google.gwt.maps.utility.client.DefaultPackage;
import com.google.gwt.maps.utility.client.GoogleMapsUtility;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.RemoteEventServiceFactory;
public class InitializationCommand implements IncrementalCommand {
    //TODO: Refactor this away.
    
    public abstract static class DataLoader{
        boolean m_loaded = false;
        
        public void setLoaded() {
            m_loaded = true;
            onLoaded();
        };
        public boolean isLoaded() {
            return m_loaded;
        }
        
        public void load() {
            setLoaded();
        };
        public void onLoaded() {};
        
        public void loadApi( DefaultPackage api) {
            GoogleMapsUtility.loadUtilityApi(new Runnable() {
                public void run() {
                    setLoaded();
                }
            }, api);
        }
        
    }
    
    /**
     * 
     */
    private Queue<DataLoader> m_queue = new LinkedList<DataLoader>();
    
    private DataLoader m_currentLoader;
    private final DefaultLocationManager m_locationManager;

    private MapPanelAdder m_mapPanelAdder;

    private EventServiceInitializer m_eventInitializer;
    
    private int m_state = 0;

    private Application m_application;
    

    /**
     * <p>Constructor for InitializationCommand.</p>
     * @param application 
     *
     * @param abstractLocationManager a {@link org.opennms.features.poller.remote.gwt.client.DefaultLocationManager} object.
     * @param dataLoaders a {@link org.opennms.features.poller.remote.gwt.client.InitializationCommand.DataLoader} object.
     */
    public InitializationCommand(Application application, DefaultLocationManager abstractLocationManager, MapPanelAdder mapPanelAdder, EventServiceInitializer eventInitializer) {
        m_application = application;
        m_locationManager = abstractLocationManager;
        m_mapPanelAdder = mapPanelAdder;
        m_eventInitializer = eventInitializer;
    }
    

    
    /**
     * <p>getLocationManager</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.LocationManager} object.
     */
    public DefaultLocationManager getLocationManager() {
        return m_locationManager;
    }
    
    
    
	/**
	 * <p>execute</p>
	 *
	 * @return a boolean.
	 */
	public boolean execute() {
	    switch(m_state) {
	        
	    case 0:
            // Append the map panel to the main SplitPanel
            m_locationManager.getPanel().add(m_locationManager.m_mapPanel.getWidget());
            getApplication().updateTimestamp();
            getApplication().onLocationClick(null);

            LocationListener locationListener = new DefaultLocationListener(m_locationManager);
            final RemoteEventService eventService = RemoteEventServiceFactory.getInstance().getRemoteEventService();
            eventService.addListener(MapRemoteEventHandler.LOCATION_EVENT_DOMAIN, locationListener);
            eventService.addListener(null, locationListener);
            
            m_locationManager.getRemoteService().start(new AsyncCallback<Void>() {
                public void onFailure(Throwable throwable) {
                    // Log.debug("unable to start location even service backend", throwable);
                    Window.alert("unable to start location event service backend: " + throwable.getMessage());
                    throw new InitializationException("remote service start failed", throwable);
                }
            
                public void onSuccess(Void voidArg) {
                   updateMapSize();
                }
            });
	        m_state = 30;
	        return false;
	    }
	    
	    return false;
	}



    private void updateMapSize() {
        getApplication().splitPanel.setWidgetMinSize(getApplication().locationPanel, 255);
        getApplication().mainPanel.setSize("100%", "100%");
        RootPanel.get("remotePollerMap").add(getApplication().mainPanel);
        getApplication().mainPanel.setSize("100%", getApplication().getAppHeight().toString());
        getApplication().mainPanel.forceLayout();
    }

    private Application getApplication() {
        return m_application;
        
    }



    /**
	 * Override this
	 *
	 * @throws org.opennms.features.poller.remote.gwt.client.InitializationException if any.
	 */
	protected void finished() throws InitializationException {
	   m_locationManager.initializationComplete();
	}

    /**
     * <p>getRemoteService</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.LocationStatusServiceAsync} object.
     */
    public LocationStatusServiceAsync getRemoteService() {
        return m_locationManager.getRemoteService();
    }

    void doCommand() {
        DeferredCommand.addCommand(this);
    }

}
