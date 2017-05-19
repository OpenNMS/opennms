/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ticketer.otrs31;

import java.math.BigInteger;
import java.util.Objects;

import javax.xml.ws.BindingProvider;

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.netmgt.ticketer.otrs.common.DefaultOtrsConfigDao;
import org.otrs.ticketconnector.GenericTicketConnector;
import org.otrs.ticketconnector.GenericTicketConnectorPortType;
import org.otrs.ticketconnector.OTRSArticle;
import org.otrs.ticketconnector.TicketCreate;
import org.otrs.ticketconnector.TicketCreateResponse;
import org.otrs.ticketconnector.OTRSTicketCreateTicket;
import org.otrs.ticketconnector.TicketGet;
import org.otrs.ticketconnector.TicketGetResponse;
import org.otrs.ticketconnector.OTRSTicketGetResponseArticle;
import org.otrs.ticketconnector.OTRSTicketGetResponseTicket;
import org.otrs.ticketconnector.TicketUpdate;
import org.otrs.ticketconnector.OTRSTicketUpdateTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for OTRS 3.1 and above.
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public class Otrs31TicketerPlugin implements Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(Otrs31TicketerPlugin.class);

    private final DefaultOtrsConfigDao m_configDao;

    private final GenericTicketConnectorPortType m_ticketConnector;

    public Otrs31TicketerPlugin() {
        m_configDao = new DefaultOtrsConfigDao();
        m_ticketConnector = new GenericTicketConnector().getGenericTicketConnectorPort();

        BindingProvider bindingProvider = (BindingProvider) m_ticketConnector;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, m_configDao.getEndpoint());
        LOG.info("Binding {} to value {}", BindingProvider.ENDPOINT_ADDRESS_PROPERTY, m_configDao.getEndpoint());
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.api.integration.ticketing.Plugin#get(java.lang.String)
     */
    @Override
    public Ticket get(String ticketId) throws PluginException {
        Objects.requireNonNull(ticketId, "Please provide a ticketId");
        Objects.requireNonNull(m_ticketConnector, "The GenericTicketConnector was not initialized properly");

        TicketGet ticketGet = new TicketGet();
        ticketGet.setUserLogin(m_configDao.getUserName());
        ticketGet.setPassword(m_configDao.getPassword());
        ticketGet.setTicketID(new BigInteger[] { new BigInteger(ticketId) });

        TicketGetResponse response = m_ticketConnector.ticketGet(ticketGet);
        LOG.debug("TicketGet responded with {} tickets" + response.getTicketLength());

        if (response.getTicketLength() == 0) {
            // TODO error handling in this case
        }

        if (response.getTicketLength() > 1) {
            LOG.warn("Received more than 1 tickets, ignore all except the first one.");
        }

        final OTRSTicketGetResponseTicket otrsTicket = response.getTicket(0);

        Ticket opennmsTicket = new Ticket();
        // add ticket basics from the OTRS ticket
        opennmsTicket.setId(otrsTicket.getTicketID().toString());
        opennmsTicket.setSummary(otrsTicket.getTitle());

        // Note that we user "Owner" from the OTRS ticket here. There
        // is nothing to ensure
        // That this is a valid OpenNMS user
        opennmsTicket.setUser(otrsTicket.getCustomerUserID());
        opennmsTicket.setState(otrsToOpenNMSState(otrsTicket.getStateID()));

        // add all the article details from the OTRS ticket
        // this is not strictly essential as we have no way of viewing
        // this atm.

        String opennmsTicketDetails = "";
        for (OTRSTicketGetResponseArticle article : otrsTicket.getArticle()) {
            LOG.debug("Adding Article details from OTRS article ID {}", article.getArticleID());
            opennmsTicketDetails = new StringBuilder().append(opennmsTicketDetails).append("\n")
                    .append("From: ").append(article.getFrom()).append("\n")
                    .append("Subject: ").append(article.getSubject()).append("\n")
                    .append("Body: ").append(article.getBody()).append("\n").toString();
        }

        opennmsTicket.setDetails(opennmsTicketDetails);

        return opennmsTicket;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.opennms.api.integration.ticketing.Plugin#saveOrUpdate(org.opennms
     * .api.integration.ticketing.Ticket)
     */
    @Override
    public void saveOrUpdate(Ticket ticketToUpdateOrCreate) throws PluginException {
        Objects.requireNonNull(ticketToUpdateOrCreate, "The provided ticket must not be null");

        if (ticketToUpdateOrCreate.getId() == null) {
           create(ticketToUpdateOrCreate);
        } else {
            update(ticketToUpdateOrCreate);
        }

    }

    private void create(Ticket newTicket) throws Otrs31PluginException {
        final String summary = newTicket.getSummary().replaceAll("\\<.*?\\>", "");

        // TODO Check whether we should use the OpenNMS ticket for this
        // The original OTRS plugin checks this and sets if there is a user
        // in the OpenNMS ticket. Suspect this may just cause pain as the
        // OpenNMS user is unlikely to be a valid OTRS customer user.

        final OTRSTicketCreateTicket otrsTicket = new OTRSTicketCreateTicket();
        otrsTicket.setCustomerUser(m_configDao.getDefaultUser());
        otrsTicket.setTitle(summary);
        otrsTicket.setQueue(m_configDao.getQueue());
        otrsTicket.setStateID(openNMSToOTRSState(newTicket.getState()));
        otrsTicket.setPriority(m_configDao.getPriority());
        otrsTicket.setType(m_configDao.getType());

        OTRSArticle otrsArticle = new OTRSArticle();
        // TODO Figure out why we can't set ArticleFrom without an error from OTRS
        // otrsArticle.setFrom(m_configDao.getArticleFrom());
        otrsArticle.setSubject(summary);
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
        if (response.getError() != null) {
            throw new Otrs31PluginException(response.getError());
        }
        LOG.debug("Created new ticket with ID {}", response.getTicketID().toString());
        newTicket.setId(response.getTicketID().toString());
    }

    private void update(Ticket ticketToUpdate) throws PluginException {
        Ticket currentTicket = get(ticketToUpdate.getId());
        LOG.debug("updating existing ticket : {}", currentTicket.getId());

        if (currentTicket.getState() != ticketToUpdate.getState()) {

            OTRSTicketUpdateTicket ticketUpdate = new OTRSTicketUpdateTicket();


            ticketUpdate.setStateID(openNMSToOTRSState(ticketToUpdate.getState()));

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

            switch (ticketToUpdate.getState()) {

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

    /**
     * Convenience method for converting OTRS ticket StateID to OpenNMS
     * enumerated ticket states.
     * 
     * @param otrsStateId
     * @return the converted
     *         <code>org.opennms.netmgt.ticketd.Ticket.State</code>
     */

    private Ticket.State otrsToOpenNMSState(BigInteger otrsStateId) {

        Ticket.State openNMSState;

        if (m_configDao.getValidOpenStateId().contains(otrsStateId.intValue())) {
            LOG.debug("OTRS state ID {} matched OpenNMS state Open", otrsStateId);
            openNMSState = Ticket.State.OPEN;
        } else if (m_configDao.getValidClosedStateId().contains(otrsStateId.intValue())) {
            LOG.debug("OTRS state ID {} matched OpenNMS state Closed", otrsStateId);
            openNMSState = Ticket.State.CLOSED;
        } else if (m_configDao.getValidCancelledStateId().contains(otrsStateId.intValue())) {
            LOG.debug("OTRS state ID {} matched OpenNMS state Cancelled", otrsStateId);
            openNMSState = Ticket.State.CANCELLED;
        } else {
            LOG.debug("OTRS state ID {} has no matching OpenNMS state",  otrsStateId);
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

        LOG.debug("getting otrs state from OpenNMS State {}", state.toString());

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
        LOG.debug("Setting OTRS state ID to {}", otrsStateId.toString());

        return otrsStateId;

    }

}
