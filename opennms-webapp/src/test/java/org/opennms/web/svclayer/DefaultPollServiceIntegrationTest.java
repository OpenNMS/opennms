package org.opennms.web.svclayer;

import static org.easymock.EasyMock.createMock;

import org.opennms.netmgt.model.DemandPoll;
import org.opennms.web.services.PollerService;
import org.opennms.web.svclayer.support.DefaultDemandPollService;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.transaction.annotation.Transactional;

public class DefaultPollServiceIntegrationTest extends AbstractTransactionalDataSourceSpringContextTests{

	private DemandPollService m_demandPollService;
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] {
				"META-INF/opennms/applicationContext-dao.xml",
				"org/opennms/web/svclayer/applicationContext-svclayer.xml" };
	}

	public DemandPollService getDemandPollService() {
		return m_demandPollService;
	}

	public void setDemandPollService(DemandPollService pollService) {
		m_demandPollService = pollService;
	}
	
	@Transactional(readOnly=false)
	public void testPollMonitoredService() {
		PollerService api = createMock(PollerService.class);
		((DefaultDemandPollService)m_demandPollService).setPollerAPI(api);
		
		DemandPoll poll = m_demandPollService.pollMonitoredService(1, "192.168.2.100", 1, 1);
		assertNotNull("DemandPoll should not be null", poll);
		assertTrue("Polled service addr doesn't match...", poll.getId() >= 1);
	}
}
