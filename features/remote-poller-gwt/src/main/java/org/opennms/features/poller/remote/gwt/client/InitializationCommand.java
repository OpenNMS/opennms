
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

import com.google.gwt.maps.utility.client.DefaultPackage;
import com.google.gwt.maps.utility.client.GoogleMapsUtility;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
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
    private final DefaultLocationManager m_abstractLocationManager;

    private MapPanelAdder m_mapPanelAdder;

    private EventServiceInitializer m_eventInitializer;
    
    private int m_state = 0;
    

    /**
     * <p>Constructor for InitializationCommand.</p>
     *
     * @param abstractLocationManager a {@link org.opennms.features.poller.remote.gwt.client.DefaultLocationManager} object.
     * @param dataLoaders a {@link org.opennms.features.poller.remote.gwt.client.InitializationCommand.DataLoader} object.
     */
    public InitializationCommand(DefaultLocationManager abstractLocationManager, MapPanelAdder mapPanelAdder, EventServiceInitializer eventInitializer) {
        m_abstractLocationManager = abstractLocationManager;
        m_mapPanelAdder = mapPanelAdder;
        m_eventInitializer = eventInitializer;
    }
    

    
    /**
     * <p>getLocationManager</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.LocationManager} object.
     */
    public DefaultLocationManager getLocationManager() {
        return m_abstractLocationManager;
    }
    
    
    
	/**
	 * <p>execute</p>
	 *
	 * @return a boolean.
	 */
	public boolean execute() {
	    if(m_currentLoader == null || m_currentLoader.isLoaded()) {
            
	        if(m_currentLoader == null) {
	            m_currentLoader =  m_mapPanelAdder;
            }else if(m_currentLoader == m_mapPanelAdder) {
                m_currentLoader =  m_eventInitializer;
            }else {
                m_currentLoader =  null;
                finished();
                return false;
            }
            
            m_currentLoader.load();
            
	    }
	    
	    return true;
//	    switch(m_state) {
//	        
//	    case 0:
//	        m_mapPanelAdder.load();
//	        m_state++;
//	        return true;
//	        
//	    case 1:
//	        if(m_mapPanelAdder.isLoaded()) {
//	            m_state++;
//	        }
//	        return true;
//	        
//	    case 2:    
//	        m_eventInitializer.load();
//	        m_state++;
//	        return true;
//	        
//	    case 3:
//	        if(m_eventInitializer.isLoaded()) {
//	            m_state++;
//	            return true;
//	        }
//	        
//	    case 4:
//	        finished();
//	        return false;
//	    }
	}

    /**
	 * Override this
	 *
	 * @throws org.opennms.features.poller.remote.gwt.client.InitializationException if any.
	 */
	protected void finished() throws InitializationException {
	   m_abstractLocationManager.initializationComplete();
	}

    /**
     * <p>getRemoteService</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.LocationStatusServiceAsync} object.
     */
    public LocationStatusServiceAsync getRemoteService() {
        return m_abstractLocationManager.getRemoteService();
    }

    void doCommand() {
        DeferredCommand.addCommand(this);
    }

}
