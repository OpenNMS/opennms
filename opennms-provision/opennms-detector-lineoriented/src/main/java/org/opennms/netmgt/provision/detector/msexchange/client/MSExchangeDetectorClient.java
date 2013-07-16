/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.msexchange.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.msexchange.response.MSExchangeResponse;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>MSExchangeDetectorClient class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MSExchangeDetectorClient implements Client<LineOrientedRequest, MSExchangeResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(MSExchangeDetectorClient.class);
    private Integer m_imapPort;
    private Integer m_pop3Port;
    private String m_pop3Response;
    private String m_imapResponse;
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        
    }

    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException, Exception {
        setImapResponse(connectAndGetResponse(address, getImapPort(), timeout));
        setPop3Response(connectAndGetResponse(address, getPop3Port(), timeout));
    }

    private String connectAndGetResponse(final InetAddress address, final Integer port, final int timeout) {
        Socket socket = null;
        InputStreamReader isr = null;
        BufferedReader lineRdr = null;
        
        if(port != null){
            try{
                socket = new Socket();
                final InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port.intValue());
                socket.connect(inetSocketAddress, timeout);
                socket.setSoTimeout(timeout);
                
    
                // Allocate a line reader
                isr = new InputStreamReader(socket.getInputStream());
                lineRdr = new BufferedReader(isr);
    
                // Read the banner line and see if it contains the
                // substring "Microsoft Exchange"
                //
                final String banner = lineRdr.readLine();
                
                socket.close();
                return banner;
                
            }catch(final Exception e) {
                LOG.debug("An error occurred while connecting to {}:{}", InetAddressUtils.str(address), port, e);
                IOUtils.closeQuietly(lineRdr);
                IOUtils.closeQuietly(isr);
                if(socket != null) {
                    try {
                        socket.close();
                    } catch (final IOException e1) {
                        LOG.debug("Additionally, an exception occurred while trying to close the socket.", e);
                    }
                }
            }
        }
        return null;
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.detector.msexchange.response.MSExchangeResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public MSExchangeResponse receiveBanner() throws IOException, Exception {
        MSExchangeResponse response = new MSExchangeResponse();
        response.setPop3Response(getPop3Response());
        response.setImapResponse(getImapResponse());
        return response;
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     * @return a {@link org.opennms.netmgt.provision.detector.msexchange.response.MSExchangeResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public MSExchangeResponse sendRequest(LineOrientedRequest request) throws IOException, Exception {
        return null;
    }

    /**
     * <p>setImapPort</p>
     *
     * @param imapPort a int.
     */
    public void setImapPort(int imapPort) {
        m_imapPort = imapPort;
    }

    /**
     * <p>getImapPort</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getImapPort() {
        return m_imapPort;
    }

    /**
     * <p>setFtpPort</p>
     *
     * @param ftpPort a int.
     */
    public void setFtpPort(int ftpPort) {
        m_pop3Port = ftpPort;
    }

    /**
     * <p>getPop3Port</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getPop3Port() {
        return m_pop3Port;
    }

    /**
     * <p>setPop3Port</p>
     *
     * @param pop3Port a int.
     */
    public void setPop3Port(int pop3Port) {
        m_pop3Port = pop3Port;
    }

    /**
     * <p>setImapResponse</p>
     *
     * @param imapResponse a {@link java.lang.String} object.
     */
    public void setImapResponse(String imapResponse) {
        m_imapResponse = imapResponse;
    }

    /**
     * <p>getImapResponse</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getImapResponse() {
        return m_imapResponse;
    }

    /**
     * <p>setPop3Response</p>
     *
     * @param pop3Response a {@link java.lang.String} object.
     */
    public void setPop3Response(String pop3Response) {
        m_pop3Response = pop3Response;
    }

    /**
     * <p>getPop3Response</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPop3Response() {
        return m_pop3Response;
    }
}
