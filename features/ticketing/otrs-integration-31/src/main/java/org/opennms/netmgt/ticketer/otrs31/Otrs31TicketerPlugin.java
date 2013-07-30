/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2008-2013 The OpenNMS Group,
 * Inc. OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc. OpenNMS(R)
 * is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ticketer.otrs31;

import java.math.BigInteger;

import javax.xml.ws.BindingProvider;

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.netmgt.ticketer.otrs.common.DefaultOtrsConfigDao;
import org.otrs.ticketconnector.GenericTicketConnector;
import org.otrs.ticketconnector.GenericTicketConnectorInterface;
import org.otrs.ticketconnector.OTRSArticle;
import org.otrs.ticketconnector.OTRSTicketCreateTicket;
import org.otrs.ticketconnector.OTRSTicketGetResponseArticle;
import org.otrs.ticketconnector.OTRSTicketUpdateTicket;
import org.otrs.ticketconnector.TicketCreate;
import org.otrs.ticketconnector.TicketCreateResponse;
import org.otrs.ticketconnector.TicketGet;
import org.otrs.ticketconnector.TicketGetResponse;
import org.otrs.ticketconnector.TicketUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for OTRS 3.1 and above
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public class Otrs31TicketerPlugin implements Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(Otrs31TicketerPlugin.class);

    private DefaultOtrsConfigDao m_configDao;

    private GenericTicketConnectorInterface m_ticketConnector;

    public Otrs31TicketerPlugin() {

        m_configDao = new DefaultOtrsConfigDao();

        GenericTicketConnector service = new GenericTicketConnector();

        m_ticketConnector = service.getGenericTicketConnectorEndPoint();

        BindingProvider bindingProvider = (BindingProvider) m_ticketConnector;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                                m_configDao.getEndpoint());

    }

    /*
     * (non-Javadoc)
     * @see org.opennms.api.integration.ticketing.Plugin#get(java.lang.String)
     */
    public Ticket get(String ticketId) throws PluginException {

        Ticket opennmsTicket = new Ticket();
        TicketGetResponse response;

        if (ticketId == null) {

            LOG.error("No OTRS ticketID available in OpenNMS Ticket");
            throw new PluginException(
                                      "No OTRS ticketID available in OpenNMS Ticket");

        } else {

            if (m_ticketConnector != null) {

                TicketGet request = new TicketGet();
                request.setUserLogin(m_configDao.getUserName());
                request.setPassword(m_configDao.getPassword());

                request.setTicketID(new BigInteger[] { new BigInteger(
                                                                      ticketId) });

                response = m_ticketConnector.ticketGet(request);

                LOG.debug("reponded with " + response.getTicket().length
                        + "tickets");

                // add ticket basics from the OTRS ticket

                opennmsTicket.setId(response.getTicket(0).getTicketID().toString());
                opennmsTicket.setSummary(response.getTicket(0).getTitle());

                // Note that we user "Owner" from the OTRS ticket here. There
                // is nothing to ensure
                // That this is a valid OpenNMS user

                opennmsTicket.setUser(response.getTicket(0).getCustomerUserID());
                opennmsTicket.setState(otrsToOpenNMSState(response.getTicket(0).getStateID()));

                // add all the article details from the OTRS ticket
                // this is not strictly essential as we have no way of viewing
                // this atm.

                String opennmsTicketDetails = "";

                for (OTRSTicketGetResponseArticle article : response.getTicket(0).getArticle()) {
                    LOG.debug("Adding Article details from OTRS article ID {}",
                              article.getArticleID());
                    opennmsTicketDetails = opennmsTicketDetails + "\n"
                            + "From: " + article.getFrom() + "\n"
                            + "Subject: " + article.getSubject() + "\n"
                            + "Body: " + article.getBody() + "\n";
                }

                opennmsTicket.setDetails(opennmsTicketDetails);

                return opennmsTicket;

            } else {

                LOG.error("No Ticket Connector Available to service request to OTRS");
                throw new PluginException(
                                          "No Ticket Connector Available to service request to OTRS");
            }

        }

    }

    /*
     * (non-Javadoc)
     * @see
     * org.opennms.api.integration.ticketing.Plugin#saveOrUpdate(org.opennms
     * .api.integration.ticketing.Ticket)
     */
    public void saveOrUpdate(Ticket newTicket) throws PluginException {

        Ticket currentTicket = null;

        if (newTicket.getId() == null) {

            OTRSTicketCreateTicket otrsTicket = new OTRSTicketCreateTicket();
          
            // TODO Check whether we should use the OpenNMS ticket for this
            // The original OTRS plugin checks this and sets if there is a user
            // in the OpenNMS ticket. Suspect this may just cause pain as the 
            // OpenNMS user is unlikely to be a valid OTRS customer user.
            
            otrsTicket.setCustomerUser(m_configDao.getDefaultUser());
            otrsTicket.setTitle(newTicket.getSummary());
            otrsTicket.setQueue(m_configDao.getQueue());
            otrsTicket.setStateID(openNMSToOTRSState(newTicket.getState()));
            otrsTicket.setPriority(m_configDao.getPriority());

            OTRSArticle otrsArticle = new OTRSArticle();
            
            // TODO Figure out why we can't set ArticleFrom without an error from OTRS
            // otrsArticle.setFrom(m_configDao.getArticleFrom());
            
            otrsArticle.setSubject(newTicket.getSummary());
            otrsArticle.setBody(newTicket.getDetails());
            
            otrsArticle.setArticleType(m_configDao.getArticleType());
            otrsArticle.setSenderType(m_configDao.getArticleSenderType());
            otrsArticle.setContentType(m_configDao.getArticleContentType());
            otrsArticle.setHistoryType(m_configDao.getArticleHistoryType());
            otrsArticle.setHistoryComment(m_configDao.getArticleHistoryComment());

            TicketCreate createRequest = new TicketCreate();
            createRequest.setUserLogin(m_configDao.getUserName());
            createRequest.setPassword(m_configDao.getPassword());
            createRequest.setTicket(otrsTicket);
            createRequest.setArticle(otrsArticle);

            TicketCreateResponse response = m_ticketConnector.ticketCreate(createRequest);

            if (response.getTicketID() == null) {
                throw new PluginException("null ticketID returned by OTRS");
            } else {
                LOG.debug("Created new ticket ID "
                        + response.getTicketID().toString());
                newTicket.setId(response.getTicketID().toString());
            }

        } else {

            currentTicket = get(newTicket.getId());
            LOG.debug("updating existing ticket : {}", currentTicket.getId());

            if (currentTicket.getState() != newTicket.getState()) {

                OTRSTicketUpdateTicket ticketUpdate = new OTRSTicketUpdateTicket();
                

                ticketUpdate.setStateID(openNMSToOTRSState(newTicket.getState()));

                OTRSArticle articleUpdate = new OTRSArticle();

                // TODO Figure out why we can't set ArticleFrom without an error from OTRS
                // otrsArticle.setFrom(m_configDao.getArticleFrom());

                articleUpdate.setSubject(m_configDao.getArticleUpdateSubject());

                // All OTRS article fields from defaults

                articleUpdate.setArticleType(m_configDao.getArticleType());
                articleUpdate.setSenderType(m_configDao.getArticleSenderType());
                articleUpdate.setContentType(m_configDao.getArticleContentType());
                articleUpdate.setHistoryType(m_configDao.getArticleHistoryType());
                articleUpdate.setHistoryComment(m_configDao.getArticleHistoryComment());

                switch (newTicket.getState()) {

                case OPEN:
                    // ticket is new
                    articleUpdate.setBody(m_configDao.getTicketOpenedMessage());
                    break;
                case CANCELLED:
                    // not sure how often we see this
                    articleUpdate.setBody(m_configDao.getTicketCancelledMessage());
                    break;
                case CLOSED:
                    // closed successful
                    articleUpdate.setBody(m_configDao.getTicketClosedMessage());
                    break;
                default:
                    LOG.debug("No valid OpenNMS state on ticket");
                    articleUpdate.setBody(m_configDao.getTicketUpdatedMessage());
                }

                TicketUpdate update = new TicketUpdate();
                update.setUserLogin(m_configDao.getUserName());
                update.setPassword(m_configDao.getPassword());
                update.setTicketID(new BigInteger(currentTicket.getId()));
                update.setTicket(ticketUpdate);
                update.setArticle(articleUpdate);

                m_ticketConnector.ticketUpdate(update);

            } else {

                // There is no else at the moment
                // Tickets are _only_ updated with new state

            }
        }

    }

    /**
     * Convenience method for converting OTRS ticket StateID to OpenNMS
     * enumerated ticket states.
     * 
     * @param otrsStateID
     * @return the converted
     *         <code>org.opennms.netmgt.ticketd.Ticket.State</code>
     */

    private Ticket.State otrsToOpenNMSState(BigInteger otrsStateId) {

        Ticket.State openNMSState;

        if (m_configDao.getValidOpenStateId().contains(otrsStateId.intValue())) {
            LOG.debug("OTRS state ID {} matched OpenNMS state Open",
                      otrsStateId);
            openNMSState = Ticket.State.OPEN;
        } else if (m_configDao.getValidClosedStateId().contains(otrsStateId.intValue())) {
            LOG.debug("OTRS state ID {} matched OpenNMS state Closed",
                      otrsStateId);
            openNMSState = Ticket.State.CLOSED;
        } else if (m_configDao.getValidCancelledStateId().contains(otrsStateId.intValue())) {
            LOG.debug("OTRS state ID {} matched OpenNMS state Cancelled",
                      otrsStateId);
            openNMSState = Ticket.State.CANCELLED;
        } else {
            LOG.debug("OTRS state ID {} has no matching OpenNMS state",
                      otrsStateId);
            // we don't know what state it is, so default to keeping it open.
            openNMSState = Ticket.State.OPEN;
        }

        return openNMSState;

    }

    /**
     * Convenience method for converting OpenNMS enumerated ticket states to
     * OTRS ticket StateID.
     * 
     * @param state
     * @return a BigInteger representing the OTRS StateID.
     */

    private BigInteger openNMSToOTRSState(Ticket.State state) {

        BigInteger otrsStateId;

        LOG.debug("getting otrs state from OpenNMS State {}",
                  state.toString());

        switch (state) {

        case OPEN:
            // ticket is new
            otrsStateId = BigInteger.valueOf(m_configDao.getOpenStateId());
            break;
        case CANCELLED:
            // not sure how often we see this
            otrsStateId = BigInteger.valueOf(m_configDao.getCancelledStateId());
            break;
        case CLOSED:
            // closed successful
            otrsStateId = BigInteger.valueOf(m_configDao.getClosedStateId());
            break;
        default:
            LOG.debug("No valid OpenNMS state on ticket");
            otrsStateId = BigInteger.valueOf(m_configDao.getOpenStateId());
        }

        LOG.debug("OpenNMS state was        {}", state.toString());
        LOG.debug("setting OTRS state ID to {}", otrsStateId.toString());

        return otrsStateId;

    }

}
