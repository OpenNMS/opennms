package org.opennms.netmgt.ticketd;

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;

public class DefaultTicketerServiceLayer implements TicketerServiceLayer {
	
	private AlarmDao m_alarmDao;

	public void setAlarmDao(AlarmDao alarmDao) {
		m_alarmDao = alarmDao;
	}

	public void cancelTicketForAlarm(int alarmId, String ticketId) {
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
		alarm.setTTicketState(TroubleTicketState.CANCELLED);
		m_alarmDao.saveOrUpdate(alarm);
	}

	public void closeTicketForAlarm(int alarmId, String ticketId) {
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
		alarm.setTTicketState(TroubleTicketState.CLOSED);
		m_alarmDao.saveOrUpdate(alarm);
	}

	public void createTicketForAlarm(int alarmId) {
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
        alarm.setTTicketId(""+System.currentTimeMillis());
		alarm.setTTicketState(TroubleTicketState.OPEN);
		m_alarmDao.saveOrUpdate(alarm);
	}

	public void updateTicketForAlarm(int alarmId, String ticketId) {
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
		alarm.setTTicketState(TroubleTicketState.OPEN);
		m_alarmDao.saveOrUpdate(alarm);
	}

}
