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
package org.opennms.netmgt.provision.detector;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.protocols.dns.DNSAddressRequest;

/**
 * @author Donald Desloge
 *
 */
public class DnsDetector extends AbstractDetector {
    
    /**
     * </P>
     * The default port on which the host is checked to see if it supports DNS.
     * </P>
     */
    private final static int DEFAULT_PORT = 53;

    /**
     * Default number of retries for DNS requests
     */
    private final static int DEFAULT_RETRY = 3;

    /**
     * Default timeout (in milliseconds) for DNS requests.
     */
    private final static int DEFAULT_TIMEOUT = 3000; // in milliseconds

    /**
     * Default DNS lookup
     */
    private final static String DEFAULT_LOOKUP = "localhost";
    
    private String m_lookup;
    
    protected DnsDetector() {
        setServiceName("DNS");
        setPort(DEFAULT_PORT);
        setRetries(DEFAULT_RETRY);
        setTimeout(DEFAULT_TIMEOUT);
        setLookup(DEFAULT_LOOKUP);
    }
    
    @Override
    public void init() {

    }

    @Override
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor) {
        
        boolean isAServer = false;
        // Allocate a communication socket
        //
        DatagramSocket socket = null;
        try {
            // Allocate a socket
            //
            socket = new DatagramSocket();
            socket.setSoTimeout(getTimeout());

            // Allocate a receive buffer
            //
            byte[] data = new byte[512];

            for (int count = 0; count < getRetries() && !isAServer; count++) {
                try {
                    // Construct a new DNS Address Request
                    //
                    DNSAddressRequest request = new DNSAddressRequest(getLookup());

                    // build the datagram packet used to request the address.
                    //
                    byte[] rdata = request.buildRequest();
                    DatagramPacket outpkt = new DatagramPacket(rdata, rdata.length, address, getPort());
                    rdata = null;

                    // send the output packet
                    //
                    socket.send(outpkt);

                    // receive a resposne
                    //
                    DatagramPacket inpkt = new DatagramPacket(data, data.length);
                    socket.receive(inpkt);
                    
                    if (inpkt.getAddress().equals(address)) {
                        try {
                            request.verifyResponse(inpkt.getData(), inpkt.getLength());
                            isAServer = true;
                        } catch (IOException ex) {
                            detectMonitor.info(this, ex, "Failed to match response to request, an IOException occured", new Object());
                        }
                    }
                } catch (InterruptedIOException ex) {
                    // discard this exception, do next loop
                    //
                }
            }
        } catch (IOException ex) {
            detectMonitor.info(this, ex, "isServer: An I/O exception during DNS resolution test.", new Object());
            //log.warn("isServer: An I/O exception during DNS resolution test.", ex);
        } finally {
            if (socket != null)
                socket.close();
        }

        return isAServer;
    }

    public void setLookup(String lookup) {
        m_lookup = lookup;
    }

    public String getLookup() {
        return m_lookup;
    }

}
