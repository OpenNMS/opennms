package org.opennms.netmgt.ticketer.otrs;

import junit.framework.TestCase;
import java.rmi.RemoteException;

import org.opennms.integration.otrs.ticketservice.ArticleCore;
import org.opennms.integration.otrs.ticketservice.Credentials;
import org.opennms.integration.otrs.ticketservice.TicketCore;
import org.opennms.integration.otrs.ticketservice.TicketIDAndNumber;
import org.opennms.integration.otrs.ticketservice.TicketServiceLocator;
import org.opennms.integration.otrs.ticketservice.TicketServicePort_PortType;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.api.integration.ticketing.Ticket;
import java.io.File;
import java.util.Date;

import javax.xml.rpc.ServiceException;

public class OtrsTicketerPluginTest extends TestCase {

	
	// defaults for ticket
	
	private String defaultUser = new String("root@localhost");

	// defaults for article
	
	private String defaultArticleBody = new String("default body text");
	private String defaultArticleSubject = new String("default article subject");
	
	// login credentials
	
	Credentials m_creds  = new Credentials("opennms","opennms");
	
	DefaultOtrsConfigDao m_configDao;
	
	OtrsTicketerPlugin m_ticketer;
	
	Ticket m_ticket;
	
	private MockEventIpcManager m_eventIpcManager;
	
	 @Override
	 protected void setUp() throws Exception {

	        System.setProperty("opennms.home", "src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");

	        System.out.println("src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");
	        
	        m_eventIpcManager = new MockEventIpcManager();
	        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);

	        m_ticketer = new OtrsTicketerPlugin();
	        
	        m_creds = new Credentials("opennms","opennms");
	        
	        m_ticket = new Ticket();
	        m_ticket.setState(Ticket.State.OPEN);
	        m_ticket.setSummary("Ticket Summary for ticket: " + new Date());
	        m_ticket.setDetails("First Article for ticket: " + new Date());
			m_ticket.setUser("root@localhost");
			
	}

	public void testGet() {
		
		TicketIDAndNumber idAndNumber = null;
		
		String summary = new String("Ticket created by testGet()");
		String details = new String("This ticketwas created by OtrsTicketerPluginTest");
		
		try {
			idAndNumber = createTicketAndArticle(summary,details);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// now construct a ticket by hand.
		
		Ticket ticket = new Ticket();
		ticket.setId(String.valueOf(idAndNumber.getTicketNumber()));
		ticket.setState(Ticket.State.OPEN);
		ticket.setSummary(summary);
		ticket.setDetails(details);
		ticket.setUser(defaultUser);

        Ticket newTicket = m_ticketer.get(ticket.getId());
        
        assertTicketEquals(ticket, newTicket);
		
	}

	public void testSave() {
		
		m_ticketer.saveOrUpdate(m_ticket);
		
		Ticket retrievedTicket = m_ticketer.get(m_ticket.getId());
		
		assertTicketEquals(m_ticket, retrievedTicket);
		
	}
	
	public void testUpdate() {
		
		String firstArticle = new String("First Article");
		String secondArticle = new String("Second Article");
		
		// save with first article
		
		m_ticket.setDetails(firstArticle);
		
		m_ticketer.saveOrUpdate(m_ticket);
		
		// update with first article
		
		m_ticket.setDetails(secondArticle);
		
		m_ticketer.saveOrUpdate(m_ticket);
		
		// get a clean copy from the ID
		
		Ticket retrievedTicket = m_ticketer.get(m_ticket.getId());
		
		// compare the opennms ticket to one retrieved from OTRS
		
		assertTicketEquals(m_ticket, retrievedTicket);
		
		// should also have the first article as history
		
		// ensure that old ticket details still exist somewhere in the OTRS ticket
		
		if (retrievedTicket.getDetails().indexOf(firstArticle) <= 0 ) {
        	fail("could not find " + firstArticle + " in " + retrievedTicket.getDetails());
        }
		
	}
	
	public void testStateUpdate() throws InterruptedException {
		
		m_ticketer.saveOrUpdate(m_ticket);
		
		// my new ticket should be open

		assertEquals(m_ticket.getState(),Ticket.State.OPEN);
		
		// set it cancelled
		
		m_ticket.setState(Ticket.State.CANCELLED);
		
		// and save it
		
		m_ticketer.saveOrUpdate(m_ticket);
		
		// sleep for a bit
		
		Thread.sleep(100);
		
		// get a new copy
		
		Ticket retrievedTicket = m_ticketer.get(m_ticket.getId());
		
		// my new copy should be closed
		
        assertEquals(retrievedTicket.getState(),Ticket.State.CANCELLED);
		
	}
	
	
	 private void assertTicketEquals(Ticket existing, Ticket retrieved) {
	        assertEquals(existing.getId(), retrieved.getId());
	        assertEquals(existing.getState(), retrieved.getState());
	        // removed the test of user until I can figure out which user!
	        assertEquals(existing.getUser(), retrieved.getUser());
	        assertEquals(existing.getSummary(), retrieved.getSummary());
	        if (retrieved.getDetails().indexOf(existing.getDetails()) <= 0 ) {
	        	fail("could not find " + existing.getDetails() + " in " + retrieved.getDetails());
	        }
	 }
	 
	 // This is just to bootstrap a saved ticket so that we can get it back later
	 
	 private TicketIDAndNumber createTicketAndArticle(String ticketSubject, String articleBody) throws InterruptedException {
			
			TicketIDAndNumber idAndNumber = null;
			
			m_configDao = new DefaultOtrsConfigDao();
			
			TicketCore otrsTicket = new TicketCore();

			otrsTicket.setLock(m_configDao.getLock());
			otrsTicket.setQueue(m_configDao.getQueue());
			otrsTicket.setPriority(m_configDao.getPriority());
			otrsTicket.setState(m_configDao.getState());
			otrsTicket.setOwnerID(m_configDao.getOwnerID());
			
			otrsTicket.setUser(defaultUser);
			otrsTicket.setTitle(ticketSubject);
			
			Integer articleId = null;
			
			TicketServiceLocator service = new TicketServiceLocator();

			service.setTicketServicePortEndpointAddress(m_configDao.getEndpoint());
			
			m_creds  = new Credentials(m_configDao.getUserName(),m_configDao.getPassword());

			TicketServicePort_PortType port = null;
			
			try {
				port = service.getTicketServicePort();
			} catch (ServiceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				idAndNumber = port.ticketCreate(otrsTicket, m_creds);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ArticleCore otrsArticle = new ArticleCore();
				
			otrsArticle.setArticleType(m_configDao.getArticleType());
			otrsArticle.setSenderType(m_configDao.getArticleSenderType());
			otrsArticle.setContentType(m_configDao.getArticleContentType());
			otrsArticle.setHistoryType(m_configDao.getArticleHistoryType());
			otrsArticle.setHistoryComment(m_configDao.getArticleHistoryComment());
			otrsArticle.setSenderType(m_configDao.getArticleSenderType());
			
			otrsArticle.setSubject(defaultArticleSubject);
			otrsArticle.setFrom(m_configDao.getArticleFrom());
			otrsArticle.setBody(defaultArticleBody);
			otrsArticle.setUser(defaultUser);
			otrsArticle.setTicketID(idAndNumber.getTicketID());
			otrsArticle.setBody(articleBody);

			
			try {
				articleId = port.articleCreate(otrsArticle, m_creds);
				assertNotNull(articleId);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return idAndNumber;
	 }
	 
	 

}
