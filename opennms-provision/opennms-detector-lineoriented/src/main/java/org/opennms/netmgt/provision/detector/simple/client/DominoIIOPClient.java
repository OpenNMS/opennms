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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author thedesloge
 *
 */
public class DominoIIOPClient extends LineOrientedClient {
    
    private int m_iorPort = 1000;
    
    public void connect(InetAddress host, int port, int timeout) throws IOException, Exception {        
        if(!preconnect(host, getIorPort(), timeout)) {
            throw new Exception("Failed to preconnect");
        }
        
//        Socket socket = new Socket();
//        socket.connect(new InetSocketAddress(host, port), timeout);
//        socket.setSoTimeout(timeout);
//        setInput(new BufferedReader(new InputStreamReader(socket.getInputStream())));
//        setOutput(socket.getOutputStream());
//        m_socket = socket;
        
    }

    /**
     * @param timeout 
     * @param port 
     * @param host 
     * @param timeout 
     * @return
     * @throws IOException 
     */
    private boolean preconnect(InetAddress host, int port, int timeout) throws IOException {
        return retrieveIORText(host.getHostAddress(), port, timeout);
    }

    /**
     * @param hostAddress
     * @param port
     * @return
     */
    private boolean retrieveIORText(String hostAddress, int port, int timeout) throws IOException {
        String IOR = "";
        URL u = new URL("http://" + hostAddress + ":" + port + "/diiop_ior.txt");
        URLConnection conn = u.openConnection();
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        InputStream is = conn.getInputStream();
        BufferedReader dis = new BufferedReader(new java.io.InputStreamReader(is));
        boolean done = false;
        while (!done) {
            String line = dis.readLine();
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
        dis.close();

        if (!IOR.startsWith("IOR:")) { return false; }

        
        return true;
    }

    public void setIorPort(int iorPort) {
        m_iorPort = iorPort;
    }

    public int getIorPort() {
        return m_iorPort;
    }
    

}
