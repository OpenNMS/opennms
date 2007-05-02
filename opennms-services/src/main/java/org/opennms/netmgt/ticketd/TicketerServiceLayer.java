package org.opennms.netmgt.ticketd;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TicketerServiceLayer {
	
	public void createTicketForAlarm(int alarmId);
	
	public void updateTicketForAlarm(int alarmId, String ticketId);
	
	public void closeTicketForAlarm(int alarmId, String ticketId);
	
	public void cancelTicketForAlarm(int alarmId, String ticketId);

}
