/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.client.DominoIIOPClient;

/**
 * <p>DominoIIOPDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */

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
    public DominoIIOPDetector(final String serviceName, final int port) {
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
        final DominoIIOPClient client = new DominoIIOPClient();
        client.setIorPort(getIorPort());
        return client;
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
