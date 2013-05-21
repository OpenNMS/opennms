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

package org.opennms.netmgt.provision.support;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.apache.mina.core.session.IdleStatus;

/**
 * <p>Abstract AsyncBasicDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class AsyncBasicDetector<Request, Response> extends AsyncAbstractDetector {
    
    protected static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

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
