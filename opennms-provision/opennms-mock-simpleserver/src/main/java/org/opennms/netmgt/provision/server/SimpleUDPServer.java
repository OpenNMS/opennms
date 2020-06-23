/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleUDPServer {
    private static Logger LOG = LoggerFactory.getLogger(SimpleUDPServer.class);

    public static interface RequestMatcher {
        public boolean matches(DatagramPacket input);
    }
    
    public static interface Exchange {
        public boolean sendReply(DatagramSocket socket) throws IOException;
        public boolean processRequest(DatagramSocket socket) throws IOException;
    }
    
    public static class SimpleServerExchange implements Exchange{
        private byte[] m_response;
        private RequestMatcher m_requestMatcher;
        private int m_responsePort;
        private InetAddress m_responseAddress;
        
        public SimpleServerExchange(RequestMatcher requestMatcher, byte[] response){
            m_response = response;
            m_requestMatcher = requestMatcher;
        }
        
        @Override
        public boolean processRequest(DatagramSocket socket) throws IOException {
            byte[] data = new byte[512];
            DatagramPacket packet = new DatagramPacket(data, data.length, socket.getLocalAddress(), socket.getLocalPort());
            
            socket.receive(packet);
            
            setResponsePort(packet.getPort());
            setResponseAddress(packet.getAddress());
            return m_requestMatcher.matches(packet);
        }

        @Override
        public boolean sendReply(DatagramSocket socket) throws IOException {
            DatagramPacket packet = new DatagramPacket(m_response, m_response.length, getResponseAddress(), getResponsePort());
            socket.send(packet);
            
            return true;
        }

        public void setResponsePort(int responsePort) {
            m_responsePort = responsePort;
        }

        public int getResponsePort() {
            return m_responsePort;
        }

        public void setResponseAddress(InetAddress responseAddress) {
            m_responseAddress = responseAddress;
        }

        public InetAddress getResponseAddress() {
            return m_responseAddress;
        }
        
    }
    
    private static int DEFAULT_TEST_PORT = 8888;
    
    private Thread m_serverThread = null;
    private int m_timeout;
    private DatagramSocket m_socket;
    private List<Exchange> m_conversation = new ArrayList<>();
    private int m_port = DEFAULT_TEST_PORT;
    private InetAddress m_testInetAddress;

    private SimpleServerRunnable m_runnable;
    
    /**
     * <p>onInit</p>
     */
    public void onInit() {
        // Do nothing by default
    } 
    
    /**
     * <p>startServer</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void startServer() throws IOException {
        m_runnable = getRunnable();
        m_serverThread = new Thread(m_runnable, this.getClass().getSimpleName());
        m_serverThread.start();
        try {
            m_runnable.awaitStartup();
        } catch (final InterruptedException e) {
            LOG.debug("Interrupted while shutting down.", e);
        }
    }
    
    /**
     * <p>stopServer</p>
     *
     * @throws java.io.IOException if any.
     */
    public void stopServer() throws IOException {
        IOUtils.closeQuietly(getServerSocket());
        IOUtils.closeQuietly(m_socket);
        if(m_serverThread != null && m_serverThread.isAlive()) { 
            m_serverThread.interrupt();
        }
        try {
            m_runnable.awaitShutdown();
        } catch (final InterruptedException e) {
            LOG.debug("Interrupted while shutting down.", e);
        }
    }
    
    private DatagramSocket getServerSocket() {
        return m_socket;
    }

    /**
     * <p>getRunnable</p>
     *
     * @return a {@link java.lang.Runnable} object.
     * @throws java.lang.Exception if any.
     */
    public SimpleServerRunnable getRunnable() throws IOException {
        return new SimpleServerRunnable() {
            
            @Override
            public void run() {
                try{
                    m_socket = new DatagramSocket(getPort(), getInetAddress());
                    m_socket.setSoTimeout(getTimeout());
                    ready();

                    attemptConversation(m_socket);
                } catch(Throwable e) {
                    throw new UndeclaredThrowableException(e);
                } finally {
                    IOUtils.closeQuietly(m_socket);
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
     * <p>setPort</p>
     *
     * @param port a int.
     */
    public void setPort(int port) {
        m_port = port;
    }  
    
    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    public int getPort(){
        return m_port;
    }
    
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
     * <p>attemptConversation</p>
     *
     * @param socket a {@link java.net.DatagramSocket} object.
     * @return a boolean.
     * @throws java.io.IOException if any.
     */
    protected boolean attemptConversation(DatagramSocket socket) throws IOException{
        for(Exchange ex : m_conversation){
            if(!ex.processRequest(socket)){
                return false;
            }
            
            if(!ex.sendReply(socket)){
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * <p>addRequestResponse</p>
     *
     * @param request a {@link java.net.DatagramPacket} object.
     * @param response an array of byte.
     */
    protected void addRequestResponse(DatagramPacket request, byte[] response){
        m_conversation.add(new SimpleServerExchange(recievedPacket(request), Arrays.copyOf(response, response.length)));
    }
    
    /**
     * <p>recievedPacket</p>
     *
     * @param request a {@link java.net.DatagramPacket} object.
     * @return a {@link org.opennms.netmgt.provision.server.SimpleUDPServer.RequestMatcher} object.
     */
    protected RequestMatcher recievedPacket(final DatagramPacket request) {
        return new RequestMatcher() {

            @Override
            public boolean matches(DatagramPacket packet) {
                return packet != null ? true : false;
            }
            
        };
    }

    /**
     * <p>setInetAddress</p>
     *
     * @param testInetAddress a {@link java.net.InetAddress} object.
     */
    public void setInetAddress(InetAddress testInetAddress) {
        m_testInetAddress = testInetAddress;
    }

    /**
     * <p>getInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getInetAddress() {
        if(m_testInetAddress == null) {
            return InetAddressUtils.getLocalHostAddress();
        } else {
            return m_testInetAddress;
        }
    }

    
    
}
