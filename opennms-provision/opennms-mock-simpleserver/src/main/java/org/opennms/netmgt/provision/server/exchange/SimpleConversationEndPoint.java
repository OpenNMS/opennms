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

package org.opennms.netmgt.provision.server.exchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SimpleConversationEndPoint class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SimpleConversationEndPoint {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleConversationEndPoint.class);
    
    public static class SimpleExchange implements Exchange{
        private ResponseHandler m_responseHandler;
        private RequestHandler m_requestHandler;
        
        public SimpleExchange(ResponseHandler responseHandler, RequestHandler requestHandler) {
            setResponseHandler(responseHandler);
            setRequestHandler(requestHandler);
        }

        @Override
        public boolean matchResponseByString(String response) {
            return getResponseHandler().matches(response);
        }

        @Override
        public boolean processResponse(BufferedReader in) throws IOException {
            String input = in.readLine();
            
            LOG.info("SimpleExchange response: {}", input);
            if(input == null) { return false;}
            
            return matchResponseByString(input);
        }

        @Override
        public boolean sendRequest(OutputStream out) throws IOException {
            if(getRequestHandler() != null) {
                getRequestHandler().doRequest(out);
            }
            return true;
        }

        public void setResponseHandler(ResponseHandler responseHandler) {
            m_responseHandler = responseHandler;
        }

        public ResponseHandler getResponseHandler() {
            return m_responseHandler;
        }

        public void setRequestHandler(RequestHandler requestHandler) {
            m_requestHandler = requestHandler;
        }

        public RequestHandler getRequestHandler() {
            return m_requestHandler;
        }
        
    }
    protected Conversation m_conversation;
    private int m_timeout;
    
    /**
     * <p>init</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void init() throws Exception {
        m_conversation = new Conversation();
    };
    
    /**
     * <p>setTimeout</p>
     *
     * @param timeout a int.
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return a int.
     */
    public int getTimeout() {
        return m_timeout;
    }
    
    /**
     * <p>startsWith</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @return ResponseHandler
     */
    protected static ResponseHandler startsWith(final String prefix) {
        return new ResponseHandler() {

            @Override
            public boolean matches(String input) {
                return input.startsWith(prefix);
            }
            
        };
    }
    
    /**
     * <p>contains</p>
     *
     * @param phrase a {@link java.lang.String} object.
     * @return ResponseHandler
     */
    protected static ResponseHandler contains(final String phrase) {
        return new ResponseHandler() {

            @Override
            public boolean matches(String input) {
                return input.contains(phrase);
            }
            
        };
    }
    
    /**
     * <p>matches</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return ResponseHandler
     */
    protected static ResponseHandler matches(final String regex) {
        return new ResponseHandler() {
            
            @Override
            public boolean matches(String input) {
                return input.matches(regex);
            }
            
        };
    }
    
    /**
     * Add a ResponseHandler by calling one of the three utility methods:
     *
     * startsWith(String prefix);
     * contains(String phrase);
     * regexMatches(String regex);
     *
     * Within the extending class's overriding onInit method
     *
     * @param responseHandler a {@link org.opennms.netmgt.provision.server.exchange.ResponseHandler} object.
     * @param requestHandler a {@link org.opennms.netmgt.provision.server.exchange.RequestHandler} object.
     */
    protected void addResponseHandler(ResponseHandler responseHandler, RequestHandler requestHandler) {
        m_conversation.addExchange(new SimpleExchange(responseHandler, requestHandler));
    }
    
    /**
     * <p>singleLineRequest</p>
     *
     * @param request a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.server.exchange.RequestHandler} object.
     */
    protected static RequestHandler singleLineRequest(final String request) {
      return new RequestHandler() {

          @Override
          public void doRequest(OutputStream out) throws IOException {
              out.write(String.format("%s\r\n", request).getBytes());
          }
          
      };
    }
    
    /**
     * <p>multilineLineRequest</p>
     *
     * @param request an array of {@link java.lang.String} objects.
     * @return a {@link org.opennms.netmgt.provision.server.exchange.RequestHandler} object.
     */
    protected static RequestHandler multilineLineRequest(final String[] request) {
        return new RequestHandler() {

            @Override
            public void doRequest(OutputStream out) throws IOException {
                for(int i = 0; i < request.length; i++) {
                    out.write(String.format("%s\r\n", request[i]).getBytes());
                }
            }
          
            
        };
    }
}
