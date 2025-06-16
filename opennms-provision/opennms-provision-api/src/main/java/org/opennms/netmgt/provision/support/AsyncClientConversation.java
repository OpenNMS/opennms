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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;



/**
 * <p>
 * A Conversation is a sequence of {@link ConversationExchange} instances that are used to 
 * describe the sequence of messages that are passed back and forth during a network
 * transaction.
 * </p>
 *
 * @author Donald Desloge
 */
public class AsyncClientConversation<Request, Response> {

    private final List<ConversationExchange<Request, Response>> m_conversation = new ArrayList<ConversationExchange<Request, Response>>();
    private boolean m_isComplete = false;
    private boolean m_hasBanner = false;
    
    /**
     * <p>addExchange</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.support.ConversationExchange} object.
     */
    public void addExchange(final ConversationExchange<Request, Response> request) {
        m_conversation.add(request);
    }
    
    /**
     * <p>setHasBanner</p>
     *
     * @param hasBanner a boolean.
     */
    public void setHasBanner(final boolean hasBanner){
        m_hasBanner = hasBanner;
    }
    
    /**
     * <p>hasBanner</p>
     *
     * @return a boolean.
     */
    public boolean hasBanner(){
        return m_hasBanner;
    }


    /**
     * <p>hasExchanges</p>
     *
     * @return a boolean.
     */
    public boolean hasExchanges() {
        return !m_conversation.isEmpty();
    }

    /**
     * <p>isComplete</p>
     *
     * @return a boolean.
     */
    public boolean isComplete() {
        return m_isComplete;
    }

    /**
     * <p>validate</p>
     *
     * @param message a Response object.
     * @return a boolean.
     */
    public boolean validate(final Response message) {
        final ConversationExchange<Request, Response> ex = m_conversation.remove(0);

        if(m_conversation.isEmpty()) {
            m_isComplete = true;
        }
        
        return ex.validate(message);
    }

    /**
     * <p>getRequest</p>
     *
     * @return a Request object.
     */
    public Request getRequest() {
        return extracted();
    }

    private Request extracted() {
        return m_conversation.isEmpty() ? null : m_conversation.get(0).getRequest();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("hasBanner", m_hasBanner);
        builder.append("isComplete", m_isComplete);
        builder.append("conversation", m_conversation.toArray());
        return builder.toString();
    }
}
