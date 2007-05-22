package org.opennms.netmgt.ticketd;

public class NullTicketerPlugin implements TicketerPlugin {

    public Ticket get(String ticketId) {
        throw new UnsupportedOperationException(
                "NullITicketerPlugin.get not yet implemented.");
    }

    public void saveOrUpdate(Ticket ticket) {
        throw new UnsupportedOperationException(
                "NullITicketerPlugin.saveOrUpdate not yet implemented.");
    }

}
