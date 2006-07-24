package org.opennms.web.svclayer;

import org.opennms.netmgt.dao.PollResultDao;
import org.opennms.netmgt.model.PollResult;
import org.opennms.web.services.PollerService;

public class DefaultDemandPollService implements DemandPollService {
	
	private PollResultDao m_pollResultDao;
	private PollerService m_pollerAPI;
	
	public void setPollResultDao(PollResultDao pollResultDao) {
		m_pollResultDao = pollResultDao;
	}
	
	public void setPollerAPI(PollerService pollerAPI) {
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
