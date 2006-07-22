package org.opennms.web.svclayer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import junit.framework.TestCase;

import org.easymock.IAnswer;
import org.opennms.netmgt.dao.PollResultDao;
import org.opennms.netmgt.model.PollResult;
import org.opennms.netmgt.poller.PollerAPI;

public class DemandPollServiceTest extends TestCase {
	
	private DefaultDemandPollService m_demandPollService;
	private PollResultDao m_pollResultDao;
	private PollerAPI m_pollerAPI;

	protected void setUp() throws Exception {
		m_pollResultDao = createMock(PollResultDao.class);
		m_pollerAPI = createMock(PollerAPI.class);

		m_demandPollService = new DefaultDemandPollService();
		m_demandPollService.setPollResultDao(m_pollResultDao);
		m_demandPollService.setPollerAPI(m_pollerAPI);
	}

	protected void tearDown() throws Exception {
	}
	
	public void testPollMonitoredService() {
		
		final int expectedResultId = 7;

		// anticipate a call to the dao save with a pollResult
		m_pollResultDao.save(isA(PollResult.class));
		
		// make sure that we update the id property of the passed in result
		expectLastCall().andAnswer(new IAnswer<Object>() {

			public Object answer() throws Throwable {
				PollResult result = (PollResult)getCurrentArguments()[0];
				result.setId(expectedResultId);
				return null;
			}
			
		});
		
		m_pollerAPI.poll(1, "192.168.1.1", 1, 3, expectedResultId);
		
		replay(m_pollResultDao);
		replay(m_pollerAPI);
		
		PollResult result = m_demandPollService.pollMonitoredService(1, "192.168.1.1", 1, 3);

		verify(m_pollResultDao);
		verify(m_pollerAPI);

		assertNotNull("Null is an invalid response from pollMonitoredService", result);
		assertEquals("Expected Id to be set by dao", expectedResultId, result.getId());
		
	}
	
	public void testGetUpdatedResults() {
		
		final int resultId = 3;
		
		PollResult expectedResult = new PollResult(3);
		
		expect(m_pollResultDao.get(resultId)).andReturn(expectedResult);
		replay(m_pollResultDao);
		
		PollResult result = m_demandPollService.getUpdatedResults(resultId);
		
		verify(m_pollResultDao);
		
		assertEquals(expectedResult, result);
	}

}
