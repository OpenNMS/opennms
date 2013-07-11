/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ClientConversation class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class ClientConversation<Request, Response> {
    
    private static final Logger LOG = LoggerFactory.getLogger(ClientConversation.class);
    
    private ResponseValidator<Response> m_bannerValidator;
    private final List<ConversationExchange<Request, Response>> m_conversation = new ArrayList<ConversationExchange<Request, Response>>();

    /**
     * <p>expectBanner</p>
     *
     * @param bannerValidator a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public void expectBanner(ResponseValidator<Response> bannerValidator) {
        m_bannerValidator = bannerValidator;
    }

    /**
     * <p>addExchange</p>
     *
     * @param request a Request object.
     * @param validator a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public void addExchange(final Request request, ResponseValidator<Response> validator) {
        RequestBuilder<Request> builder = new RequestBuilder<Request>() {
            @Override
            public Request getRequest() {
                return request;
            }
        };
        addExchange(builder, validator);
    }

    /**
     * <p>addExchange</p>
     *
     * @param requestBuilder a {@link org.opennms.netmgt.provision.support.RequestBuilder} object.
     * @param validator a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public void addExchange(RequestBuilder<Request> requestBuilder, ResponseValidator<Response> validator) {
        addExchange(new ConversationExchangeDefaultImpl<Request, Response>(requestBuilder, validator));
    }

    /**
     * <p>addExchange</p>
     *
     * @param exchange a {@link org.opennms.netmgt.provision.support.ClientConversation.ConversationExchange} object.
     */
    public void addExchange(ConversationExchange<Request, Response> exchange) {
        m_conversation.add(exchange); 
    }

    /**
     * <p>attemptConversation</p>
     *
     * @param client a {@link org.opennms.netmgt.provision.support.Client} object.
     * @return a boolean.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    public boolean attemptConversation(Client<Request, Response> client) throws IOException, Exception { 

        if (m_bannerValidator != null) {
            Response banner = client.receiveBanner();
            if (!m_bannerValidator.validate(banner)) {
                LOG.info("False on Banner");
                return false;
            }
        }

        for(ConversationExchange<Request, Response> ex : m_conversation) {

            Request request = ex.getRequest();

            LOG.info("Sending Request {}\n", request);
            Response response = client.sendRequest(request);

            LOG.info("Received Response {}\n", response);
            if (!ex.validate(response)) {
                return false;
            }
        }

        return true;
    }
}
