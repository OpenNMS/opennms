package org.opennms.web.svclayer;

import static org.easymock.EasyMock.*;

import org.easymock.IAnswer;
import org.opennms.netmgt.dao.PollResultDao;
import org.opennms.netmgt.model.PollResult;

import junit.framework.TestCase;

public class DemandPollServiceTest extends TestCase {
	
	private DefaultDemandPollService m_demandPollService;
	private PollResultDao m_pollResultDao;

	protected void setUp() throws Exception {
		m_pollResultDao = createMock(PollResultDao.class);

		m_demandPollService = new DefaultDemandPollService();
		m_demandPollService.setPollResultDao(m_pollResultDao);
	}

	protected void tearDown() throws Exception {
	}
	
	public void testPollMonitoredService() {

		// anticipate a call to the dao save with a pollResult
		m_pollResultDao.save(isA(PollResult.class));
		
		// make sure that we update the id property of the passed in result
		expectLastCall().andAnswer(new IAnswer<Object>() {

			public Object answer() throws Throwable {
				PollResult result = (PollResult)getCurrentArguments()[0];
				result.setId(3);
				return null;
			}
			
		});
		
	    
		
		replay(m_pollResultDao);
		
		PollResult result = m_demandPollService.pollMonitoredService(1, "192.168.1.1", 1, 3);

		verify(m_pollResultDao);

		assertNotNull("Null is an invalid response from pollMonitoredService", result);
		assertEquals("Expected Id to be set by dao", 3, result.getId());
		
		
		
		
	}
	
	

}
