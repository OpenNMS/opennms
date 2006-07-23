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
import org.opennms.netmgt.dao.DemandPollDao;
import org.opennms.netmgt.dao.PollResultDao;
import org.opennms.netmgt.model.DemandPoll;
import org.opennms.netmgt.model.PollResult;
import org.opennms.web.services.PollerService;

public class DemandPollServiceTest extends TestCase {
	
	private DefaultDemandPollService m_demandPollService;
	private DemandPollDao m_demandPollDao;
	private PollerService m_pollerService;
	private SingleDemandPollStore m_pollStore;

	protected void setUp() throws Exception {
		m_demandPollDao = createMock(DemandPollDao.class);
		m_pollerService = createMock(PollerService.class);
		m_pollStore = new SingleDemandPollStore();

		m_demandPollService = new DefaultDemandPollService();
		m_demandPollService.setDemandPollDao(m_demandPollDao);
		m_demandPollService.setPollerAPI(m_pollerService);
	}

	protected void tearDown() throws Exception {
	}
	
	class SingleDemandPollStore implements DemandPollDao {
		
		int m_id = 13;
		DemandPoll m_demandPoll = null;
		
		public int getExpectedId() {
			return m_id;
		}

		public void save(DemandPoll poll) {
			poll.setId(m_id);
			m_demandPoll = poll;
		}

		public DemandPoll get(int resultId) {
			if (resultId == m_id)
				return m_demandPoll;
			return null;
		}
		
		
	}
	
	public void testPollMonitoredService() {
		
		final int expectedResultId = m_pollStore.getExpectedId();

		// anticipate a call to the dao save with a pollResult
		m_demandPollDao.save(isA(DemandPoll.class));
		expectLastCall().andAnswer(new IAnswer<Object>() {

			public Object answer() throws Throwable {
				DemandPoll poll = (DemandPoll)getCurrentArguments()[0];
				m_pollStore.save(poll);
				return null;
			}
			
		});
		

		m_pollerService.poll(1, "192.168.1.1", 1, 3, expectedResultId);
		
		replay(m_demandPollDao);
		replay(m_pollerService);
		
		DemandPoll result = m_demandPollService.pollMonitoredService(1, "192.168.1.1", 1, 3);

		verify(m_demandPollDao);
		verify(m_pollerService);

		assertNotNull("Null is an invalid response from pollMonitoredService", result);
		assertEquals("Expected Id to be set by dao", expectedResultId, result.getId());
		
	}
	
	public void testGetUpdatedResults() {
		
		final int resultId = 3;
		
		DemandPoll expectedResult = new DemandPoll();
		
		
		expect(m_demandPollDao.get(resultId)).andReturn(expectedResult);
		replay(m_demandPollDao);
		
		DemandPoll result = m_demandPollService.getUpdatedResults(resultId);
		
		verify(m_demandPollDao);
		
		assertEquals(expectedResult, result);
	}

}
