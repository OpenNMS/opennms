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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Conversation class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Conversation {
    
    private static final Logger LOG = LoggerFactory.getLogger(Conversation.class);
    
    public static class ErrorExchange implements Exchange{
        private static final String ERROR_STRING = "DEFAULT ERROR STRING: YOU HAVE NOT IMPLEMENTED AN ERROR EXCHANGE";
        
        @Override
        public boolean matchResponseByString(String response) {
            return false;
        }

        @Override
        public boolean processResponse(BufferedReader in) throws IOException {
            return true;
        }

        @Override
        public boolean sendRequest(OutputStream out) throws IOException {
            out.write(String.format("%s\r\n", ERROR_STRING).getBytes());
            return true;
        }
        
    }
    
    private final List<Exchange> m_conversation = new ArrayList<Exchange>();
    private Exchange m_errorExchange = new ErrorExchange();
    
    /**
     * <p>addExchange</p>
     *
     * @param exchange a {@link org.opennms.netmgt.provision.server.exchange.Exchange} object.
     */
    public void addExchange(Exchange exchange) {
        m_conversation.add(exchange); 
    }
    
    /**
     * <p>addErrorExchange</p>
     *
     * @param ex a {@link org.opennms.netmgt.provision.server.exchange.Exchange} object.
     */
    public void addErrorExchange(Exchange ex) {
        m_errorExchange = ex;
    }
    
    /**
     * <p>attemptServerConversation</p>
     *
     * @param in a {@link java.io.BufferedReader} object.
     * @param out a {@link java.io.OutputStream} object.
     * @throws java.lang.Exception if any.
     */
    public void attemptServerConversation(BufferedReader in, OutputStream out) throws Exception {
        boolean isFinished = false;

        while(!isFinished) {
           try { 
                if(m_conversation.size() == 0) { return; }
                String line  = in.readLine();
                LOG.debug("Server line read: {}", line);
                
                if(line == null) {
                    return;
                }
                
                Exchange ex = findMatchingExchange(line);
                
                if(ex == null) {
                    m_errorExchange.sendRequest(out);
                }else {
                    if(!ex.sendRequest(out)) {
                        m_errorExchange.sendRequest(out);
                    }
                }
           }catch(Throwable e) {
               isFinished = true;
               Object[] args = {};
               LOG.info("SimpleServer conversation attempt failed", args, e);
           }
            
        }
        
    }
    
    /**
     * <p>attemptClientConversation</p>
     *
     * @param in a {@link java.io.BufferedReader} object.
     * @param out a {@link java.io.OutputStream} object.
     * @return a boolean.
     * @throws java.io.IOException if any.
     */
    public boolean attemptClientConversation(BufferedReader in, OutputStream out) throws IOException {
        
        for(Iterator<Exchange> it = m_conversation.iterator(); it.hasNext();) {
            Exchange ex = it.next();
            
            if(!ex.processResponse(in)) {
               return false; 
            }
            LOG.debug("processed response successfully");
            if(!ex.sendRequest(out)) {
                return false;
            }
            LOG.debug("send request if there was a request");
        }
        
        return true;
        
    }
    
    private Exchange findMatchingExchange(String input) throws IOException {
        
        for(Exchange ex : m_conversation) {
            
            if(ex.matchResponseByString(input)) {
                return ex; 
            }
            
        }
        return null;
    }
    
    /**
     * <p>startsWith</p>
     *
     * @param response a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.server.exchange.ResponseHandler} object.
     */
    public static ResponseHandler startsWith(final String response){
        return new ResponseHandler(){

            @Override
            public boolean matches(String input) {
                return input.startsWith(response);      
            }
            
        };
    }
    
    /**
     * <p>contains</p>
     *
     * @param response a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.server.exchange.ResponseHandler} object.
     */
    public static ResponseHandler contains(final String response){
        return new ResponseHandler(){

            @Override
            public boolean matches(String input) {
               return input.contains(response);
            }
            
        };
    }
    
    /**
     * <p>regexpMatches</p>
     *
     * @param response a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.server.exchange.ResponseHandler} object.
     */
    public static ResponseHandler regexpMatches(final String response){
        return new ResponseHandler(){

            @Override
            public boolean matches(String input) {
               return input.matches(response);
            }
            
        };
    }
}
