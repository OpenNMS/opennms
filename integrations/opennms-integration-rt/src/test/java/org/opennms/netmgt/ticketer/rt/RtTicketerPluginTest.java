package org.opennms.netmgt.ticketer.rt;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.test.mock.MockLogAppender;

public class RtTicketerPluginTest extends TestCase {
    
    /**
     * Test Cases for RtTicketerPlugin
     * 
     * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
     * 
     */

	DefaultRtConfigDao m_configDao;
	
	RtTicketerPlugin m_ticketer;
	
	Ticket m_ticket;
	
	String ticketID;
	

    /**
     * Don't run this test unless the runRtTests property
     * is set to "true".
     */
    @Override
    protected void runTest() throws Throwable {
        if (!isRunTest()) {
            System.err.println("Skipping test '" + getName() + "' because system property '" + getRunTestProperty() + "' is not set to 'true'");
            return;
        }
            
        try {
            System.err.println("------------------- begin "+getName()+" ---------------------");
            super.runTest();
        } finally {
            System.err.println("------------------- end "+getName()+" -----------------------");
        }
    }

    private boolean isRunTest() {
        return Boolean.getBoolean(getRunTestProperty());
    }

    private String getRunTestProperty() {
        return "runRtTests";
    }

	 @Override
	 protected void setUp() throws Exception {
	        MockLogAppender.setupLogging();

	        System.setProperty("opennms.home", "src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");

	        System.out.println("src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");

	        m_ticketer = new RtTicketerPlugin();
	        
	        m_ticket = new Ticket();
	        m_ticket.setState(Ticket.State.OPEN);
	        m_ticket.setSummary("Ticket Summary for ticket: " + new Date());
	        m_ticket.setDetails("First Article for ticket: " + new Date());
	        m_ticket.setUser("root@localhost");
	        
	        
	}
	
	public void testSaveAndGet() {


		try {
            m_ticketer.saveOrUpdate(m_ticket);
            Ticket retrievedTicket = m_ticketer.get(m_ticket.getId());
            assertTicketEquals(m_ticket, retrievedTicket);
        } catch (PluginException e) {
            fail("Something failed in the ticketer plugin");
            e.printStackTrace();
        }
        
	}
	
public void testUpdateAndGet() {
             
        try {
            m_ticketer.saveOrUpdate(m_ticket);
            Ticket savedTicket = m_ticketer.get(m_ticket.getId());
            assertTicketEquals(m_ticket, savedTicket);
            m_ticket.setState(Ticket.State.CLOSED);
            m_ticketer.saveOrUpdate(m_ticket);
            System.out.println("before update, ticket status was " + savedTicket.getState().toString());
            Ticket updatedTicket =  m_ticketer.get(m_ticket.getId());
            System.out.println("after update, ticket status was " + updatedTicket.getState().toString());
            assertTicketEquals(m_ticket, updatedTicket);
        } catch (PluginException e) {
            fail("Something failed in the ticketer plugin");
            e.printStackTrace();
        }

        
    }
		
	 private void assertTicketEquals(Ticket existing, Ticket retrieved) {
	        assertEquals(existing.getId(), retrieved.getId());
	        assertEquals(existing.getState(), retrieved.getState());
	        assertEquals(existing.getUser(), retrieved.getUser());
	        assertEquals(existing.getSummary(), retrieved.getSummary());
	 }
	 
}
