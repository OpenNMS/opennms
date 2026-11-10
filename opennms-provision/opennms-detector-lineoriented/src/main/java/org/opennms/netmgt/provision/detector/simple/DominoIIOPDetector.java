/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
