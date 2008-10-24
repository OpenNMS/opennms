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
package org.opennms.netmgt.provision.detector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.regex.Pattern;

import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.conversation.Conversation;
import org.opennms.netmgt.provision.conversation.SimpleConversationEndPoint;
import org.opennms.netmgt.provision.exchange.RequestHandler;
import org.opennms.netmgt.provision.exchange.ResponseHandler;

/**
 * 
 * @author <a href=mailto:desloge@opennms.com>Donald Desloge</a>
 *
 */

abstract public class SimpleDetector extends SimpleConversationEndPoint implements ServiceDetector {
    
    private int m_port;
    private int m_retries;
    private int m_timeout;
    private String m_serviceName;
    private Socket m_socket;
    
    private Conversation m_conversation = new Conversation();
    
    protected SimpleDetector(int defaultPort, int defaultTimeout, int defaultRetries) {
        m_port = defaultPort;
        m_timeout = defaultTimeout;
        m_retries = defaultRetries;
    }
        
    public int getPort() {
        return m_port;
    }

    public void setPort(int port) {
        m_port = port;
    }

    public int getRetries() {
        return m_retries;
    }

    public void setRetries(int retries) {
        m_retries = retries;
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }
    
    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }

    public String getServiceName() {
        return m_serviceName;
    }
    
    public void init() {
        if (m_timeout <= 0) {
            throw new IllegalStateException(String.format("Timeout of %d is invalid.  Must be > 0", m_timeout));
        }
        
        onInit();
    }
    
    public void onInit() { }
    
    protected Socket createSocketConnection(InetAddress host, int port, int timeout) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeout);
        socket.setSoTimeout(timeout);
        return socket;
    }
    
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectorMonitor) {
        int port = getPort();
        int retries = getRetries();
        int timeout = getTimeout();
        
        detectorMonitor.start(this, "Checking address: %s for %s capability", address, getServiceName());
                
        for (int attempts = 0; attempts <= retries; attempts++) {
            m_socket = null;

            try {
                m_socket = createSocketConnection(address, port, timeout);
                
                detectorMonitor.attempt(this, attempts, "Attempting to connect to address: %s attempt #%s",address.getHostAddress(),attempts);

                // Allocate a line reader
                //
                OutputStream out = m_socket.getOutputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
                
                if (attemptConversation(in, out)) {
                    return true;
                }
                
            } catch (ConnectException cE) {
                // Connection refused!! Continue to retry.
            } catch (NoRouteToHostException e) {
                // No Route to host!!!
               
                e.fillInStackTrace();
                detectorMonitor.info(this, e, "%s: No route to address %s was available", getServiceName(), address.getHostAddress());
                throw new UndeclaredThrowableException(e);
            } catch (InterruptedIOException e) {
                // Expected exception
                detectorMonitor.info(this, e, "%s: Did not connec to to address within timeout: %d attempt: %d", getServiceName(), timeout, attempts);
            } catch (IOException e) {
                detectorMonitor.info(this, e, "%s: An unexpected I/O exception occured contacting address %s",getServiceName(), address.getHostAddress());
            } catch (Throwable t) {
                detectorMonitor.failure(this, "%s: Failed to detect %s on address %s", getServiceName(), getServiceName(), address.getHostAddress());
                detectorMonitor.error(this, t, "%s: An undeclared throwable exception was caught contating address %s", getServiceName(), address.getHostAddress());
                t.fillInStackTrace();
                System.out.println("error : " + t.getMessage());
            } finally {
                try {
                    if (m_socket != null)
                        m_socket.close();
                } catch (IOException e) {
                }
            }
        }
        
        return false;
    }
    
    private boolean attemptConversation(BufferedReader in, OutputStream out) throws IOException {
        return m_conversation.attemptClientConversation(in, out);
    }

    protected void expectBanner(ResponseHandler bannerMatcher, RequestHandler requestHandler) {
        m_conversation.addExchange(new SimpleExchange(bannerMatcher, requestHandler));
    }
    
    protected void addResponseHandler(ResponseHandler responseHandler, RequestHandler requestHandler) {
        m_conversation.addExchange(new SimpleExchange(responseHandler, requestHandler));
    }
    
    protected RequestHandler closeDetector() {
        return new RequestHandler() {

            public void doRequest(OutputStream out) throws IOException {
                if(m_socket != null && !m_socket.isClosed()) {
                    m_socket.close();
                }
            }
            
        };
    }
    
    protected ResponseHandler regexFind(final String regex) {
        return new ResponseHandler() {

            public boolean matches(String input) {
                return Pattern.compile(regex).matcher(input).find();
            }
            
        };
    }
    
}
