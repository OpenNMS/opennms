package org.opennms.core.soa;

public interface RegistrationHook {
	
    public void registrationAdded(Registration registration);
    
    public void registrationRemoved(Registration registration);

}
