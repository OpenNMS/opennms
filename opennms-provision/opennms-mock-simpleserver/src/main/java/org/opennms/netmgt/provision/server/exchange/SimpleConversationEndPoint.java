/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications;
 * Created 10/16/2008
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.server.exchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import org.opennms.core.utils.LogUtils;


public class SimpleConversationEndPoint {
    
    public static class SimpleExchange implements Exchange{
        private ResponseHandler m_responseHandler;
        private RequestHandler m_requestHandler;
        
        public SimpleExchange(ResponseHandler responseHandler, RequestHandler requestHandler) {
            setResponseHandler(responseHandler);
            setRequestHandler(requestHandler);
        }

        public boolean matchResponseByString(String response) {
            return getResponseHandler().matches(response);
        }

        public boolean processResponse(BufferedReader in) throws IOException {
            String input = in.readLine();
            
            LogUtils.infof(this, "SimpleExchange response: " + input);
            if(input == null) { return false;}
            
            return matchResponseByString(input);
        }

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
    
    public void init() throws Exception {
        m_conversation = new Conversation();
    };
    
    public void onInit() {};
    
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    public int getTimeout() {
        return m_timeout;
    }
    
    /**
     * 
     * @param prefix
     * @return ResponseHandler
     */
    protected ResponseHandler startsWith(final String prefix) {
        return new ResponseHandler() {

            public boolean matches(String input) {
                return input.startsWith(prefix);
            }
            
        };
    }
    
    /**
     * 
     * @param phrase
     * @return ResponseHandler
     */
    protected ResponseHandler contains(final String phrase) {
        return new ResponseHandler() {

            public boolean matches(String input) {
                return input.contains(phrase);
            }
            
        };
    }
    
    /**
     * 
     * @param regex
     * @return ResponseHandler
     */
    protected ResponseHandler matches(final String regex) {
        return new ResponseHandler() {
            
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
     */
    protected void addResponseHandler(ResponseHandler responseHandler, RequestHandler requestHandler) {
        m_conversation.addExchange(new SimpleExchange(responseHandler, requestHandler));
    }
    
    protected RequestHandler singleLineRequest(final String request) {
      return new RequestHandler() {

          public void doRequest(OutputStream out) throws IOException {
              out.write(String.format("%s\r\n", request).getBytes());
          }
          
      };
    }
    
    protected RequestHandler multilineLineRequest(final String[] request) {
        return new RequestHandler() {

            public void doRequest(OutputStream out) throws IOException {
                for(int i = 0; i < request.length; i++) {
                    out.write(String.format("%s\r\n", request[i]).getBytes());
                }
            }
          
            
        };
    }
}
