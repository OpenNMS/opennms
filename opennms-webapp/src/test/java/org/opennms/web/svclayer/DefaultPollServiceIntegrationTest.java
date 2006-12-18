package org.opennms.web.svclayer;

import static org.easymock.EasyMock.createMock;

import java.io.File;
import java.io.FileReader;

import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.SiteStatusViewsFactory;
import org.opennms.netmgt.config.SurveillanceViewsFactory;
import org.opennms.netmgt.config.ViewsDisplayFactory;
import org.opennms.netmgt.model.DemandPoll;
import org.opennms.web.services.PollerService;
import org.opennms.web.svclayer.support.DefaultDemandPollService;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.transaction.annotation.Transactional;

public class DefaultPollServiceIntegrationTest extends AbstractTransactionalDataSourceSpringContextTests{

	private DemandPollService m_demandPollService;
        
	public DefaultPollServiceIntegrationTest() throws Exception {
		File f = new File("src/test/opennms-home");
		System.setProperty("opennms.home", f.getAbsolutePath());

		File rrdDir = new File("target/test/opennms-home/share/rrd");
		if (!rrdDir.exists()) {
			rrdDir.mkdirs();
		}
		System.setProperty("opennms.logs.dir", "src/test/opennms-home/logs");
		System.setProperty("rrd.base.dir", rrdDir.getAbsolutePath());
	}
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] {
				"META-INF/opennms/applicationContext-dao.xml",
				"org/opennms/web/svclayer/applicationContext-svclayer.xml",
		};
	}

	public DemandPollService getDemandPollService() {
		return m_demandPollService;
	}

	public void setDemandPollService(DemandPollService pollService) {
		m_demandPollService = pollService;
	}
        
        public void testBogus() {
            // Empty test to keep JUnit from complaining about no tests
        }
	
	@Transactional(readOnly=false)
	public void FIXMEtestPollMonitoredService() {
		PollerService api = createMock(PollerService.class);
		((DefaultDemandPollService)m_demandPollService).setPollerAPI(api);
		
		DemandPoll poll = m_demandPollService.pollMonitoredService(1, "192.168.2.100", 1, 1);
		assertNotNull("DemandPoll should not be null", poll);
		assertTrue("Polled service addr doesn't match...", poll.getId() >= 1);
	}
}
