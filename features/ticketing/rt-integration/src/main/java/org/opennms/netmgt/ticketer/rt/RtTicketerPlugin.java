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

package org.opennms.netmgt.ticketer.rt;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.rt.ReadOnlyRtConfigDao;
import org.opennms.netmgt.rt.RTTicket;
import org.opennms.netmgt.rt.RequestTracker;
import org.opennms.netmgt.rt.RequestTrackerException;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for RT
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class RtTicketerPlugin implements Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(RtTicketerPlugin.class);
    private static Pattern m_tagPattern = Pattern.compile("<[^>]*>");
    
    private RequestTracker m_requestTracker;

    private String m_queue;
    private String m_requestor;

    private String m_openStatus;
    private String m_closedStatus;
    private String m_cancelledStatus;
    private List<Integer> m_validOpenStatus;
    private List<String> m_validClosedStatus;
    private List<String> m_validCancelledStatus;

	/**
	 * <p>Constructor for RtTicketerPlugin.</p>
	 */
	public RtTicketerPlugin() {
	    ReadOnlyRtConfigDao dao = new ReadOnlyRtConfigDao();
	    m_openStatus = dao.getOpenStatus();
	    m_closedStatus = dao.getClosedStatus();
        m_cancelledStatus = dao.getCancelledStatus();
	    m_validOpenStatus = dao.getValidOpenStatus();
	    m_validClosedStatus = dao.getValidClosedStatus();
	    m_validCancelledStatus = dao.getValidCancelledStatus();
	    
	    m_queue = dao.getQueue();
	    m_requestor = dao.getRequestor();

	    m_requestTracker = new RequestTracker(dao.getBaseURL(), dao.getUsername(), dao.getPassword(), dao.getTimeout(), dao.getRetry());
	}
    
	/**
	 * {@inheritDoc}
	 *
	 * Gets ticket details from the RT trouble ticket system
	 */
    @Override
	public Ticket get(final String ticketId) throws PluginException {

		Ticket ticket = null;
		RTTicket rtt = null;
        try {
            rtt = m_requestTracker.getTicket(Long.valueOf(ticketId), false);
        } catch (final RequestTrackerException e) {
            throw new PluginException(e);
        }
		
		if (rtt != null) {
		    ticket = new Ticket();
    		ticket.setState(rtToOpenNMSState(rtt.getStatus()));
    		ticket.setId(rtt.getId().toString());
    		ticket.setUser(StringUtils.join(rtt.getRequestors(), ", "));
    		ticket.setSummary(rtt.getSubject());
    		ticket.setDetails(rtt.getText());
		} else {
		    throw new PluginException("could not find ticket in RT for Ticket: " + ticketId);
		}
		
		return ticket;

	}

	/**
	 * {@inheritDoc}
	 *
	 * Creates a new ticket (if none exists) or updates an existing ticket in the
	 * RT trouble ticket system. Ticket updates are currently limited to updating
	 * the ticket status only.
	 */
    @Override
	public void saveOrUpdate(final Ticket newTicket) throws PluginException {
		
		String newTicketID;
		
		Ticket currentTicket = null;
		
		try {
		    
		    // If there's no external ID in the OpenNMS ticket, we need to create one
			
			if ((newTicket.getId() == null) ) {
			    
			    LOG.debug("TicketId is null creating a new ticket");
                RTTicket ticket = rtTicketFromTicket(newTicket);
                
                Long rtTicketNumber = null;
                try {
                    rtTicketNumber = m_requestTracker.createTicket(ticket);
                } catch (final Exception e) {
                    throw new PluginException(e);
                }
                
                if (rtTicketNumber == null) {
                    throw new PluginException("Received no ticket number from RT");
                }
				
			    newTicketID = rtTicketNumber.toString();
				newTicket.setId(newTicketID);

				LOG.debug("created new ticket: {}", newTicket.getId());
				
				
			} else {
			    
			    currentTicket = get(newTicket.getId()); 
				LOG.debug("updating existing ticket: {}", currentTicket.getId());
				
				if (currentTicket.getState() != newTicket.getState()) {
					updateRtStatus(newTicket);
				} else {
					// There is no else at the moment
					// Tickets are _only_ updated with new state
				}
				
			}
			
		} catch (final PluginException e) {
			LOG.error("Failed to create or update RT ticket", e);
			throw e;
		}
			
	}

	/**
	* Convenience method for updating the Ticket Status in RT
	* 
	* @param ticket the ticket details
	*/
	
	private void updateRtStatus(final Ticket ticket) throws PluginException {

        try {
            m_requestTracker.updateTicket(Long.valueOf(ticket.getId()), "Status: " + openNMSToRTState(ticket.getState()));
        } catch (final Exception e) {
            LOG.warn("Error updating ticket {} to state {}", ticket.getId(), ticket.getState(), e);
        }
	}

	private RTTicket rtTicketFromTicket(final Ticket ticket) {
	    final RTTicket rtt = new RTTicket();

	    final String id = ticket.getId();
	    if (id != null && id.length() > 0) {
	        rtt.setId(Long.valueOf(id));
	    }
	    rtt.setQueue(m_queue);
	    rtt.setRequestor(m_requestor);
	    if (ticket.getSummary() != null) rtt.setSubject(ticket.getSummary());
        // Remove any HTML tags in the ticket details.
	    if (ticket.getDetails() != null) rtt.setText(m_tagPattern.matcher(ticket.getDetails()).replaceAll(""));
	    rtt.setStatus(openNMSToRTState(ticket.getState()));

	    return rtt;
	}

	/**
     * Convenience method for converting OpenNMS enumerated ticket states to
     * RT status.
     *
     * @param   state   a valid <code>org.opennms.netmgt.ticketd.Ticket.State</code>.
     * @return a String representing the RT Status of the ticket.
     */
	
	private String openNMSToRTState(final Ticket.State state) {

		String rtStatus;
		
		LOG.debug("getting RT status from OpenNMS State {}", state);

        switch (state) {
        
            case OPEN:
            	// ticket is new
            	rtStatus = m_openStatus;
            	LOG.debug("OpenNMS Status OPEN matched rt status {}", rtStatus);
            	break;
            case CLOSED:
                // closed successful
                rtStatus = m_closedStatus;
                LOG.debug("OpenNMS Status CLOSED matched rt status {}", rtStatus);
                break;
            case CANCELLED:
            	// not sure how often we see this
            	rtStatus = m_cancelledStatus;
            	LOG.debug("OpenNMS Status CANCELLED matched rt status {}", rtStatus);
            	break;
            default:
                LOG.debug("No valid OpenNMS state on ticket");
                rtStatus = m_openStatus;
        }
        
        LOG.debug("OpenNMS state was {}, setting RT status to {}", state, rtStatus);
        
        return rtStatus;
    }

    /**
     * Convenience method for converting RT ticket Status to 
     * OpenNMS enumerated ticket states.
     * 
     * @param rtStatus a valid RT status string
     * @return the converted <code>org.opennms.netmgt.ticketd.Ticket.State</code>
     */
	
    private Ticket.State rtToOpenNMSState(final String rtStatus) {
    	
        if (m_validOpenStatus.contains(rtStatus)) {
        	LOG.debug("RT status {} matched OpenNMS state Open", rtStatus);
        	return Ticket.State.OPEN;
        } else if (m_validClosedStatus.contains(rtStatus)) {
            LOG.debug("RT status {} matched OpenNMS state Closed", rtStatus);
            return Ticket.State.CLOSED;
		} else if (m_validCancelledStatus.contains(rtStatus)) {
            LOG.debug("RT status {} matched OpenNMS state Cancelled", rtStatus);
            return Ticket.State.CANCELLED;
		}
        
        // we don't know what it is, so default to keeping it open.
        return Ticket.State.OPEN;
        
    }
    
    /**
     * <p>setUser</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public void setUser(final String user) {
        m_requestTracker.setUser(user);
    }


    /**
     * <p>setPassword</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPassword(final String password) {
        m_requestTracker.setPassword(password);
    }
	
	

}
