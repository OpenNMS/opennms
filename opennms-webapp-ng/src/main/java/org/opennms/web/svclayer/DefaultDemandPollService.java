package org.opennms.web.svclayer;

import org.opennms.netmgt.dao.PollResultDao;
import org.opennms.netmgt.model.PollResult;
import org.opennms.netmgt.poller.PollerAPI;

public class DefaultDemandPollService implements DemandPollService {
	
	private PollResultDao m_pollResultDao;
	private PollerAPI m_pollerAPI;
	
	public void setPollResultDao(PollResultDao pollResultDao) {
		m_pollResultDao = pollResultDao;
	}
	
	public void setPollerAPI(PollerAPI pollerAPI) {
		m_pollerAPI = pollerAPI;
	}
	
	public PollResult pollMonitoredService(int nodeid, String ipAddr, int ifIndex, int serviceId) {
		PollResult pollResult = new PollResult();
		m_pollResultDao.save(pollResult);
		m_pollerAPI.poll(nodeid, ipAddr, ifIndex, serviceId, pollResult.getId());
		return pollResult;
	}

	public PollResult getUpdatedResults(int resultId) {
		return m_pollResultDao.get(resultId);
	}

}
