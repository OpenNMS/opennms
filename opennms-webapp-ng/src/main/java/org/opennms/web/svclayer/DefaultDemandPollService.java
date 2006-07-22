package org.opennms.web.svclayer;

import org.opennms.netmgt.dao.PollResultDao;
import org.opennms.netmgt.model.PollResult;

public class DefaultDemandPollService implements DemandPollService {
	
	private PollResultDao m_pollResultDao;
	
	public void setPollResultDao(PollResultDao pollResultDao) {
		m_pollResultDao = pollResultDao;
	}
	
//	public void setPollerAPI(PollerAPI pollerAPI) {
//	}

	public PollResult pollMonitoredService(int nodeid, String ipAddr, int ifIndex, int serviceId) {
		PollResult pollResult = new PollResult();
		//pollResult.setMessage("Queued for Polling");
		m_pollResultDao.save(pollResult);
//		m_poller.poll(1, "192.168.1.1", 1, 3, pollResult.getId());
		return pollResult;
	}

	public PollResult getUpdatedResults(int resultId) {
		throw new UnsupportedOperationException("not yet implemented");
	}

}
