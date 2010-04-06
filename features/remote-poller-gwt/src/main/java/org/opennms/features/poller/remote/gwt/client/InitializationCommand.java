/**
 * 
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
    private final AbstractLocationManager m_abstractLocationManager;

    private Runnable m_finisher;
    

    public InitializationCommand(AbstractLocationManager abstractLocationManager, Runnable finisher, DataLoader...dataLoaders) {
        m_abstractLocationManager = abstractLocationManager;
        m_finisher = finisher;
        initialize(dataLoaders);
    }
    
    protected void initialize(DataLoader...dataLoaders) {
        m_queue.addAll(Arrays.asList(dataLoaders));
    }

    
    public LocationManager getLocationManager() {
        return m_abstractLocationManager;
    }
    
    
    
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
	 * @throws InitializationException
	 */
	protected void finished() throws InitializationException {
		if(m_finisher != null) {
		    m_finisher.run();
		}
	}

    public LocationStatusServiceAsync getRemoteService() {
        return m_abstractLocationManager.getRemoteService();
    }

}