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

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.integration.otrs.ticketservice.Article;
import org.opennms.integration.otrs.ticketservice.ArticleCore;
import org.opennms.integration.otrs.ticketservice.Credentials;
import org.opennms.integration.otrs.ticketservice.TicketCore;
import org.opennms.integration.otrs.ticketservice.TicketIDAndNumber;
import org.opennms.integration.otrs.ticketservice.TicketServiceLocator;
import org.opennms.integration.otrs.ticketservice.TicketServicePort_PortType;
import org.opennms.integration.otrs.ticketservice.TicketStateUpdate;
import org.opennms.integration.otrs.ticketservice.TicketWithArticles;

import org.opennms.api.integration.ticketing.*;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for OTRS
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public class OtrsTicketerPlugin implements Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(OtrsTicketerPlugin.class);
    
	private DefaultOtrsConfigDao m_configDao; 
	
	private String m_endpoint; 
	
	/**
	 * <p>Constructor for OtrsTicketerPlugin.</p>
	 */
	public OtrsTicketerPlugin() {
		
		m_configDao = new DefaultOtrsConfigDao();
		m_endpoint = m_configDao.getEndpoint();
		
	}

	/** {@inheritDoc} */
        @Override
	public Ticket get(String ticketId) throws PluginException {

		TicketWithArticles ticketWithArticles = null;

		// don't try to get ticket if it's marked as not available
		
		Ticket opennmsTicket = new Ticket();

		if (ticketId == null)  {
		    
		    LOG.error("No OTRS ticketID available in OpenNMS Ticket");
		    throw new PluginException("No OTRS ticketID available in OpenNMS Ticket");
		    
		} else {
		    
		    TicketServicePort_PortType port = getTicketServicePort(m_endpoint);
	   
		    if (port != null) {
		    
    		    long otrsTicketNumber = Long.parseLong(ticketId.trim());
    
    			Credentials creds = new Credentials();
    
    			creds.setUser(m_configDao.getUserName());
    			creds.setPass(m_configDao.getPassword());
    
    			// get the ticket from OTRS system
    			
    			try {
    				ticketWithArticles = port.getByNumber(otrsTicketNumber, creds);
    			} catch (RemoteException e) {
				LOG.error("Failed to retrieve OTRS ticket", e);
    				throw new PluginException("Failed to retrieve OTRS ticket");
    			}
    			
		    }
			
		}

		// add ticket basics from the OTRS ticket
		
		LOG.debug("Adding Ticket details from OTRS ticket # {}", ticketWithArticles.getTicket().getTicketNumber());
		opennmsTicket.setId(ticketWithArticles.getTicket().getTicketNumber().toString());
		opennmsTicket.setSummary(ticketWithArticles.getTicket().getTitle());
		
		// Note that we user "Owner" from the OTRS ticket here. There is nothing to ensure
		// That this is a valid OpenNMS user
		
		opennmsTicket.setUser(ticketWithArticles.getTicket().getOwner());
		opennmsTicket.setState(otrsToOpenNMSState(ticketWithArticles.getTicket().getStateID()));
		
		LOG.debug("Retrieved ticket state : {}", otrsToOpenNMSState(ticketWithArticles.getTicket().getStateID()));
		
		// add all the article details from the OTRS ticket
		// this is not strictly essential as we have no way of viewing this atm.
		
		String opennmsTicketDetails = "";

		for (Article article : ticketWithArticles.getArticles()) {
			LOG.debug("Adding Article details from OTRS article ID {}", article.getArticleID());
			opennmsTicketDetails = opennmsTicketDetails + "\n"
					+ "From:    " + article.getFrom() + "\n" + "Subject: "
					+ article.getSubject() + "\n" + "Body:\n"
					+ article.getBody() + "\n";
		}
		
		opennmsTicket.setDetails(opennmsTicketDetails);

		return opennmsTicket;

	}


	/** {@inheritDoc} */
        @Override
	public void saveOrUpdate(Ticket newTicket) throws PluginException {
		
		TicketIDAndNumber idAndNumber = null;
		
		TicketServicePort_PortType port = getTicketServicePort(m_endpoint);
		
		Ticket currentTicket = null;
		
		Credentials creds = new Credentials();

		creds.setUser(m_configDao.getUserName());
		creds.setPass(m_configDao.getPassword());
		
		try {
		    
		    // If there's no external ID in the OpenNMS ticket, we need to create one
			
			if ((newTicket.getId() == null) ) {
				
				idAndNumber =  newOTRSTicket(newTicket, port, creds);

				newTicket.setId(String.valueOf(idAndNumber.getTicketNumber()));

				LOG.debug("created new ticket: {}", newTicket.getId());
				
				newOTRSArticle(idAndNumber.getTicketNumber(), newTicket, port, creds);
				
				
			} else {
			    
			    currentTicket = get(newTicket.getId()); 
				
				LOG.debug("updating existing ticket : {}", currentTicket.getId());
				
				if (currentTicket.getState() != newTicket.getState()) {
					
					updateOTRSState(newTicket, port, creds);
					updateOTRSArticle(Long.parseLong(currentTicket.getId()), newTicket, port, creds);
					
				} else {
		
					// There is no else at the moment
					// Tickets are _only_ updated with new state

				}
				
			}
			
		} catch (RemoteException e) {
			LOG.error("Failed to create or update OTRS ticket", e);
			throw new PluginException("Failed to create or update OTRS ticket");
		}
			
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
		LOG.debug("Updating ticket with new state");
		LOG.debug("Ticket ID:     {}", ticket.getId());
		LOG.debug("OpenNMS State: {}", ticket.getState().toString());
		LOG.debug("OTRS state:    {}", otrsStateId.toString());
		
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
		
		LOG.debug("Adding a new article to ticket: {}", otrsTicketNumber);
		
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
		
		LOG.debug("Adding a new article to ticket: {}", otrsTicketNumber);
		
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
                        LOG.debug("No valid OpenNMS state on ticket");
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
		
		LOG.debug("getting otrs state from OpenNMS State {}", state.toString());

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
		LOG.debug("No valid OpenNMS state on ticket");
                otrsStateId =  m_configDao.getOpenStateId();
        }
        
        LOG.debug("OpenNMS state was        {}", state.toString());
        LOG.debug("setting OTRS state ID to {}", otrsStateId.toString());
        
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
		LOG.debug("OTRS state ID {} matched OpenNMS state Open", otrsStateId);
        	openNMSState = Ticket.State.OPEN;
        } else if (m_configDao.getValidClosedStateId().contains(otrsStateId)) {
                LOG.debug("OTRS state ID {} matched OpenNMS state Closed", otrsStateId);
        	openNMSState = Ticket.State.CLOSED;
		} else if (m_configDao.getValidCancelledStateId().contains(otrsStateId)) {
		        LOG.debug("OTRS state ID {} matched OpenNMS state Cancelled", otrsStateId);
			openNMSState = Ticket.State.CANCELLED;
		} else {
		        LOG.debug("OTRS state ID {} has no matching OpenNMS state", otrsStateId);
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
    
    private TicketServicePort_PortType getTicketServicePort(String endpoint) throws PluginException {
        
        TicketServiceLocator service = new TicketServiceLocator();
        
        service.setTicketServicePortEndpointAddress(endpoint);

        TicketServicePort_PortType port = null;

        try {
            port = service.getTicketServicePort();
        } catch (ServiceException e) {
            LOG.error("Failed initialzing OTRS TicketServicePort", e);
            throw new PluginException("Failed initialzing OTRS TicketServicePort");
        }
        
        return port;
    }
    
    /**
     * <p>getEndpoint</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEndpoint() {
        return m_endpoint;
    }

    /**
     * <p>setEndpoint</p>
     *
     * @param endpoint a {@link java.lang.String} object.
     */
    public void setEndpoint(String endpoint) {
        m_endpoint = endpoint;
    }
	
	

}
