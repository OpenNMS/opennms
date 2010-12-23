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

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.server.exchange.Exchange;
import org.opennms.netmgt.provision.server.exchange.RequestHandler;
import org.opennms.netmgt.provision.server.exchange.SimpleConversationEndPoint;

/**
 * <p>SimpleServer class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SimpleServer extends SimpleConversationEndPoint {
    
    public static class ServerErrorExchange implements Exchange{
        protected RequestHandler m_errorRequest;
        
        public ServerErrorExchange(final RequestHandler requestHandler) {
            m_errorRequest = requestHandler;
        }
        
        public boolean matchResponseByString(final String response) {
            return false;
        }

        public boolean processResponse(final BufferedReader in) throws IOException {
            return false;
        }

        public boolean sendRequest(final OutputStream out) throws IOException {
            m_errorRequest.doRequest(out);
            return false;
        }
        
    }
    
    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;
    private int m_threadSleepLength = 0;
    private Socket m_socket;
    private String m_banner;
    protected volatile boolean m_stopped = false;
    
    /**
     * <p>setBanner</p>
     *
     * @param banner a {@link java.lang.String} object.
     */
    public void setBanner(final String banner){
        m_banner = banner;
    }
    
    /**
     * <p>getBanner</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBanner() {
        return m_banner;
    }
    
    /**
     * <p>getInetAddress</p>
     *
     * @return InetAddress returns the inetaddress from the serversocket.
     */
    public InetAddress getInetAddress(){
        return getServerSocket().getInetAddress();
    }
    
    /**
     * <p>getLocalPort</p>
     *
     * @return a int.
     */
    public int getLocalPort() {
        return getServerSocket().getLocalPort();
    }
    
    /**
     * <p>setThreadSleepLength</p>
     *
     * @param timeout a int.
     */
    public void setThreadSleepLength(final int timeout) {
        m_threadSleepLength = timeout;
    }
    
    /**
     * <p>getThreadSleepLength</p>
     *
     * @return a int.
     */
    public int getThreadSleepLength() {
        return m_threadSleepLength;
    }
    
    /**
     * <p>init</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void init() throws Exception {
        super.init();
        setServerSocket(new ServerSocket());
        getServerSocket().bind(null);
        onInit();
    }

    /**
     * <p>onInit</p>
     */
    public void onInit() {} 
    
    /**
     * <p>startServer</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void startServer() throws Exception {
        setServerThread(new Thread(getRunnable(), this.getClass().getSimpleName()));
        getServerThread().start();
    }
    
    /**
     * <p>stopServer</p>
     *
     * @throws java.io.IOException if any.
     */
    public void stopServer() throws IOException {
        m_stopped = true;
        getServerSocket().close();
        if(getServerThread() != null && getServerThread().isAlive()) { 
            
            if(getSocket() != null && !getSocket().isClosed()) {
               getSocket().close();  
            }
            
        }
    }
    
    /**
     * <p>dispose</p>
     */
    public void dispose(){
        
    }
    
    /**
     * <p>getRunnable</p>
     *
     * @return a {@link java.lang.Runnable} object.
     * @throws java.lang.Exception if any.
     */
    protected Runnable getRunnable() throws Exception {
        return new Runnable(){
            
            public void run(){
                OutputStream out = null;
                InputStreamReader isr = null;
                BufferedReader in = null;
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
                        out = getSocket().getOutputStream();
                        if (getBanner() != null) {
                            sendBanner(out);
                        }
                        isr = new InputStreamReader(getSocket().getInputStream());
                        in = new BufferedReader(isr);
                        attemptConversation(in, out);
                    }
                } catch (final Exception e){
                    LogUtils.infof(this, e, "SimpleServer Exception on conversation");
                } finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(isr);
                    IOUtils.closeQuietly(out);
                    try {
                        stopServer();
                    } catch (final IOException e) {
                        LogUtils.infof(this, e, "SimpleServer Exception on stopping server");
                    }
                }
            }
            
        };
    }
    
    /**
     * <p>sendBanner</p>
     *
     * @param out a {@link java.io.OutputStream} object.
     * @throws java.io.IOException if any.
     */
    protected void sendBanner(final OutputStream out) throws IOException {
        out.write(String.format("%s\r\n", getBanner()).getBytes());        
    }
    
    /**
     * <p>attemptConversation</p>
     *
     * @param in a {@link java.io.BufferedReader} object.
     * @param out a {@link java.io.OutputStream} object.
     * @throws java.lang.Exception if any.
     * @return a boolean.
     */
    protected boolean attemptConversation(final BufferedReader in, final OutputStream out) throws Exception{
        m_conversation.attemptServerConversation(in, out);      
        return true;
    }
    
    /**
     * <p>addErrorHandler</p>
     *
     * @param requestHandler a {@link org.opennms.netmgt.provision.server.exchange.RequestHandler} object.
     */
    protected void addErrorHandler(final RequestHandler requestHandler) {
        m_conversation.addErrorExchange(new ServerErrorExchange(requestHandler));
    }
    
    /**
     * <p>errorString</p>
     *
     * @param error a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.server.exchange.RequestHandler} object.
     */
    protected RequestHandler errorString(final String error) {
        return new RequestHandler() {

            public void doRequest(final OutputStream out) throws IOException {
                out.write(String.format("%s\r\n", error).getBytes());
                
            }
            
        };
    }
    
    /**
     * <p>shutdownServer</p>
     *
     * @param response a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.server.exchange.RequestHandler} object.
     */
    protected RequestHandler shutdownServer(final String response) {
        return new RequestHandler() {
            
            public void doRequest(final OutputStream out) throws IOException {
                out.write(String.format("%s\r\n", response).getBytes());
                stopServer();
            }
            
        };
    }
    
    /**
     * <p>setServerSocket</p>
     *
     * @param serverSocket a {@link java.net.ServerSocket} object.
     */
    public void setServerSocket(final ServerSocket serverSocket) {
        m_serverSocket = serverSocket;
    }
    
    /**
     * <p>getServerSocket</p>
     *
     * @return a {@link java.net.ServerSocket} object.
     */
    public ServerSocket getServerSocket() {
        return m_serverSocket;
    }

    /**
     * <p>setSocket</p>
     *
     * @param socket a {@link java.net.Socket} object.
     */
    public void setSocket(final Socket socket) {
        m_socket = socket;
    }

    /**
     * <p>getSocket</p>
     *
     * @return a {@link java.net.Socket} object.
     */
    public Socket getSocket() {
        return m_socket;
    }

    /**
     * <p>setServerThread</p>
     *
     * @param serverThread a {@link java.lang.Thread} object.
     */
    protected void setServerThread(final Thread serverThread) {
        m_serverThread = serverThread;
    }

    /**
     * <p>getServerThread</p>
     *
     * @return a {@link java.lang.Thread} object.
     */
    protected Thread getServerThread() {
        return m_serverThread;
    }
}
