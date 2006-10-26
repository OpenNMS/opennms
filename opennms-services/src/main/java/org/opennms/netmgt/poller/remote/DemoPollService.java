/**
 * 
 */
package org.opennms.netmgt.poller.remote;

import java.util.Collection;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorLocator;

class DemoPollService implements PollService {
	
	private int m_repetitions;
	private int m_pollCount;
	private PollStatus m_currentStatus;

	public DemoPollService(int repetitions, PollStatus initialStatus) {
		m_repetitions = repetitions;
		m_currentStatus = initialStatus;
	}
	
	public DemoPollService(int repetitions) {
		this(repetitions, PollStatus.up());
	}
	
	public DemoPollService() {
		this(2);
	}

	public PollStatus poll(PolledService polledService) {
        PollStatus status = m_currentStatus;
        
        m_pollCount++;
        if (m_pollCount % m_repetitions == 0) {
        	m_currentStatus = (m_currentStatus.isDown() ? PollStatus.up(100+m_pollCount) : PollStatus.down("pollCount is "+m_pollCount));
        }
        
        return status;
    }

    public void initialize(PolledService polledService) {
        // TODO Auto-generated method stub
        
    }

    public void release(PolledService polledService) {
        // TODO Auto-generated method stub
        
    }

    public void setServiceMonitorLocators(Collection<ServiceMonitorLocator> locators) {
        // TODO Auto-generated method stub
        
    }
	
}