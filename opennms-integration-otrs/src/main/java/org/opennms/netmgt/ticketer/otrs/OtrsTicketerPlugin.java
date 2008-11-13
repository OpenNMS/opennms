/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc. All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ticketer.otrs;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.integration.otrs.ticketservice.Article;
import org.opennms.integration.otrs.ticketservice.ArticleCore;
import org.opennms.integration.otrs.ticketservice.Credentials;
import org.opennms.integration.otrs.ticketservice.TicketCore;
import org.opennms.integration.otrs.ticketservice.TicketIDAndNumber;
import org.opennms.integration.otrs.ticketservice.TicketServiceLocator;
import org.opennms.integration.otrs.ticketservice.TicketServicePort_PortType;
import org.opennms.integration.otrs.ticketservice.TicketStateUpdate;
import org.opennms.integration.otrs.ticketservice.TicketWithArticles;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;

import org.opennms.api.integration.ticketing.*;
import org.opennms.netmgt.xml.event.Event;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for OTRS
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * 
 */

public class OtrsTicketerPlugin implements Plugin {
	
	static final String COMMS_ERROR_UEI = "uei.opennms.org/troubleTicket/communicationError";
	
	// TODO: Springify this
	
	EventIpcManager m_eventIpcManager;
	
	private DefaultOtrsConfigDao m_configDao; 
	
	public OtrsTicketerPlugin() {
		
		m_configDao = new DefaultOtrsConfigDao();
		m_eventIpcManager = EventIpcManagerFactory.getIpcManager();
	}

	public Ticket get(String ticketId) {

		TicketWithArticles ticketWithArticles = null;

		long otrsTicketNumber = Long.parseLong(ticketId.trim());

		TicketServicePort_PortType port = getTicketServicePort();

		Ticket opennmsTicket = new Ticket();

		if (port != null) {

			Credentials creds = new Credentials();

			creds.setUser(m_configDao.getUserName());
			creds.setPass(m_configDao.getPassword());

			// get the ticket from OTRS system
			
			try {
				ticketWithArticles = port.getByNumber(otrsTicketNumber, creds);
			} catch (RemoteException e) {
				log().error("Failed to retrieve OTRS ticket" + e);
				m_eventIpcManager.sendNow(createEvent("Failed to retrieve OTRS ticket"));
			}

			
		}
		
		// construct an opennms ticket from the returned OTRS ticket and articles

		if (ticketWithArticles != null) {

			// add ticket basics from the OTRS ticket
			
			log().debug(
					"Adding Ticket details from OTRS ticket # "
							+ ticketWithArticles.getTicket().getTicketNumber());
			opennmsTicket.setId(ticketWithArticles.getTicket().getTicketNumber().toString());
			opennmsTicket.setSummary(ticketWithArticles.getTicket().getTitle());
			
			// Note that we user "Owner" from the OTRS ticket here. There is nothing to ensure
			// That this is a valid OpenNMS user
			
			opennmsTicket.setUser(ticketWithArticles.getTicket().getOwner());
			opennmsTicket.setState(otrsToOpenNMSState(ticketWithArticles.getTicket().getStateID()));
			
			log().debug("Retrieved ticket state : " + otrsToOpenNMSState(ticketWithArticles.getTicket().getStateID()));
			
			// add all the article details from the OTRS ticket
			// this is not strictly essential as we have no way of viewing this atm.
			
			String opennmsTicketDetails = "";

			for (Article article : ticketWithArticles.getArticles()) {
				log().debug(
						"Adding Article details from OTRS article ID "
								+ article.getArticleID());
				opennmsTicketDetails = opennmsTicketDetails + "\n"
						+ "From:    " + article.getFrom() + "\n" + "Subject: "
						+ article.getSubject() + "\n" + "Body:\n"
						+ article.getBody() + "\n";
			}
			
			opennmsTicket.setDetails(opennmsTicketDetails);

		}

		return opennmsTicket;

	}


	public void saveOrUpdate(Ticket newTicket) {
		
		TicketIDAndNumber idAndNumber = null;
		
		TicketServicePort_PortType port = getTicketServicePort();
		
		Ticket currentTicket = null;
		
		Credentials creds = new Credentials();

		creds.setUser(m_configDao.getUserName());
		creds.setPass(m_configDao.getPassword());
		
		if (newTicket.getId() != null) {

			currentTicket = get(newTicket.getId()); 
		
		} 
		
		try {
			
			if (currentTicket == null) {
				
				idAndNumber =  newOTRSTicket(newTicket, port, creds);

				log().debug("creating new ticket : " + idAndNumber.getTicketNumber());
				
				newTicket.setId(String.valueOf(idAndNumber.getTicketNumber()));

				log().debug("Ticket ID is " + newTicket.getId());
				
				newOTRSArticle(idAndNumber.getTicketNumber(), newTicket, port, creds);
				
				
			} else {
				
				log().debug("updating existing ticket : " + currentTicket.getId());
				
				if (currentTicket.getState() != newTicket.getState()) {
					
					updateOTRSState(newTicket, port, creds);
					updateOTRSArticle(Long.parseLong(currentTicket.getId()), newTicket, port, creds);
					
				} else {
		
					// There is no else at the moment
					// Tickets are _only_ updated with new state

				}
				
			}
			
		} catch (RemoteException e) {
			log().error("Failed to create or update OTRS ticket" + e);
			createEvent("Failed to create or update OTRS ticket");
		}
		
		
	}

	private Event createEvent(String reason) {
		EventBuilder bldr = new EventBuilder(COMMS_ERROR_UEI, "Ticketd");
		bldr.addParam("reason", reason);
		return bldr.getEvent();
	}

	private void updateOTRSState(Ticket ticket, TicketServicePort_PortType port,
			Credentials creds) throws RemoteException {
		
		Integer otrsStateId = openNMSToOTRSState(ticket.getState());
		
		TicketStateUpdate stateUpdate = new TicketStateUpdate();
		
		stateUpdate.setStateID(otrsStateId);
		stateUpdate.setTicketNumber(Long.parseLong(ticket.getId()));
		if (ticket.getUser() != null) {
			stateUpdate.setUser(ticket.getUser());
		} else {
			stateUpdate.setUser(m_configDao.getDefaultUser());
		}
		log().debug("Updating ticket with new state");
		log().debug("Ticket ID:     " + ticket.getId());
		log().debug("OpenNMS State: " + ticket.getState().toString());
		log().debug("OTRS state:    " + otrsStateId.toString());
		
		port.ticketStateUpdate(stateUpdate, creds);
	}
	
	private TicketIDAndNumber newOTRSTicket(Ticket newTicket, TicketServicePort_PortType port,
			Credentials creds) throws RemoteException {
		
		TicketIDAndNumber idAndNumber = null;
		
		TicketCore newOtrsTicket = new TicketCore();
		
		newOtrsTicket.setTitle(newTicket.getSummary());
		
		// TODO: Could remove this once we have the userid reliably in the the ticket
		
		if (newTicket.getUser() != null) {
			newOtrsTicket.setUser(newTicket.getUser());
		} else {
			newOtrsTicket.setUser(m_configDao.getDefaultUser());
		}
		
		newOtrsTicket.setStateID(openNMSToOTRSState(newTicket.getState()));
		
		// All OTRS ticket fields from defaults
		
		newOtrsTicket.setQueue(m_configDao.getQueue());
		newOtrsTicket.setPriority(m_configDao.getPriority());
		newOtrsTicket.setLock(m_configDao.getLock());
		newOtrsTicket.setOwnerID(m_configDao.getOwnerID());
		
		idAndNumber = port.ticketCreate(newOtrsTicket, creds);
				
		return idAndNumber;
		
	}
	
	
	
	private void newOTRSArticle(Long otrsTicketNumber, Ticket newTicket, TicketServicePort_PortType port,
			Credentials creds) throws RemoteException {
		
		ArticleCore newOtrsArticle = new ArticleCore();
		
		// All OTRS article fields from ticket
		
		log().debug("Adding a new article to ticket: " + otrsTicketNumber);
		
		newOtrsArticle.setBody(newTicket.getDetails());
		newOtrsArticle.setTicketNumber(otrsTicketNumber);
		
		// TODO: Could remove this once we have the userid reliably in the the ticket
		
		newOtrsArticle.setFrom(m_configDao.getArticleFrom());
		
		if (newTicket.getUser() != null) {
			newOtrsArticle.setUser(newTicket.getUser());
		} else {
			newOtrsArticle.setUser(m_configDao.getDefaultUser());
		}
		
		newOtrsArticle.setSubject(newTicket.getSummary());
		
		// All OTRS article fields from defaults
			
		newOtrsArticle.setArticleType(m_configDao.getArticleType());
		newOtrsArticle.setSenderType(m_configDao.getArticleSenderType());
		newOtrsArticle.setContentType(m_configDao.getArticleContentType());
		newOtrsArticle.setHistoryType(m_configDao.getArticleHistoryType());
		newOtrsArticle.setHistoryComment(m_configDao.getArticleHistoryComment());

		port.articleCreate(newOtrsArticle, creds);
		
	}

	private void updateOTRSArticle(Long otrsTicketNumber, Ticket newTicket, TicketServicePort_PortType port,
			Credentials creds) throws RemoteException {
		
		ArticleCore newOtrsArticle = new ArticleCore();

		// All OTRS article fields from ticket
		
		log().debug("Adding a new article to ticket: " + otrsTicketNumber);
		
		switch (newTicket.getState()) {

            	    case OPEN:
                        // ticket is new
			newOtrsArticle.setBody(m_configDao.getTicketOpenedMessage());
                        break;
                    case CANCELLED:
                        // not sure how often we see this
                        newOtrsArticle.setBody(m_configDao.getTicketCancelledMessage());
                        break;
                    case CLOSED:
                        // closed successful
                        newOtrsArticle.setBody(m_configDao.getTicketClosedMessage());
                        break;
                    default:
                        log().debug("No valid OpenNMS state on ticket");
                        newOtrsArticle.setBody(m_configDao.getTicketUpdatedMessage());
        	}

		newOtrsArticle.setTicketNumber(otrsTicketNumber);
		
		// TODO: Could remove this once we have the userid reliably in the the ticket
		
		newOtrsArticle.setFrom(m_configDao.getArticleFrom());
		
		if (newTicket.getUser() != null) {
			newOtrsArticle.setUser(newTicket.getUser());
		} else {
			newOtrsArticle.setUser(m_configDao.getDefaultUser());
		}
		
		newOtrsArticle.setSubject(m_configDao.getArticleUpdateSubject());
		
		// All OTRS article fields from defaults
			
		newOtrsArticle.setArticleType(m_configDao.getArticleType());
		newOtrsArticle.setSenderType(m_configDao.getArticleSenderType());
		newOtrsArticle.setContentType(m_configDao.getArticleContentType());
		newOtrsArticle.setHistoryType(m_configDao.getArticleHistoryType());
		newOtrsArticle.setHistoryComment(m_configDao.getArticleHistoryComment());

		port.articleCreate(newOtrsArticle, creds);
		
	}

	/**
     * Convenience method for converting OpenNMS enumerated ticket states to
     * OTRS ticket StateID.
     * 
     * TODO: Convert this to something parameterised
     *
     * @param state
     * @return an Integer representing the OTRS StateID.
     */
	
	private Integer openNMSToOTRSState(Ticket.State state) {

		Integer otrsStateId;
		
		log().debug("getting otrs state from OpenNMS State " + state.toString());

        switch (state) {
        
            case OPEN:
            	// ticket is new
            	otrsStateId = m_configDao.getOpenStateId();
            	break;
            case CANCELLED:
            	// not sure how often we see this
            	otrsStateId = m_configDao.getCancelledStateId();
            	break;
            case CLOSED:
                // closed successful
                otrsStateId = m_configDao.getClosedStateId();
                break;
            default:
            	log().debug("No valid OpenNMS state on ticket");
                otrsStateId =  m_configDao.getOpenStateId();
        }
        
        log().debug("OpenNMS state was        " + state.toString());
        log().debug("setting OTRS state ID to " + otrsStateId.toString());
        
        return otrsStateId;
    }

    /**
     * Convenience method for converting OTRS ticket StateID to 
     * OpenNMS enumerated ticket states.
     * 
     * @param otrsStateID
     * @return the converted <code>org.opennms.netmgt.ticketd.Ticket.State</code>
     */
	
    private Ticket.State otrsToOpenNMSState(Integer otrsStateId ) {
    	
    	Ticket.State openNMSState;
    	
        if (m_configDao.getValidOpenStateId().contains(otrsStateId)) {
        	log().debug("OTRS state ID " + otrsStateId.toString() + " matched OpenNMS state Open");
        	openNMSState = Ticket.State.OPEN;
        } else if (m_configDao.getValidClosedStateId().contains(otrsStateId)) {
        	log().debug("OTRS state ID " + otrsStateId.toString() + " matched OpenNMS state Closed");
        	openNMSState = Ticket.State.CLOSED;
		} else if (m_configDao.getValidCancelledStateId().contains(otrsStateId)) {
			log().debug("OTRS state ID " + otrsStateId.toString() + " matched OpenNMS state Cancelled");
			openNMSState = Ticket.State.CANCELLED;
		} else {
			log().debug("OTRS state ID " + otrsStateId.toString() + " has no matching OpenNMS state");
			// we dont know what it is, so default to keeping it open.
			openNMSState = Ticket.State.OPEN;
		}
        
        return openNMSState;
        
    }
    
    /**
     * Convenience method for initialising the ticketServicePort and correctly setting the endpoint.
     *
     * @return TicketServicePort to connect to the remote service.
     */
    
    private TicketServicePort_PortType getTicketServicePort() {
        
        TicketServiceLocator service = new TicketServiceLocator();
        
        service.setTicketServicePortEndpointAddress(m_configDao.getEndpoint());

        TicketServicePort_PortType port = null;

        try {
            port = service.getTicketServicePort();
        } catch (ServiceException e) {
            log().error("Failed initialzing OTRS TicketServicePort" + e);
            m_eventIpcManager.sendNow(createEvent("Failed initialzing OTRS TicketServicePort"));
        }
        
        return port;
    }
    
    /**
	 * Covenience logging.
	 * 
	 * @return a log4j Category for this class
	 */
	Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	public EventIpcManager getEventIpcManager() {
		return m_eventIpcManager;
	}

	public void setEventIpcManager(EventIpcManager ipcManager) {
		m_eventIpcManager = ipcManager;
	}
	
	

}
