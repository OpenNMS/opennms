/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
