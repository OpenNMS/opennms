package org.opennms.netmgt.protocols;

import org.opennms.netmgt.model.PollStatus;

public interface Poll {

    /**
     * Poll based on the configuration provided by the implementation
     * 
     * @return a {@link PollStatus} status object
     * @throws InsufficientParametersException
     */
    public PollStatus poll() throws InsufficientParametersException;
    
}
