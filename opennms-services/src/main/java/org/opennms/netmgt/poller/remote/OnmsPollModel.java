package org.opennms.netmgt.poller.remote;

import java.io.Serializable;
import java.util.Date;


public class OnmsPollModel implements Serializable {
	
	private long m_pollInterval;
    
    public OnmsPollModel() {
        m_pollInterval = -1L;
    }
    
    public OnmsPollModel(long pollInterval) {
        m_pollInterval = pollInterval;
    }
	
	public long getPollInterval() {
		return m_pollInterval;
	}

	public void setPollInterval(long pollInterval) {
		m_pollInterval = pollInterval;
	}

    public Date getNextPollTime(Date lastPollTime) {
        return new Date(lastPollTime.getTime()+m_pollInterval);
    }

    public Date getPreviousPollTime(Date initialPollTime) {
        return new Date(initialPollTime.getTime()-m_pollInterval);
    }
	
	
	
}
