/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
    private volatile SimpleServerRunnable m_runnable;

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
    public void startServer() throws IOException {
        m_runnable = getRunnable();

        setServerThread(new Thread(m_runnable, this.getClass().getSimpleName()));
        getServerThread().setDaemon(true);
        getServerThread().start();
        try {
            if (m_runnable != null) m_runnable.awaitStartup();
        } catch (final InterruptedException e) {
            LOG.debug("Interrupted while starting up.", e);
        }
    }
    
    /**
     * <p>stopServer</p>
     *
     * @throws java.io.IOException if any.
     */
    public void stopServer() throws IOException {
        if (!m_stopped) {
            m_stopped = true;
            final Thread t = getServerThread();
            setServerThread(null);
            IOUtils.closeQuietly(getSocket());
            IOUtils.closeQuietly(getServerSocket());
            try {
                Thread.sleep(200);
            } catch (final InterruptedException e) {
            }

            if(t != null && t.isAlive()) { 
                t.interrupt();
            }
            try {
                if (m_runnable != null) m_runnable.awaitShutdown();
            } catch (final InterruptedException e) {
                LOG.debug("Interrupted while shutting down.", e);
            }
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
    protected SimpleServerRunnable getRunnable() throws IOException {
        return new SimpleServerRunnable() {
            
            @Override
            public void run(){
                OutputStream out = null;
                InputStreamReader isr = null;
                BufferedReader in = null;
                try{
                    if (getTimeout() > 0) {
                        getServerSocket().setSoTimeout(getTimeout());
                    }
                    ready();
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
                            IOUtils.closeQuietly(getSocket());
                        }
                    }
                } catch (final InterruptedException e) {
                    if (m_stopped) {
                        LOG.debug("interrupted, shutting down", e);
                    } else {
                        LOG.info("interrupted while listening", e);
                    }
                } catch (final Exception e) {
                    if (m_stopped) {
                        LOG.trace("error during conversation", e);
                    } else {
                        LOG.info("error during conversation", e);
                    }
                } finally {
                    finished();
                    try {
                        // just in case we're stopping because of an exception
                        stopServer();
                    } catch (final Exception e) {
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
                try {
                    stopServer();
                } catch (final Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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
