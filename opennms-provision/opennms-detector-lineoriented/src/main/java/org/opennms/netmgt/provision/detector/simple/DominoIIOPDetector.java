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
package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.client.DominoIIOPClient;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>DominoIIOPDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */

@Component
@Scope("prototype")
public class DominoIIOPDetector extends LineOrientedDetector {
    
    /**
     * Default port of where to find the IOR via HTTP
     */
    private static final int DEFAULT_IORPORT = 80;

    /**
     * Default port.
     */
    private static final int DEFAULT_PORT = 63148;

    private static final String DEFAULT_SERVICE_NAME = "DominoIIOP";
    
    private int m_iorPort = DEFAULT_IORPORT;
    
    /**
     * Default constructor
     */
    public DominoIIOPDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public DominoIIOPDetector(String serviceName, int port) {
        super(serviceName, port);
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        // Empty on init method, everything for this detector is done in the Client
        // It does a preconnect to check for the diiop_ior.txt if that file is found it will
        // attempt to connect to the port that you set it to connect or the default port 63148
        
    }
    
    /** {@inheritDoc} */
    @Override
    protected DominoIIOPClient getClient() {
        DominoIIOPClient client = new DominoIIOPClient();
        client.setIorPort(getIorPort());
        return client;
    }

    /**
     * <p>setIorPort</p>
     *
     * @param iorPort a int.
     */
    public void setIorPort(int iorPort) {
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
