package org.opennms.netmgt.ticketd;

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.ticketd.Ticket.State;
import org.springframework.orm.ObjectRetrievalFailureException;

public class DefaultTicketerServiceLayer implements TicketerServiceLayer {
	
	private AlarmDao m_alarmDao;
    private TicketerPlugin m_ticketerPlugin;

	public void setAlarmDao(AlarmDao alarmDao) {
		m_alarmDao = alarmDao;
	}
    
    public void setTicketerPlugin(TicketerPlugin ticketerPlugin) {
        m_ticketerPlugin = ticketerPlugin;
    }
    
	public void cancelTicketForAlarm(int alarmId, String ticketId) {
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
		if (alarm == null) {
			throw new ObjectRetrievalFailureException("Unable to locate Alarm with ID: "+alarmId, null);
		}

		setTicketState(ticketId, Ticket.State.CANCELLED);
        
        alarm.setTTicketState(TroubleTicketState.CANCELLED);
        m_alarmDao.saveOrUpdate(alarm);
        
	}

    private void setTicketState(String ticketId, State state) { 
        Ticket ticket = m_ticketerPlugin.get(ticketId);
        ticket.setState(state);
        m_ticketerPlugin.saveOrUpdate(ticket);
    }
    
    
	public void closeTicketForAlarm(int alarmId, String ticketId) {
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
        
       setTicketState(ticketId, State.CLOSED);
        
		alarm.setTTicketState(TroubleTicketState.CLOSED);
		m_alarmDao.saveOrUpdate(alarm);
	}

	public void createTicketForAlarm(int alarmId) {
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
        
        Ticket ticket = createTicketFromAlarm(alarm);
        
        m_ticketerPlugin.saveOrUpdate(ticket);

        alarm.setTTicketId(ticket.getId());
		alarm.setTTicketState(TroubleTicketState.OPEN);
		m_alarmDao.saveOrUpdate(alarm);
	}

    private Ticket createTicketFromAlarm(OnmsAlarm alarm) {
        Ticket ticket = new Ticket();
        ticket.setSummary(alarm.getLogMsg());
        ticket.setDetails(alarm.getDescription());
        ticket.setId(alarm.getTTicketId());
        return ticket;
    }

	public void updateTicketForAlarm(int alarmId, String ticketId) {
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
        
        Ticket ticket = createTicketFromAlarm(alarm);
        m_ticketerPlugin.saveOrUpdate(ticket);
        
		alarm.setTTicketState(TroubleTicketState.OPEN);
		m_alarmDao.saveOrUpdate(alarm);
	}

}
