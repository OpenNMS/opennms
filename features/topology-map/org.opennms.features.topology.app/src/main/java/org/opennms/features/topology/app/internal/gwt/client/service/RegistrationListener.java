package org.opennms.features.topology.app.internal.gwt.client.service;

public interface RegistrationListener<T> {

    public void providerRegistered(Registration registration, T provider);
    
    public void providerUnregistered(Registration registration, T provider);
}
