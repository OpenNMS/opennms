package org.opennms.netmgt.poller.remote;

import org.opennms.netmgt.model.PollStatus;

public class OnmsPollModel {
	
	private long m_pollInterval;
	
	private PollStatus m_currentStatus;
	
	private long m_lastPollTime;

	public PollStatus getCurrentStatus() {
		return m_currentStatus;
	}

	public void setCurrentStatus(PollStatus currentStatus) {
		m_currentStatus = currentStatus;
	}

	public long getLastPollTime() {
		return m_lastPollTime;
	}

	public void setLastPollTime(long lastPollTime) {
		m_lastPollTime = lastPollTime;
	}

	public long getPollInterval() {
		return m_pollInterval;
	}

	public void setPollInterval(long pollInterval) {
		m_pollInterval = pollInterval;
	}
	
}
