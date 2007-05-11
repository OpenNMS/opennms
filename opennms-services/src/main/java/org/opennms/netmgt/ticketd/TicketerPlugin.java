package org.opennms.netmgt.ticketd;

public interface TicketerPlugin {
    
    public Ticket get(String ticketId);
    
    public void saveOrUpdate(Ticket ticket);
    

}
