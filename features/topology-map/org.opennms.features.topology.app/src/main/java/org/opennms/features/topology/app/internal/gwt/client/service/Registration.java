package org.opennms.features.topology.app.internal.gwt.client.service;

import java.util.Map;

public interface Registration {

    public ServiceRegistry getRegistry();
    
    public Class<?>[] getProvidedInterfaces();
    
    public <T> T getProvider(Class<T> service);
    
    public Object getProvider();
    
    public Map<String, String> getProperties();
    
    public boolean isUnregistered();
    
    public void unregister();
}
