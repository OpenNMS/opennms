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

import java.util.regex.Pattern;

import org.apache.mina.core.session.IdleStatus;

/**
 * <p>Abstract AsyncBasicDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class AsyncBasicDetector<Request, Response> extends AsyncAbstractDetector {

    /**
     * Default value of 3000ms = 3s
     */
    private int m_idleTime = 3000;

    private AsyncClientConversation<Request, Response> m_conversation = new AsyncClientConversation<Request, Response>();
    private boolean useSSLFilter = false;
    
    /**
     * <p>Constructor for AsyncBasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param <Request> a Request object.
     * @param <Response> a Response object.
     */
    public AsyncBasicDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /**
     * <p>Constructor for AsyncBasicDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     */
    public AsyncBasicDetector(final String serviceName, final int port, final int timeout, final int retries) {
        super(serviceName, port, timeout, retries);
    }
    
    /**
     * <p>expectBanner</p>
     *
     * @param bannerValidator a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    protected void expectBanner(final ResponseValidator<Response> bannerValidator) {
        m_conversation.setHasBanner(true);
        m_conversation.addExchange(new ConversationExchangeDefaultImpl<Request, Response>(null, bannerValidator));
    }
    
    /**
     * <p>send</p>
     *
     * @param request a Request object.
     * @param responseValidator a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    protected void send(final Request request, final ResponseValidator<Response> responseValidator) {
        m_conversation.addExchange(new ConversationExchangeDefaultImpl<Request, Response>(new RequestBuilder<Request>() {
            @Override
            public Request getRequest() {
                return request;
            }
        }, responseValidator));
    }
    
    
    /**
     * Set the time limit in milliseconds that the connection can wait before
     * transitioning to the {@link IdleStatus#BOTH_IDLE}, {@link IdleStatus#READER_IDLE}, 
     * or {@link IdleStatus#WRITER_IDLE} states.
     *
     * @param idleTime a int.
     */
    public final void setIdleTime(final int idleTime) {
        m_idleTime = idleTime;
    }

    /**
     * <p>getIdleTime</p>
     *
     * @return a int.
     */
    public final int getIdleTime() {
        return m_idleTime;
    }

    /**
     * <p>getConversation</p>
     *
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation} object.
     */
    protected final AsyncClientConversation<Request, Response> getConversation() {
        return m_conversation;
    }

    /**
     * <p>startsWith</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    protected ResponseValidator<Response> startsWith(final String prefix) {
        return new ResponseValidator<Response>() {

            @Override
            public boolean validate(final Object message) {
                final String str = message.toString().trim();
                return str.startsWith(prefix);
            }
            
        };
    }
    
    /**
     * <p>find</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    protected ResponseValidator<Response> find(final String regex){
        return new ResponseValidator<Response>() {

            @Override
            public boolean validate(final Object message) {
                final String str = message.toString().trim();
                return Pattern.compile(regex).matcher(str).find();
            }
          
            
        };
    }

    /**
     * <p>Setter for the field <code>useSSLFilter</code>.</p>
     *
     * @param useSSLFilter a boolean.
     */
    public final void setUseSSLFilter(final boolean useSSLFilter) {
        this.useSSLFilter = useSSLFilter;
    }

    /**
     * <p>isUseSSLFilter</p>
     *
     * @return a boolean.
     */
    public final boolean isUseSSLFilter() {
        return useSSLFilter;
    }
}
