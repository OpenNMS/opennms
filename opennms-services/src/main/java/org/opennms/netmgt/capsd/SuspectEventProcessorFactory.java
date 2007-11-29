package org.opennms.netmgt.capsd;

public interface SuspectEventProcessorFactory {

    public abstract SuspectEventProcessor createSuspectEventProcessor(
            String ifAddress);

}