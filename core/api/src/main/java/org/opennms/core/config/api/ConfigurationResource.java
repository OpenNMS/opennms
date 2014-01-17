package org.opennms.core.config.api;

public interface ConfigurationResource<T> {
    public T get() throws ConfigurationResourceException;
    public void save(T config) throws ConfigurationResourceException;
}
