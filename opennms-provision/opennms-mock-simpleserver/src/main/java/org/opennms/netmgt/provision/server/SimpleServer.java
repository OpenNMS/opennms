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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.server.exchange.Exchange;
import org.opennms.netmgt.provision.server.exchange.RequestHandler;
import org.opennms.netmgt.provision.server.exchange.SimpleConversationEndPoint;

public class SimpleServer extends SimpleConversationEndPoint {
    
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
    
    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;
    private int m_threadSleepLength = 0;
    private Socket m_socket;
    private String m_banner;
    protected volatile boolean m_stopped;
    
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
        return getServerSocket().getInetAddress();
    }
    
    public int getLocalPort() {
        return getServerSocket().getLocalPort();
    }
    
    public void setThreadSleepLength(int timeout) {
        m_threadSleepLength = timeout;
    }
    
    public int getThreadSleepLength() {
        return m_threadSleepLength;
    }
    
    public void init() throws Exception {
        super.init();
        setServerSocket(new ServerSocket());
        getServerSocket().bind(null);
        onInit();
    }

    public void onInit() {} 
    
    public void startServer() throws Exception {
        setServerThread(new Thread(getRunnable(), this.getClass().getSimpleName()));
        getServerThread().start();
    }
    
    public void stopServer() throws IOException {
        m_stopped = true;
//        getServerSocket().getSoTimeout();
        getServerSocket().close();
        if(getServerThread() != null && getServerThread().isAlive()) { 
            
            if(getSocket() != null && !getSocket().isClosed()) {
               getSocket().close();  
            }
            
        }
    }
    
    public void dispose(){
        
    }
    
    protected Runnable getRunnable() throws Exception {
        return new Runnable(){
            
            public void run(){
                try{
                    if (getTimeout() > 0) {
                        getServerSocket().setSoTimeout(getTimeout());
                    }
                    while (!m_stopped) {
                        setSocket(getServerSocket().accept());
                        if (m_threadSleepLength > 0) {
                            Thread.sleep(m_threadSleepLength);
                        }
                        if (getTimeout() > 0) {
                            getSocket().setSoTimeout(getTimeout());
                        }
                        OutputStream out = getSocket().getOutputStream();
                        if (getBanner() != null) {
                            sendBanner(out);
                        }
                        ;
                        BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
                        attemptConversation(in, out);
                    }
                } catch (Exception e){
                    info(e, "SimpleServer Exception on conversation");
                } finally {
                    try {
                        stopServer();
                    } catch (IOException e) {
                        info(e, "SimpleServer Exception on stopping server");
                    }
                }
            }
            
        };
    }
    
    protected void sendBanner(OutputStream out) throws IOException {
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
    
    protected void addErrorHandler(RequestHandler requestHandler) {
        m_conversation.addErrorExchange(new ServerErrorExchange(requestHandler));
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
                stopServer();
            }
            
        };
    }
    
    public void setServerSocket(ServerSocket serverSocket) {
        m_serverSocket = serverSocket;
    }
    
    public ServerSocket getServerSocket() {
        return m_serverSocket;
    }

    public void setSocket(Socket socket) {
        m_socket = socket;
    }

    public Socket getSocket() {
        return m_socket;
    }

    protected void setServerThread(Thread serverThread) {
        m_serverThread = serverThread;
    }

    protected Thread getServerThread() {
        return m_serverThread;
    }
    
    private void info(Throwable t, String format, Object... args) {
        ThreadCategory log = ThreadCategory.getInstance(getClass());
        if (log.isInfoEnabled()) {
            log.info(String.format(format, args), t);
        }
    }
    
}
