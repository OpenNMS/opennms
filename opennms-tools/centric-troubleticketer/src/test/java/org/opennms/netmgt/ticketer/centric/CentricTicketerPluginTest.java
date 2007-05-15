package org.opennms.netmgt.ticketer.centric;

import java.util.Date;

import junit.framework.TestCase;

import org.opennms.netmgt.ticketd.Ticket;

public class CentricTicketerPluginTest extends TestCase {
    
    CentricTicketerPlugin m_ticketer;
    
    
    
    @Override
    protected void setUp() throws Exception {
        m_ticketer = new CentricTicketerPlugin();
    }



    public void testSave() {
        
        Ticket ticket = new Ticket();
        ticket.setState(Ticket.State.OPEN);
        ticket.setSummary("This is the summary");
        ticket.setDetails("These are the details");
        
        m_ticketer.saveOrUpdate(ticket);
        
        assertNotNull(ticket.getId());
        
        Ticket newTicket = m_ticketer.get(ticket.getId());
        
        assertNotNull(newTicket);
        assertTicketEquals(ticket, newTicket);
        
    }
    
    public void testUpdate() {
        
        Ticket ticket = new Ticket();
        ticket.setState(Ticket.State.OPEN);
        ticket.setSummary("A Ticket at "+new Date());
        ticket.setDetails("Ticket details for ticket: "+new Date());
        
        m_ticketer.saveOrUpdate(ticket);
        
        assertNotNull(ticket.getId());
        
        Ticket newTicket = m_ticketer.get(ticket.getId());
        
        assertNotNull(newTicket);
        assertTicketEquals(ticket, newTicket);
        
        
        newTicket.setState(Ticket.State.CANCELLED);
        newTicket.setDetails("These details have changed");
        
        m_ticketer.saveOrUpdate(newTicket);
        
        Ticket newerTicket = m_ticketer.get(ticket.getId());
        
        assertTicketEquals(newTicket, newerTicket);
    }



    private void assertTicketEquals(Ticket ticket, Ticket newTicket) {
        assertEquals(ticket.getId(), newTicket.getId());
        assertEquals(ticket.getState(), newTicket.getState());
        assertEquals(ticket.getSummary(), newTicket.getSummary());
        assertEquals(ticket.getDetails(), newTicket.getDetails());
    }
    
    public void testGet() {
        
        Ticket newTicket = m_ticketer.get("67");
        
        assertNotNull(newTicket);
        assertEquals("67", newTicket.getId());
        assertEquals("This is the summary", newTicket.getSummary());
        assertEquals("These are the details", newTicket.getDetails());
        
    }
    
    
    
    

}
