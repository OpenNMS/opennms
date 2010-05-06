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
package org.opennms.netmgt.provision.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.LogUtils;

abstract public class AbstractSimpleServer {
    
    public static interface RequestMatcher{
        public boolean matches(String input);
    }
    
    public static interface Exchange {
        public boolean sendReply(OutputStream out) throws IOException;
        public boolean processRequest(BufferedReader in) throws IOException;
    }
    
    public static class BannerExchange implements Exchange{
        private String m_banner;
        
        public BannerExchange(String banner){
            m_banner = banner;
        }
        
        public boolean processRequest(BufferedReader in) throws IOException { return true; }

        public boolean sendReply(OutputStream out) throws IOException {
            out.write(String.format("%s\r\n", m_banner).getBytes());
            return true;
        }
        
    }
    
    public static class SimpleServerExchange implements Exchange{
        private String m_response;
        private RequestMatcher m_requestMatcher;
        
        public SimpleServerExchange(RequestMatcher requestMatcher, String response){
            m_response = response;
            m_requestMatcher = requestMatcher;
        }
        
        public boolean processRequest(BufferedReader in) throws IOException {
            String line = in.readLine();
            LogUtils.infof(this, "processing request: " + line);
            
            if(line == null)return false;
            
            return m_requestMatcher.matches(line);
        }

        public boolean sendReply(OutputStream out) throws IOException {
            LogUtils.infof(this, "writing output: " + m_response);
            out.write(String.format("%s\r\n", m_response).getBytes());
            return false;
        }
        
    }
    
    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;
    private Socket m_socket;
    private int m_timeout;
    private List<Exchange> m_conversation = new ArrayList<Exchange>();
    
    public int getTimeout() {
        return m_timeout;
    }
    
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }
    
    /**
     * 
     * @return InetAddress returns the inetaddress from the serversocket.
     */
    public InetAddress getInetAddress(){
        return m_serverSocket.getInetAddress();
    }
    
    public int getLocalPort(){
        return m_serverSocket.getLocalPort();
    }
    
    public void init() throws Exception{
        m_serverSocket = new ServerSocket();
        m_serverSocket.bind(null);
        onInit();
        startServer();
    }

    public void onInit() {} 
    
    public void startServer() throws Exception{
        m_serverThread = new Thread(getRunnable());
        m_serverThread.start();
    }
    
    public Runnable getRunnable() throws Exception{
        return new Runnable(){
            
            public void run(){
                try{
                    m_serverSocket.setSoTimeout(getTimeout());
                    m_socket = m_serverSocket.accept();
                    
                    OutputStream out = m_socket.getOutputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
                    
                    attemptConversation(in, out);
                    
                    m_socket.close();
                }catch(Exception e){
                    throw new UndeclaredThrowableException(e);
                }
            }
            
        };
    }
    
    protected boolean attemptConversation(BufferedReader in, OutputStream out) throws IOException{
        for(Exchange ex : m_conversation){
            if(!ex.processRequest(in)){
                return false;
            }
            
            if(!ex.sendReply(out)){
                return false;
            }
        }
        
        return true;
    }
    
    protected void setExpectedBanner(String banner){
        m_conversation.add(new BannerExchange(banner));
    }
    
    protected void addRequestResponse(String request, String response){
        m_conversation.add(new SimpleServerExchange(regexpMatches(request), response));
    }
    
    protected RequestMatcher regexpMatches(final String regex) {
        return new RequestMatcher() {

            public boolean matches(String input) {
                return input.matches(regex);
            }
            
        };
    }   
}
