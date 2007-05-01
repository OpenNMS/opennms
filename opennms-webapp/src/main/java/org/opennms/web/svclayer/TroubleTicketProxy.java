package org.opennms.web.svclayer;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TroubleTicketProxy {

    public void createTicket(Integer alarmId);
    
    public void updateTicket(Integer alarmId);
    
    public void closeTicket(Integer alarmId);
}
