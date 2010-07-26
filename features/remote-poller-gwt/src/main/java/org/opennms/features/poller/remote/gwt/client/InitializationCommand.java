
/**
 * <p>InitializationCommand class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.features.poller.remote.gwt.client;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;


import com.google.gwt.maps.utility.client.DefaultPackage;
import com.google.gwt.maps.utility.client.GoogleMapsUtility;
import com.google.gwt.user.client.IncrementalCommand;
public class InitializationCommand implements IncrementalCommand {
	
    
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

    private Runnable m_finisher;
    

    /**
     * <p>Constructor for InitializationCommand.</p>
     *
     * @param abstractLocationManager a {@link org.opennms.features.poller.remote.gwt.client.DefaultLocationManager} object.
     * @param finisher a {@link java.lang.Runnable} object.
     * @param dataLoaders a {@link org.opennms.features.poller.remote.gwt.client.InitializationCommand.DataLoader} object.
     */
    public InitializationCommand(DefaultLocationManager abstractLocationManager, Runnable finisher, DataLoader...dataLoaders) {
        m_abstractLocationManager = abstractLocationManager;
        m_finisher = finisher;
        initialize(dataLoaders);
    }
    
    /**
     * <p>initialize</p>
     *
     * @param dataLoaders a {@link org.opennms.features.poller.remote.gwt.client.InitializationCommand.DataLoader} object.
     */
    protected void initialize(DataLoader...dataLoaders) {
        m_queue.addAll(Arrays.asList(dataLoaders));
    }

    
    /**
     * <p>getLocationManager</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.LocationManager} object.
     */
    public LocationManager getLocationManager() {
        return m_abstractLocationManager;
    }
    
    
    
	/**
	 * <p>execute</p>
	 *
	 * @return a boolean.
	 */
	public boolean execute() {
	    if(m_currentLoader == null || m_currentLoader.isLoaded()) {
	        m_currentLoader = m_queue.poll();
	        
	        if(m_currentLoader == null) {
	            finished();
	            return false;
	        }else {
	            m_currentLoader.load();
	        }
	        
	    }
	    
	    return true;
	    
	    
	}
	
	/**
	 * Override this
	 *
	 * @throws org.opennms.features.poller.remote.gwt.client.InitializationException if any.
	 */
	protected void finished() throws InitializationException {
		if(m_finisher != null) {
		    m_finisher.run();
		}
	}

    /**
     * <p>getRemoteService</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.LocationStatusServiceAsync} object.
     */
    public LocationStatusServiceAsync getRemoteService() {
        return m_abstractLocationManager.getRemoteService();
    }

}
