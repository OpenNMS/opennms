package org.opennms.web.svclayer.support;

import java.util.Date;

import org.opennms.netmgt.dao.DemandPollDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.DemandPoll;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.web.services.PollerService;
import org.opennms.web.svclayer.DemandPollService;

public class DefaultDemandPollService implements DemandPollService {
	
	private PollerService m_pollerService;
	private DemandPollDao m_demandPollDao;
	private MonitoredServiceDao m_monitoredServiceDao;
	
	public void setDemandPollDao(DemandPollDao demandPollDao) {
		m_demandPollDao = demandPollDao;
	}
	
	public void setPollerAPI(PollerService pollerAPI) {
		m_pollerService = pollerAPI;
	}
	
	public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
		m_monitoredServiceDao = monitoredServiceDao;
	}
	
	public DemandPoll pollMonitoredService(int nodeId, String ipAddr, int ifIndex, int serviceId) {
		DemandPoll demandPoll = new DemandPoll();
		demandPoll.setRequestTime(new Date());
		
		m_demandPollDao.save(demandPoll);
		
		OnmsMonitoredService monSvc = m_monitoredServiceDao.get(nodeId, ipAddr, ifIndex, serviceId);
		
		if (monSvc == null) {
			throw new RuntimeException("Service doesn't exist: "+monSvc);
		}
		m_pollerService.poll(monSvc, demandPoll.getId());
		return demandPoll;
	}

	public DemandPoll getUpdatedResults(int pollId) {
		return m_demandPollDao.get(pollId);
	}

}
