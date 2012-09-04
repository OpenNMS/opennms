/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ticketer.otrs;

import junit.framework.TestCase;
import java.rmi.RemoteException;

import org.opennms.integration.otrs.ticketservice.ArticleCore;
import org.opennms.integration.otrs.ticketservice.Credentials;
import org.opennms.integration.otrs.ticketservice.TicketCore;
import org.opennms.integration.otrs.ticketservice.TicketIDAndNumber;
import org.opennms.integration.otrs.ticketservice.TicketServiceLocator;
import org.opennms.integration.otrs.ticketservice.TicketServicePort_PortType;
import org.opennms.api.integration.ticketing.PluginException;
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
	
	DefaultOtrsConfigDao m_configDao;
	
	OtrsTicketerPlugin m_ticketer;
	
	Ticket m_ticket;
	

    /**
     * Don't run this test unless the runOtrsTests property
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
        return "runOtrsTests";
    }

	 @Override
	 protected void setUp() throws Exception {

	        System.setProperty("opennms.home", "src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");

	        System.out.println("src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");
	        
	        m_ticketer = new OtrsTicketerPlugin();
	        
	        m_configDao = new DefaultOtrsConfigDao();
	        
	        m_ticket = new Ticket();
	        m_ticket.setState(Ticket.State.OPEN);
	        m_ticket.setSummary("Ticket Summary for ticket: " + new Date());
	        m_ticket.setDetails("First Article for ticket: " + new Date());
			m_ticket.setUser("root@localhost");
			
	}

	public void testGet() {
		
		TicketIDAndNumber idAndNumber = null;
		
		Ticket newTicket = null;

		
		String summary = new String("Ticket created by testGet()");
		String details = new String("This ticketwas created by OtrsTicketerPluginTest");
		
		try {
			idAndNumber = createTicketAndArticle(summary,details);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// now construct a ticket by hand.
		
		Ticket ticket = new Ticket();
		ticket.setId(String.valueOf(idAndNumber.getTicketNumber()));
		ticket.setState(Ticket.State.OPEN);
		ticket.setSummary(summary);
		ticket.setDetails(details);
		ticket.setUser(defaultUser);

        
        try {
            newTicket = m_ticketer.get(ticket.getId());
        } catch (PluginException e) {
            e.printStackTrace();
        }
        
        assertTicketEquals(ticket, newTicket);
		
	}

	public void testSave() {
	    
	    Ticket retrievedTicket = null;
		
		try {
            m_ticketer.saveOrUpdate(m_ticket);
            retrievedTicket = m_ticketer.get(m_ticket.getId());
            
        } catch (PluginException e) {
            e.printStackTrace();
        }
		
		assertTicketEquals(m_ticket, retrievedTicket);
		
	}
	
/*	
 *	This test deliberately removed.
 *	As there is no two way update, there is no need to ensure that 
 *      the OTRS ticket contents and the OpenNMS ticket contents match
 *	after the initial save.
 *
 *	public void testUpdate() {
 *		
 *		String firstArticle = new String("First Article");
 *		String secondArticle = new String("Second Article");
 *		
 *		// save with first article
 *		
 *		m_ticket.setDetails(firstArticle);
 *		
 *		m_ticketer.saveOrUpdate(m_ticket);
 *		
 *		// update with first article
 *		
 *		m_ticket.setDetails(secondArticle);
 *		
 *		m_ticketer.saveOrUpdate(m_ticket);
 *		
 *		// get a clean copy from the ID
 *		
 *		Ticket retrievedTicket = m_ticketer.get(m_ticket.getId());
 *		
 *		// compare the opennms ticket to one retrieved from OTRS
 *		
 *		assertTicketEquals(m_ticket, retrievedTicket);
 *		
 *		// should also have the first article as history
 *		
 *		// ensure that old ticket details still exist somewhere in the OTRS ticket
 *		
 *		if (retrievedTicket.getDetails().indexOf(firstArticle) <= 0 ) {
 *        		fail("could not find " + firstArticle + " in " + retrievedTicket.getDetails());
 *        	}
 *		
 *	}
 */
	
	public void testStateUpdate() throws InterruptedException {
		
		try {
		    
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
        } catch (PluginException e) {
            e.printStackTrace();
        }
		

		
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
			
			Credentials creds = new Credentials(m_configDao.getUserName(),m_configDao.getPassword());

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
			
			TicketServicePort_PortType port = null;
			
			try {
				port = service.getTicketServicePort();
			} catch (ServiceException e1) {
				e1.printStackTrace();
			}
			
			try {
				idAndNumber = port.ticketCreate(otrsTicket, creds);
			} catch (RemoteException e) {
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
				articleId = port.articleCreate(otrsArticle, creds);
				assertNotNull(articleId);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			return idAndNumber;
	 }
	 
	 

}
