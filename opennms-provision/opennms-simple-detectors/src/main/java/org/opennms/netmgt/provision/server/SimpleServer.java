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
package org.opennms.netmgt.provision.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.opennms.netmgt.provision.conversation.SimpleConversationEndPoint;
import org.opennms.netmgt.provision.exchange.Exchange;
import org.opennms.netmgt.provision.exchange.ResponseHandler;

public class SimpleServer extends SimpleConversationEndPoint {
    
    public static interface RequestHandler{
        public void doRequest(OutputStream out) throws IOException;
    }
    
    public static class ServerErrorExchange implements Exchange{
        protected RequestHandler m_errorRequest;
        
        public ServerErrorExchange(RequestHandler requestHandler) {
            m_errorRequest = requestHandler;
        }
        
        public boolean matchResponseByString(String response) {
            return false;
        }

        public boolean processResponse(BufferedReader in) throws IOException {
            return false;
        }

        public boolean sendRequest(OutputStream out) throws IOException {
            m_errorRequest.doRequest(out);
            return false;
        }
        
    }
    
    public static class SimpleServerExchange implements Exchange {
        protected RequestHandler m_requestHandler;
        protected ResponseHandler m_responseHandler;
        
        public SimpleServerExchange(ResponseHandler responseHandler, RequestHandler requestHandler) {
            m_requestHandler = requestHandler;
            m_responseHandler = responseHandler;
        }

        public boolean processResponse(BufferedReader in) throws IOException {
            String line = in.readLine();
            
            if( line == null ) { return false;}
            
            return m_responseHandler.matches(line);
        }

        public boolean sendRequest(OutputStream out) throws IOException {
            m_requestHandler.doRequest(out);
            return true;
        }
        
        public boolean matchResponseByString(String response) {
            return m_responseHandler.matches(response);
        }
        
        public String toString() {
            return "The responsehandler:" + m_responseHandler + " and the request: " + m_requestHandler;
        }
        
    }
    
    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;
    private int m_threadSleepLength = 0;
    private Socket m_socket;
    private String m_banner;
    
    public void setBanner(String banner){
        m_banner = banner;
    }
    
    public String getBanner() {
        return m_banner;
    }
    
    /**
     * 
     * @return InetAddress returns the inetaddress from the serversocket.
     */
    public InetAddress getInetAddress(){
        return m_serverSocket.getInetAddress();
    }
    
    public int getLocalPort() {
        return m_serverSocket.getLocalPort();
    }
    
    public void setThreadSleepLength(int timeout) {
        m_threadSleepLength = timeout;
    }
    
    public void init() throws IOException {
        super.init();
        m_serverSocket = new ServerSocket();
        m_serverSocket.bind(null);
        onInit();
    }

    public void onInit() {} 
    
    public void startServer() throws Exception {
        m_serverThread = new Thread(getRunnable());
        m_serverThread.start();
    }
    
    public void stopServer() throws IOException {
        if(m_socket != null && !m_socket.isClosed()) { m_socket.close(); }
    }
    
    private Runnable getRunnable() throws Exception {
        return new Runnable(){
            
            public void run(){
                try{
                    m_serverSocket.setSoTimeout(getTimeout());
                    m_socket = m_serverSocket.accept();
                    if(m_threadSleepLength > 0) { Thread.sleep(m_threadSleepLength); }
                    m_socket.setSoTimeout(getTimeout());
                    
                    OutputStream out = m_socket.getOutputStream();
                    if(getBanner() != null){sendBanner(out);};
                    
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
                    
                    attemptConversation(in, out);
                    
                }catch(Exception e){
                    throw new UndeclaredThrowableException(e);
                } finally {
                    try {                        
                        m_socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            
        };
    }
    
    private void sendBanner(OutputStream out) throws IOException {
        out.write(String.format("%s\r\n", getBanner()).getBytes());        
    }
    
    /**
     * 
     * @param in
     * @param out
     * @return
     * @throws Exception 
     */
    protected boolean attemptConversation(BufferedReader in, OutputStream out) throws Exception{
        m_conversation.attemptServerConversation(in, out);      
        return true;
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
        m_conversation.addExchange(new SimpleServerExchange(responseHandler, requestHandler));
    }
    
    protected void addErrorHandler(RequestHandler requestHandler) {
        m_conversation.addErrorExchange(new ServerErrorExchange(requestHandler));
    }
    
    protected RequestHandler singleLineReply(final String reply) {
        return new RequestHandler() {

            public void doRequest(OutputStream out) throws IOException {
                out.write(String.format("%s\r\n", reply).getBytes());
            }
            
        };
    }
    
    protected RequestHandler errorString(final String error) {
        return new RequestHandler() {

            public void doRequest(OutputStream out) throws IOException {
                out.write(String.format("%s\r\n", error).getBytes());
                
            }
            
        };
    }
    
    protected RequestHandler shutdownServer(final String response) {
        return new RequestHandler() {
            
            public void doRequest(OutputStream out) throws IOException {
                out.write(String.format("%s\r\n", response).getBytes());
                if(!m_socket.isClosed()) {
                    m_socket.close();
                }
            }
            
        };
    }
    
}
