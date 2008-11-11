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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.protocols.dns.DNSAddressRequest;

/**
 * @author Donald Desloge
 *
 */
public class TemplateDetector extends AbstractDetector {
    
    public static interface ResponseHandler{
        public boolean matches(Object...args); //DatagramSocket socket, InetAddress address;
    }
    
    public static interface RequestHandler{
        public boolean doRequest(Object...args) throws IOException;
    }
    
    public static interface DatagramExchange{
        public boolean processResponse(Object... args); //DatagramSocket socket, InetAddress address
        public boolean sendRequest(Object... args); //DatagramSocket dSocket, InetAddress address
    }
    
    public static class DnsExchange implements DatagramExchange{
        ResponseHandler m_responseHandler;
        RequestHandler m_requestHandler;
        
        public DnsExchange(ResponseHandler responseHandler, RequestHandler requestHandler) {
            m_responseHandler = responseHandler;
            m_requestHandler = requestHandler;

        }
        
        public boolean processResponse(Object... args) {
            if(m_responseHandler != null) {
                return m_responseHandler.matches(args);
            }
            
            return true;          
            
        }

        
        public boolean sendRequest(Object...args) {
             if(m_requestHandler != null) {
                try {
                    m_requestHandler.doRequest(args);
                }catch(IOException e) {
                    return false;
                }
             }
            return true;
        }
        
    }
    
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
    private DatagramSocket m_socket = null;
    private DNSAddressRequest m_request = null;
    private List<DatagramExchange> m_conversation = new ArrayList<DatagramExchange>();
    
    protected TemplateDetector() {
        super(DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRY);
        setServiceName("DNS");
        setLookup(DEFAULT_LOOKUP);
    }
    
    @Override
    public void init() {
        m_request = new DNSAddressRequest(getLookup());
        onInit();
    }
    
    public void onInit() {        
        //expectBanner(null);
        //addMakeRequest( dnsRequest(getLookup()), dsnResponseMatches(m_request));
        addResponseHandler(null, dnsRequest(getLookup()));
        addResponseHandler(dnsResponseMatches(m_request), null);
    }

    /**
     * Adds a SimpleExchange object to the conversation. Its a reads a single line then compares the line
     * with the ResponseHandler that was passed in.
     */
    protected void addResponseHandler(ResponseHandler responseHandler, RequestHandler requestHandler) {
        addExchange(new DnsExchange(responseHandler, requestHandler));
    }
    
    /**
     * @param dnsExchange
     */
    private void addExchange(DnsExchange dnsExchange) {
        m_conversation.add(dnsExchange);
        
    }

    protected ResponseHandler nullHandler() {
        return new ResponseHandler() {

            public boolean matches(Object... args) {
                return false;
            }
            
        };
    }
    
    protected ResponseHandler dnsResponseMatches(final DNSAddressRequest request) {
        return new ResponseHandler() {

            public boolean matches(Object... args) {
                
                try {
                    DatagramSocket socket = (DatagramSocket) args[0];
                    InetAddress address = (InetAddress) args[1];
                    
                    byte[] data = new byte[512];
                    DatagramPacket inPacket = new DatagramPacket(data, data.length);
                    socket.receive(inPacket);
                    
                    if (inPacket.getAddress().equals(address)) {
                        System.out.println("Packet address matches:");
                        request.verifyResponse(inPacket.getData(), inPacket.getLength());
                        System.out.println("Success it worked");
                        return true;
                    }
                    
                    return false;
                    
                }catch(IOException e) {
                    return false;
                }catch(Exception e) {
                    return false;
                }

            }
            
        };
    }
    
    protected RequestHandler dnsRequest(final String lookup) {
        return new RequestHandler() {

            public boolean doRequest(Object...args) throws IOException {
                DatagramSocket socket = (DatagramSocket) args[0];
                InetAddress address = (InetAddress) args[1];
                socket.send(buildRequest(lookup, address));
                return true;
            }
            
        };
    }
    

    @Override
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor) {
        
        boolean isAServer = false;
        
        try {
            // Allocate a socket
            //
            m_socket = getSocketConnection();

            for (int count = 0; count < getRetries() && !isAServer; count++) {
                try {
                    
                    if(attemptConversation(address)) { 
                        System.out.println("\n***** Dude This worked ******\n");
                        return true; }

                } catch (InterruptedIOException ex) {
                    // discard this exception, do next loop
                    //
                }
            }
        } catch (IOException ex) {
            detectMonitor.info(this, ex, "isServer: An I/O exception during DNS resolution test.", new Object());
            //log.warn("isServer: An I/O exception during DNS resolution test.", ex);
        } finally {
            if (m_socket != null)
                m_socket.close();
        }

        return isAServer;
    }
    
    /**
     * @param address 
     * @throws IOException 
     * 
     */
    private boolean attemptConversation(InetAddress address) throws IOException {
               
        for(Iterator<DatagramExchange> it = m_conversation.iterator(); it.hasNext();) {
            DatagramExchange ex = it.next();
            
            if(!ex.processResponse(m_socket, address)) {
               return false; 
            }
            System.out.println("processed response successfully");
            
            if(!ex.sendRequest(m_socket, address)) {
                return false;
            }
            
            System.out.println("send request if there was a request");
        }
        
        return true;
        
    }

    protected DatagramSocket getSocketConnection() throws SocketException {
        DatagramSocket retVal = new DatagramSocket();
        retVal.setSoTimeout(getTimeout());
        return retVal;
    }
    
    protected DatagramPacket buildRequest(String lookup, InetAddress address) throws IOException {
        byte[] rdata = m_request.buildRequest();
        DatagramPacket outpkt = new DatagramPacket(rdata, rdata.length, address, getPort());
        return outpkt;
    }

    public void setLookup(String lookup) {
        m_lookup = lookup;
    }

    public String getLookup() {
        return m_lookup;
    }

}
