package org.opennms.netmgt.ticketd;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TicketerServiceLayer {
	
	public void createTicketForAlarm(int alarmId);
	
	public void upateTicketForAlarm(int alarmId, int ticketId);
	
	public void closeTicketForAlarm(int alarmId, int ticketId);
	
	public void cancelTicketForAlarm(int alarmId, int ticketId);

}
