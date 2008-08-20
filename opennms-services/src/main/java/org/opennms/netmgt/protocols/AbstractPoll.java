package org.opennms.netmgt.protocols;

import java.util.Collections;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.monitors.TimeoutTracker;

public abstract class AbstractPoll implements Poll {
    // default timeout of 3 seconds
    protected int m_timeout = 3000;
    
    /**
     * Set the timeout in milliseconds. 
     * @param milliseconds the timeout
     */
    public void setTimeout(int milliseconds) {
        m_timeout = milliseconds;
    }

    /**
     * Get the timeout in milliseconds.
     * @return the timeout
     */
    public int getTimeout() {
        return m_timeout;
    }

    public abstract PollStatus poll(TimeoutTracker tracker) throws InsufficientParametersException;
    
    public PollStatus poll() throws InsufficientParametersException {
        TimeoutTracker tracker = new TimeoutTracker(Collections.emptyMap(), 1, getTimeout());
        return poll(tracker);
    }

}
