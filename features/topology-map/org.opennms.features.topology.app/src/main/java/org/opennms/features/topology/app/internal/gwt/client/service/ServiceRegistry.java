package org.opennms.features.topology.app.internal.gwt.client.service;

import java.util.Collection;
import java.util.Map;

public interface ServiceRegistry {
    
    public Registration register(Object serviceProvider, Class<?>... services);
    
    public Registration register(Object serviceProvider, Map<String, String> properties, Class<?>... services);
    
    public <T> T findProvider(Class<T> serviceInterface);
    
    public <T> T findProvider(Class<T> serviceInterface, String filter);
    
    public <T> Collection<T> findProviders(Class<T> service);
    
    public <T> Collection<T> findProviders(Class<T> service, String filter);
    
    public <T> void addListener(Class<T> service, RegistrationListener<T> listener);
    
    public <T> void addListener(Class<T> service, RegistrationListener<T> listener, boolean notifyForExistingProviders);
    
    public <T> void removeListener(Class<T> service, RegistrationListener<T> listener);
    
    public void addRegistrationHook(RegistrationHook hook, boolean notifyForExistingProviders);
    
    public void removeRegistrationHook(RegistrationHook hook);

    public <T> T cast(Object vertexClickHandler, Class<T> class1);
    
}
