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

package org.opennms.netmgt.provision.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.provision.server.exchange.Exchange;
import org.opennms.netmgt.provision.server.exchange.RequestHandler;
import org.opennms.netmgt.provision.server.exchange.SimpleConversationEndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SimpleServer class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SimpleServer extends SimpleConversationEndPoint {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleServer.class);
    
    public static class ServerErrorExchange implements Exchange{
        protected RequestHandler m_errorRequest;
        
        public ServerErrorExchange(final RequestHandler requestHandler) {
            m_errorRequest = requestHandler;
        }
        
        @Override
        public boolean matchResponseByString(final String response) {
            return false;
        }

        @Override
        public boolean processResponse(final BufferedReader in) throws IOException {
            return false;
        }

        @Override
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
    private int m_bannerDelay = 0;
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
     * Slow down transmission of the banner by a specified number of milliseconds.
     */
    public void setBannerDelay(final int delay){
        m_bannerDelay = delay;
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
    @Override
    public void init() throws Exception {
        super.init();
        setServerSocket(new ServerSocket());
        getServerSocket().bind(null);
        onInit();
    }

    /**
     * <p>onInit</p>
     */
    protected void onInit() {
        // Do nothing by default
    } 
    
    /**
     * <p>startServer</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void startServer() throws Exception {
        setServerThread(new Thread(getRunnable(), this.getClass().getSimpleName()));
        getServerThread().setDaemon(true);
        getServerThread().start();
    }
    
    /**
     * <p>stopServer</p>
     *
     * @throws java.io.IOException if any.
     */
    public void stopServer() throws IOException {
        if (!m_stopped) {
            m_stopped = true;
            Thread t = getServerThread();
            if(t != null && t.isAlive()) { 
                t.interrupt();
                try {
                    Thread.sleep(20);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if(getSocket() != null && !getSocket().isClosed()) {
                   getSocket().close();  
                }
            }
            setServerThread(null);
            getServerSocket().close();
        }
    }
    
    /**
     * <p>dispose</p>
     */
    public void dispose(){
        // Do nothing by default
    }
    
    /**
     * <p>getRunnable</p>
     *
     * @return a {@link java.lang.Runnable} object.
     * @throws java.lang.Exception if any.
     */
    protected Runnable getRunnable() throws Exception {
        return new Runnable(){
            
            @Override
            public void run(){
                OutputStream out = null;
                InputStreamReader isr = null;
                BufferedReader in = null;
                try{
                    if (getTimeout() > 0) {
                        getServerSocket().setSoTimeout(getTimeout());
                    }
                    while (!m_stopped && getServerThread() != null) {
                        long startTime = 0;
                        try {
                            setSocket(getServerSocket().accept());
                            if (getTimeout() > 0) {
                                getSocket().setSoTimeout(getTimeout());
                            }
                            out = getSocket().getOutputStream();
                            startTime = System.currentTimeMillis();
                            if (m_threadSleepLength > 0) {
                                Thread.sleep(m_threadSleepLength);
                            }
                            if (getBanner() != null) {
                                sendBanner(out);
                            }
                            isr = new InputStreamReader(getSocket().getInputStream());
                            in = new BufferedReader(isr);
                            attemptConversation(in, out);
                        } finally {
                            // Sleep to make sure we connect at least as long as the timeout that is set
                            long sleepMore = startTime + getTimeout() - System.currentTimeMillis();
                            if (sleepMore > 0) {
                                try { Thread.sleep(sleepMore); } catch (InterruptedException e) {}
                            }
                            
                            IOUtils.closeQuietly(in);
                            IOUtils.closeQuietly(isr);
                            IOUtils.closeQuietly(out);
                            // TODO: Upgrade IOUtils so that we can use this function
                            // IOUtils.closeQuietly(getSocket());
                            getSocket().close();
                        }
                    }
                } catch (final InterruptedException e) {
                    if (m_stopped) {
                        LOG.debug("interrupted, shutting down", e);
                    } else {
                        LOG.info("interrupted while listening", e);
                    }
                    Thread.currentThread().interrupt();
                } catch (final Exception e){
                    if (m_stopped) {
                        LOG.trace("error during conversation", e);
                    } else {
                        LOG.info("error during conversation", e);
                    }
                } finally {
                    try {
                        // just in case we're stopping because of an exception
                        stopServer();
                    } catch (final IOException e) {
                        LOG.info("error while stopping server", e);
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
        byte[] bannerBytes = getBanner().getBytes();
        if (m_bannerDelay > 0) {
            int delayPerByte = (int)Math.ceil((float)m_bannerDelay / (float)bannerBytes.length);
            System.out.println("DELAY PER BYTE: " + delayPerByte);
            for (byte bannerByte : bannerBytes) {
                out.write(bannerByte);
                try { Thread.sleep(delayPerByte); } catch (InterruptedException e) {}
            }
        } else {
            out.write(bannerBytes);
        }
        out.write("\r\n".getBytes());
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

            @Override
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
            
            @Override
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
