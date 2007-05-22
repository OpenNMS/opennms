package org.opennms.netmgt.ticketd;

public class NullTicketerPlugin implements TicketerPlugin {

    public Ticket get(String ticketId) {
        Ticket ticket = new Ticket();
        ticket.setId("Ticketing not configured");
        return ticket;
    }

    public void saveOrUpdate(Ticket ticket) {
    }

}
