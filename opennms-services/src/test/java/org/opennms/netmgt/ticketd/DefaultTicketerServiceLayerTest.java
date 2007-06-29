/**
 * 
 */
package org.opennms.netmgt.ticketd;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.test.mock.EasyMockUtils;

import junit.framework.TestCase;

/**
 * @author david
 *
 */
public class DefaultTicketerServiceLayerTest extends TestCase {

	private DefaultTicketerServiceLayer m_defaultTicketerServiceLayer;
	private EasyMockUtils m_easyMockUtils;
	private AlarmDao m_alarmDao;
	private TicketerPlugin m_ticketerPlugin;
	private OnmsAlarm m_alarm;
	private Ticket m_ticket;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
		m_easyMockUtils = new EasyMockUtils();
		m_alarmDao = m_easyMockUtils.createMock(AlarmDao.class);
		m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
		m_ticketerPlugin = m_easyMockUtils.createMock(TicketerPlugin.class);
		m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);
		m_alarm = new OnmsAlarm();
		m_alarm.setId(1);
		m_alarm.setLogMsg("Test Logmsg");
		m_alarm.setDescription("Test Description");
		
		m_ticket = new Ticket();
		m_ticket.setId("4");
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}

	/**
	 * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#cancelTicketForAlarm(int, java.lang.String)}.
	 */
	public void testCancelTicketForAlarm() {
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        EasyMock.expect(m_ticketerPlugin.get(m_ticket.getId())).andReturn(m_ticket);
        
        expectNewTicketState(Ticket.State.CANCELLED);
        
        expectNewAlarmState(TroubleTicketState.CANCELLED);
        
        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.cancelTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        m_easyMockUtils.verifyAll();
	}

	/**
	 * @param state
	 */
	private void expectNewAlarmState(final TroubleTicketState state) {
		m_alarmDao.saveOrUpdate(m_alarm);
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

			public Object answer() throws Throwable {
				OnmsAlarm alarm = (OnmsAlarm) EasyMock.getCurrentArguments()[0];
				assertEquals(state, alarm.getTTicketState());
				return null;
			}
			
		});
	}

    /**
     * @param state
     */
    private void expectNewTicketState(final Ticket.State state) {
        m_ticketerPlugin.saveOrUpdate(m_ticket);
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                Ticket ticket = (Ticket) EasyMock.getCurrentArguments()[0];
                assertEquals(state, ticket.getState());
                return null;
            }
            
        });
    }

    /**
     * @param state
     */
    private void expectNewTicket() {
        m_ticketerPlugin.saveOrUpdate(EasyMock.isA(Ticket.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                Ticket ticket = (Ticket) EasyMock.getCurrentArguments()[0];
                assertNull(ticket.getId());
                ticket.setId("7");
                assertEquals(m_alarm.getLogMsg(), ticket.getSummary());
                assertEquals(m_alarm.getDescription(), ticket.getDetails());
                return null;
            }
            
        });
    }

	/**
	 * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#closeTicketForAlarm(int, java.lang.String)}.
	 */
	public void testCloseTicketForAlarm() {
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        EasyMock.expect(m_ticketerPlugin.get(m_ticket.getId())).andReturn(m_ticket);
        
        expectNewTicketState(Ticket.State.CLOSED);
        
        expectNewAlarmState(TroubleTicketState.CLOSED);
        
        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.closeTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        m_easyMockUtils.verifyAll();
	}

	/**
	 * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#createTicketForAlarm(int)}.
	 */
	public void testCreateTicketForAlarm() {
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        
        expectNewTicket();
        
        expectNewAlarmState(TroubleTicketState.OPEN);
        
        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.createTicketForAlarm(m_alarm.getId());

        m_easyMockUtils.verifyAll();
	}

	/**
	 * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#updateTicketForAlarm(int, java.lang.String)}.
	 */
	public void testUpdateTicketForAlarm() {
		
		m_ticket.setState(Ticket.State.CANCELLED);
		
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        EasyMock.expect(m_ticketerPlugin.get(m_ticket.getId())).andReturn(m_ticket);
        
        //expectUpdatedTicket();
        
        expectNewAlarmState(TroubleTicketState.CANCELLED);
        
        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.updateTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        m_easyMockUtils.verifyAll();
	}

    private void expectUpdatedTicket() {
        m_ticketerPlugin.saveOrUpdate(m_ticket);
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
        
            public Object answer() throws Throwable {
                Ticket ticket = (Ticket) EasyMock.getCurrentArguments()[0];
                assertEquals(Ticket.State.OPEN, ticket.getState());
                assertEquals(m_alarm.getDescription(), ticket.getDetails());
                return null;
            }
            
        });
    }

}
