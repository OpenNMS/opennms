package org.opennms.netmgt.poller.remote;

import java.util.Date;

import org.opennms.netmgt.model.PollStatus;

public class ServicePollState {
    
    private PolledService m_polledService;
    private int m_index;
    private PollStatus m_lastPoll;

    public ServicePollState(PolledService polledService, int index) {
        m_polledService = polledService;
        m_index = index;
    }

    public PollStatus getLastPoll() {
        return m_lastPoll;
    }

    public void setLastPoll(PollStatus lastPoll) {
        m_lastPoll = lastPoll;
    }
    
    public Date getLastPollTime() {
        return m_lastPoll.getTimestamp();
    }
    
    public Date getNextPollTime() {
        return m_polledService.getPollModel().getNextPollTime(getLastPollTime());
    }

    public int getIndex() {
        return m_index;
    }

    public PolledService getPolledService() {
        return m_polledService;
    }

    public void setInitialPollTime(Date initialPollTime) {
        m_lastPoll = PollStatus.unavailable();
        m_lastPoll.setTimestamp(m_polledService.getPollModel().getPreviousPollTime(initialPollTime));
    }

}
