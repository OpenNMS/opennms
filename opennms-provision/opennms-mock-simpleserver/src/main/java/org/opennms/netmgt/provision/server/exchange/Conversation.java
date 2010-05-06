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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;

public class Conversation {    
    
    public static class ErrorExchange implements Exchange{
        private String m_errorString = "DEFAULT ERROR STRING: YOU HAVE NOT IMPLEMENTED AN ERROR EXCHANGE";
        
        public boolean matchResponseByString(String response) {
            return false;
        }

        public boolean processResponse(BufferedReader in) throws IOException {
            return true;
        }

        public boolean sendRequest(OutputStream out) throws IOException {
            out.write(String.format("%s\r\n", m_errorString).getBytes());
            return true;
        }
        
    }
    
    private List<Exchange> m_conversation = new ArrayList<Exchange>();
    private Exchange m_errorExchange = new ErrorExchange();
    
    public void addExchange(Exchange exchange) {
        m_conversation.add(exchange); 
    }
    
    public void addErrorExchange(Exchange ex) {
        m_errorExchange = ex;
    }
    
    public void attemptServerConversation(BufferedReader in, OutputStream out) throws Exception {
        boolean isFinished = false;

        while(!isFinished) {
           try { 
                if(m_conversation.size() == 0) { return; }
                String line  = in.readLine();
                LogUtils.infof(this, "Server line read: " + line);
                
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
           }catch(Exception e) {
               isFinished = true;
               info(e, "SimpleServer conversation attempt failed");
           }
            
        }
        
    }
    
    public boolean attemptClientConversation(BufferedReader in, OutputStream out) throws IOException {
        
        for(Iterator<Exchange> it = m_conversation.iterator(); it.hasNext();) {
            Exchange ex = it.next();
            
            if(!ex.processResponse(in)) {
               return false; 
            }
            LogUtils.infof(this, "processed response successfully");
            if(!ex.sendRequest(out)) {
                return false;
            }
            LogUtils.infof(this, "send request if there was a request");
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
    
    public ResponseHandler startsWith(final String response){
        return new ResponseHandler(){

            public boolean matches(String input) {
                return input.startsWith(response);      
            }
            
        };
    }
    
    public ResponseHandler contains(final String response){
        return new ResponseHandler(){

            public boolean matches(String input) {
               return input.contains(response);
            }
            
        };
    }
    
    public ResponseHandler regexpMatches(final String response){
        return new ResponseHandler(){

            public boolean matches(String input) {
               return input.matches(response);
            }
            
        };
    }
    
    private void info(Throwable t, String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        if (log.isInfoEnabled()) {
            log.info(String.format(format, args), t);
        }
    }

    
    
}
