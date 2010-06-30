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
package org.opennms.netmgt.provision.detector.simple.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.detector.simple.request.NrpeRequest;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.nrpe.NrpePacket;
import org.opennms.netmgt.provision.support.trustmanager.RelaxedX509TrustManager;

/**
 * <p>NrpeClient class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class NrpeClient implements Client<NrpeRequest, NrpePacket> {
    
    /** 
     * List of cipher suites to use when talking SSL to NRPE, which uses anonymous DH
     */
    private static final String[] ADH_CIPHER_SUITES = new String[] {"TLS_DH_anon_WITH_AES_128_CBC_SHA"};
    
    private Socket m_socket;
    private int m_padding = 2;
    private boolean m_useSsl = true;
    private OutputStream m_out;
    private InputStream m_in;
    
    /**
     * <p>close</p>
     */
    public void close() {
        Socket socket = m_socket;
        m_socket = null;
        
        try {
            if(socket != null) {
                socket.close();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    /** {@inheritDoc} */
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        m_socket = getWrappedSocket(address, port, timeout);
        setOutput(m_socket.getOutputStream());
        setInput(m_socket.getInputStream());
    }
    
    /**
     * <p>getWrappedSocket</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param port a int.
     * @param timeout a int.
     * @return a {@link java.net.Socket} object.
     * @throws java.io.IOException if any.
     */
    protected Socket getWrappedSocket(InetAddress address, int port, int timeout) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(address, port), timeout);
        socket.setSoTimeout(timeout);
        try {
            return wrapSocket(socket, address.getHostAddress(), port);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * <p>wrapSocket</p>
     *
     * @param socket a {@link java.net.Socket} object.
     * @param hostAddress a {@link java.lang.String} object.
     * @param port a int.
     * @return a {@link java.net.Socket} object.
     * @throws java.lang.Exception if any.
     */
    protected Socket wrapSocket(Socket socket, String hostAddress, int port) throws Exception {
        if (! isUseSsl()) {
            return socket;
        } 

        Socket wrappedSocket;

        // set up the certificate validation. USING THIS SCHEME WILL ACCEPT ALL
        // CERTIFICATES
        SSLSocketFactory sslSF = null;

        TrustManager[] tm = { new RelaxedX509TrustManager() };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, tm, new java.security.SecureRandom());
        sslSF = sslContext.getSocketFactory();
        wrappedSocket = sslSF.createSocket(socket, hostAddress, port, true);
        SSLSocket sslSocket = (SSLSocket) wrappedSocket;
        // Set this socket to use anonymous Diffie-Hellman ciphers. This removes the authentication
        // benefits of SSL, but it's how NRPE rolls so we have to play along.
        sslSocket.setEnabledCipherSuites(ADH_CIPHER_SUITES);
        return wrappedSocket;
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.support.nrpe.NrpePacket} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    public NrpePacket receiveBanner() throws IOException, Exception {
        return receiveResponse();
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.detector.simple.request.NrpeRequest} object.
     * @return a {@link org.opennms.netmgt.provision.support.nrpe.NrpePacket} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    public NrpePacket sendRequest(NrpeRequest request) throws IOException, Exception {
        request.send(getOutput());
        return receiveResponse();
    }
    
    private NrpePacket receiveResponse() throws Exception {
        NrpePacket response = NrpePacket.receivePacket(getInput(), getPadding());
        LogUtils.infof(this, "what is response: " + response.getResultCode());
        return response;
    }

    /**
     * <p>setPadding</p>
     *
     * @param padding a int.
     */
    public void setPadding(int padding) {
        m_padding = padding;
    }

    /**
     * <p>getPadding</p>
     *
     * @return a int.
     */
    public int getPadding() {
        return m_padding;
    }

    /**
     * <p>setUseSsl</p>
     *
     * @param useSsl a boolean.
     */
    public void setUseSsl(boolean useSsl) {
        m_useSsl = useSsl;
    }

    /**
     * <p>isUseSsl</p>
     *
     * @return a boolean.
     */
    public boolean isUseSsl() {
        return m_useSsl;
    }

    /**
     * <p>setOutput</p>
     *
     * @param out a {@link java.io.OutputStream} object.
     */
    public void setOutput(OutputStream out) {
        m_out = out;
    }

    /**
     * <p>getOutput</p>
     *
     * @return a {@link java.io.OutputStream} object.
     */
    public OutputStream getOutput() {
        return m_out;
    }

    /**
     * <p>setInput</p>
     *
     * @param in a {@link java.io.InputStream} object.
     */
    public void setInput(InputStream in) {
        m_in = in;
    }

    /**
     * <p>getInput</p>
     *
     * @return a {@link java.io.InputStream} object.
     */
    public InputStream getInput() {
        return m_in;
    }

}
