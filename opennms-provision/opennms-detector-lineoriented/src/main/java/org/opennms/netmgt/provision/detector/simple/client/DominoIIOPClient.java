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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

/**
 * <p>DominoIIOPClient class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class DominoIIOPClient extends LineOrientedClient {
    
    private int m_iorPort = 1000;
    
    /** {@inheritDoc} */
    public void connect(final InetAddress host, final int port, final int timeout) throws IOException, Exception {        
        if(!preconnect(host, getIorPort(), timeout)) {
            throw new Exception("Failed to preconnect");
        }
    }

    /**
     * @param timeout 
     * @param port 
     * @param host 
     * @param timeout 
     * @return
     * @throws IOException 
     */
    private boolean preconnect(final InetAddress host, final int port, final int timeout) throws IOException {
        return retrieveIORText(host.getHostAddress(), port, timeout);
    }

    /**
     * @param hostAddress
     * @param port
     * @return
     */
    private boolean retrieveIORText(final String hostAddress, final int port, final int timeout) throws IOException {
        String IOR = "";
        final URL u = new URL("http://" + hostAddress + ":" + port + "/diiop_ior.txt");
        final URLConnection conn = u.openConnection();
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new InputStreamReader(conn.getInputStream());
            br = new BufferedReader(isr);
            boolean done = false;
            while (!done) {
                final String line = br.readLine();
                if (line == null) {
                    // end of stream
                    done = true;
                } else {
                    IOR += line;
                    if (IOR.startsWith("IOR:")) {
                        // the IOR does not span a line, so we're done
                        done = true;
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(isr);
        }
        if (!IOR.startsWith("IOR:")) return false;
        
        return true;
    }

    /**
     * <p>setIorPort</p>
     *
     * @param iorPort a int.
     */
    public void setIorPort(final int iorPort) {
        m_iorPort = iorPort;
    }

    /**
     * <p>getIorPort</p>
     *
     * @return a int.
     */
    public int getIorPort() {
        return m_iorPort;
    }
    

}
