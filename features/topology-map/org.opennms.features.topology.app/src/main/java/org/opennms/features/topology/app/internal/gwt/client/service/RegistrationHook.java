package org.opennms.features.topology.app.internal.gwt.client.service;

public interface RegistrationHook {
    
    public void registrationAdded(Registration registration);
    
    public void registrationRemoved(Registration registration);

}
