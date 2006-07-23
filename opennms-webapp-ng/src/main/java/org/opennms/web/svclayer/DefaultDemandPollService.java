package org.opennms.web.svclayer;

import java.util.Date;

import org.opennms.netmgt.dao.DemandPollDao;
import org.opennms.netmgt.model.DemandPoll;
import org.opennms.web.services.PollerService;

public class DefaultDemandPollService implements DemandPollService {
	
	private PollerService m_pollerService;
	private DemandPollDao m_demandPollDao;
	
	public void setDemandPollDao(DemandPollDao demandPollDao) {
		m_demandPollDao = demandPollDao;
	}
	
	public void setPollerAPI(PollerService pollerAPI) {
		m_pollerService = pollerAPI;
	}
	
	public DemandPoll pollMonitoredService(int nodeid, String ipAddr, int ifIndex, int serviceId) {
		DemandPoll demandPoll = new DemandPoll();
		demandPoll.setRequestTime(new Date());
		
		m_demandPollDao.save(demandPoll);
		m_pollerService.poll(nodeid, ipAddr, ifIndex, serviceId, demandPoll.getId());
		return demandPoll;
	}

	public DemandPoll getUpdatedResults(int resultId) {
		return m_demandPollDao.get(resultId);
	}

}
