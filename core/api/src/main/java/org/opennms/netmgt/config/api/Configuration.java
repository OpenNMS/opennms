package org.opennms.netmgt.config.api;

public interface Configuration<T> {
    public T get() throws ConfigurationException;
    public void save(T config) throws ConfigurationException;
}
