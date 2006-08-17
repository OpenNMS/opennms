package org.opennms.netmgt.poller.remote;

import java.util.Date;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PollStatus;

public class PolledService {
	
	private String m_id;
	private OnmsMonitoredService m_monitoredService;
	private Date m_lastPollTime;
	private Date m_nextPollTime;
	private Date m_lastStatusChange;
	private PollStatus m_currentStatus = PollStatus.unknown();
	private OnmsPollModel m_pollModel;
	
	public PolledService(String id, OnmsMonitoredService monitoredService, OnmsPollModel pollModel) {
		m_id = id;
		m_monitoredService = monitoredService;
		m_pollModel = pollModel;
	}
	
	public String getId() {
		return m_id;
	}
	
	public String getNodeId() {
		return ""+m_monitoredService.getNodeId();
	}
	
	public String getNodeLabel() {
		return m_monitoredService.getIpInterface().getNode().getLabel();
	}
	
	public String getIpAddress() {
		return m_monitoredService.getIpAddress();
	}
	
	public String getServiceName() {
		return m_monitoredService.getServiceName();
	}
	
	public PollStatus getCurrentStatus() {
		return m_currentStatus;
	}

	public Date getLastPollTime() {
		return m_lastPollTime;
	}

	public Date getLastStatusChange() {
		return m_lastStatusChange;
	}
	
	public OnmsPollModel getPollModel() {
		return m_pollModel;
	}

	public OnmsMonitoredService getMonitoredService() {
		return m_monitoredService;
	}
	
	public void setNextPollTime(Date pollTime) {
		m_nextPollTime = pollTime;
	}
	
	public Date getNextPollTime() {
		return m_nextPollTime;
	}
	
	public void updateStatus(PollStatus newStatus, Date pollTime) {
		if (!m_currentStatus.equals(newStatus)) {
			m_lastStatusChange = pollTime;
		}
		m_currentStatus = newStatus;
		m_lastPollTime = pollTime;
		
	}
	
	
	
	
}
