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
package org.opennms.netmgt.ticketer.rt;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.core.utils.ThreadCategory;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for RT
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * 
 */

public class RtTicketerPlugin implements Plugin {
    
	private DefaultRtConfigDao m_configDao; 
	private String m_user;
	private String m_password;
	
	private static final String TAG_REGEX = "<[^>]*>"; 
	
	public RtTicketerPlugin() {
		
		m_configDao = new DefaultRtConfigDao();
		m_user = m_configDao.getUserName();
		m_password = m_configDao.getPassword();
		
	}
    
    /**
     * Gets ticket details from the RT trouble ticket system
     * 
     * @param   ticketId    a string representation of the RT ticketID
     * @return  ticket      the ticket details
     */

	public Ticket get(String ticketId) throws PluginException {

		boolean ticketFound = false;
		
		Ticket ticket = null;
		
		HashMap<String,String> ticketAttributes = new HashMap<String,String>();
		// don't try to get ticket if it's marked as not available
		
		if (ticketId == null)  {
		    
		    log().error("No RT ticketID available in OpenNMS Ticket");
		    throw new PluginException("No RT ticketID available in OpenNMS Ticket");
		    
		} else {
		    
		    PostMethod post = new PostMethod(m_configDao.getBaseURL() + "/REST/1.0/ticket/" + ticketId);
		    
		    NameValuePair[] ticketGetParams = {
	                  new NameValuePair("user", m_user),
	                  new NameValuePair("pass", m_password)
	                };
		    
	        post.setRequestBody(ticketGetParams);
		        
		    try {
                if(getClient().executeMethod(post) != HttpStatus.SC_OK) {
                    throw new PluginException("Received a non 200 response code from the server");
                } else {
                    String in = post.getResponseBodyAsString();
                    log().debug(in);
                    Pattern inTokensPattern = Pattern.compile("^(\\w+):\\s(.*)$", Pattern.MULTILINE);
                    Matcher matcher = inTokensPattern.matcher(in);
                    while (matcher.find()) {
                        ticketFound = true;
                        ticketAttributes.put(matcher.group(1), matcher.group(2));
                    } 
                } 
            } catch (HttpException e) {
                log().error("HTTP exception attempting to logon to RT: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                log().error("HTTP exception attempting to logon to RT: " + e.getMessage());
            } finally {
                post.releaseConnection();
            }
			
		}
		
		if (ticketFound) {
		    ticket = new Ticket();
    		ticket.setState(rtToOpenNMSState(ticketAttributes.get("Status")));
    		ticket.setId(ticketId);
    		ticket.setUser(ticketAttributes.get("Requestors"));
    		ticket.setSummary(ticketAttributes.get("Subject"));
		} else {
		    throw new PluginException("could not find ticket in RT for Ticket: " + ticketId);
		}
		
		return ticket;

	}


	/**
    * Creates a new ticket (if none exists) or updates an existing ticket in the
    * RT trouble ticket system. Ticket updates are currently limited to updating
    * the ticket status only.
    * 
    * @param   ticket      the ticket details
    */
	
	public void saveOrUpdate(Ticket newTicket) throws PluginException {
		
		String newTicketID;
		
		Ticket currentTicket = null;
		
		try {
		    
		    // If there's no external ID in the OpenNMS ticket, we need to create one
			
			if ((newTicket.getId() == null) ) {
			    
			    log().debug("TicketId is null creating a new ticket");
				
			    newTicketID =  newRtTicket(newTicket);

				newTicket.setId(newTicketID);

				log().debug("created new ticket: " + newTicket.getId());
				
				
			} else {
			    
			    currentTicket = get(newTicket.getId()); 
				
				log().debug("updating existing ticket : " + currentTicket.getId());
				
				if (currentTicket.getState() != newTicket.getState()) {
					
					updateRtStatus(newTicket);
					
				} else {
		
					// There is no else at the moment
					// Tickets are _only_ updated with new state

				}
				
			}
			
		} catch (PluginException e) {
			log().error("Failed to create or update RT ticket" + e);
			throw new PluginException("Failed to create or update RT ticket");
		}
			
	}

	/**
	* Convenience method for updating the Ticket Status in RT
	* 
	* @param   ticket      the ticket details
	*/
	
	private void updateRtStatus(Ticket ticket) throws PluginException {
		
		PostMethod post = new PostMethod(m_configDao.getBaseURL() + "/REST/1.0/ticket/" + ticket.getId() + "/edit");
		
		String updateString = new String("Status: " + openNMSToRTState(ticket.getState()));
		
		NameValuePair[] statusUpdateParams = {
                new NameValuePair("content", updateString),
                new NameValuePair("user", m_user),
                new NameValuePair("pass", m_password)
              };
		try {
	        post.setRequestBody(statusUpdateParams);
	        if(getClient().executeMethod(post) != HttpStatus.SC_OK) {
	            throw new PluginException("Received a non 200 response code from the server");
	        } else {
                String in = post.getResponseBodyAsString();
                Pattern okPattern = Pattern.compile("(?s) Ticket (\\d+) updated");
                Matcher matcher = okPattern.matcher(in);
                if (! matcher.find()) {
                    throw new PluginException("Did not receive confirmation from RT that ticket was updated");
                }
	        }
        } catch (HttpException e) {
            log().error("HTTP exception attempting to logon to RT: " + e.getMessage());
            throw new PluginException(e.getMessage());
        } catch (IOException e) {
            log().error("HTTP exception attempting to logon to RT: " + e.getMessage());
            throw new PluginException(e.getMessage());
        } finally {
            post.releaseConnection();
        }

		
	}
	
	/**
	* Convenience method for creating a new ticket in RT
	* 
	* @param   ticket      the ticket details
	* @return  the RT ticket id for the new ticket
	*/
	
	private String newRtTicket(Ticket newTicket) throws PluginException {
		
		String rtTicketNumber = null;
		
		// Remove any HTML tags in the ticket details.
		
		Pattern tagPattern = Pattern.compile(TAG_REGEX);
		Matcher tagMatcher = tagPattern.matcher(newTicket.getDetails());
		String rtTicketText = tagMatcher.replaceAll("");
		
		StringBuilder contentBuilder = new StringBuilder("id: ticket/new\n");
		contentBuilder.append("Queue: " + m_configDao.getQueue() + "\n");
		contentBuilder.append("Requestor: " + m_configDao.getRequestor() + "\n");
		contentBuilder.append("Subject: " + newTicket.getSummary() + "\n");
		contentBuilder.append("text: " + rtTicketText + "\n");
		
		PostMethod post = new PostMethod(m_configDao.getBaseURL() + "/REST/1.0/edit");

		NameValuePair[] ticketCreateParams = {
		          new NameValuePair("content", contentBuilder.toString()),
		          new NameValuePair("user", m_user),
		          new NameValuePair("pass", m_password)
		        };
		    
	    post.setRequestBody(ticketCreateParams);
	    
        try {
            if(getClient().executeMethod(post) != HttpStatus.SC_OK) {
                throw new PluginException("Received a non 200 response code from the server");
            } else {
                String in = post.getResponseBodyAsString();
                log().debug(in);
                Pattern okPattern = Pattern.compile("(?s) Ticket (\\d+) created");
                Matcher matcher = okPattern.matcher(in);
                if (matcher.find()) {
                    rtTicketNumber = matcher.group(1);
                } else {
                    throw new PluginException("Did not receive confirmation that ticket was updated");
                }
            } 
        } catch (HttpException e) {
            log().error("HTTP exception attempting to logon to RT: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log().error("IO exception attempting to logon to RT: " + e.getMessage());
        } finally {
            post.releaseConnection();
        }
		
		if (rtTicketNumber == null) {
		    throw new PluginException("Received no ticket number from RT");
		} 
		
		return rtTicketNumber;
		
	}

	/**
     * Convenience method for converting OpenNMS enumerated ticket states to
     * RT status.
     *
     * @param   state   a valid <code>org.opennms.netmgt.ticketd.Ticket.State</code>.
     * @return a String representing the RT Status of the ticket.
     */
	
	private String openNMSToRTState(Ticket.State state) {

		String rtStatus;
		
		log().debug("getting RT status from OpenNMS State " + state.toString());

        switch (state) {
        
            case OPEN:
            	// ticket is new
            	rtStatus = m_configDao.getOpenStatus();
            	log().debug("OpenNMS Status OPEN matched rt status " + rtStatus);
            	break;
            case CANCELLED:
            	// not sure how often we see this
            	rtStatus = m_configDao.getCancelledStatus();
            	log().debug("OpenNMS Status CANCELLED matched rt status " + rtStatus);
            	break;
            case CLOSED:
                // closed successful
                rtStatus = m_configDao.getClosedStatus();
                log().debug("OpenNMS Status CLOSED matched rt status " + rtStatus);
                break;
            default:
            	log().debug("No valid OpenNMS state on ticket");
                rtStatus =  m_configDao.getOpenStatus();
        }
        
        log().debug("OpenNMS state was        " + state.toString());
        log().debug("setting RT status to " + rtStatus);
        
        return rtStatus;
    }

    /**
     * Convenience method for converting RT ticket Status to 
     * OpenNMS enumerated ticket states.
     * 
     * @param rtStatus  a vaild RT status string
     * @return the converted <code>org.opennms.netmgt.ticketd.Ticket.State</code>
     */
	
    private Ticket.State rtToOpenNMSState(String rtStatus ) {
    	
    	Ticket.State openNMSState;
    	
        if (m_configDao.getValidOpenStatus().contains(rtStatus)) {
        	log().debug("RT status " + rtStatus + " matched OpenNMS state Open");
        	openNMSState = Ticket.State.OPEN;
        } else if (m_configDao.getValidClosedStatus().contains(rtStatus)) {
        	log().debug("RT status " + rtStatus + " matched OpenNMS state Closed");
        	openNMSState = Ticket.State.CLOSED;
		} else if (m_configDao.getValidCancelledStatus().contains(rtStatus)) {
			log().debug("RT status " + rtStatus + " matched OpenNMS state Cancelled");
			openNMSState = Ticket.State.CANCELLED;
		} else {
			log().debug("RT status " + rtStatus + " has no matching OpenNMS state");
			// we don't know what it is, so default to keeping it open.
			openNMSState = Ticket.State.OPEN;
		}
        
        return openNMSState;
        
    }
    
    
    /**
	 * Covenience logging.
	 * 
	 * @return a log4j Category for this class
	 */
	ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}
	
	
	private HttpClient getClient() {
	    
	    HttpClient client = new HttpClient();
	    
	    HttpClientParams clientParams = new HttpClientParams();
        clientParams.setConnectionManagerTimeout(m_configDao.getTimeout());
        clientParams.setSoTimeout(m_configDao.getTimeout());
        clientParams.setParameter(HttpMethodParams.RETRY_HANDLER,
                                  new DefaultHttpMethodRetryHandler(m_configDao.getRetry(), false));
        clientParams.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
	    
	    client.setParams(clientParams);
	    
	    return client;
	    
	}
	
    public void setUser(String user) {
        m_user = user;
    }


    public void setPassword(String password) {
        m_password = password;
    }
	
	

}
