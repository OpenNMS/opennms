/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd.plugins;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractTcpPlugin;
import org.opennms.netmgt.capsd.ConnectionConfig;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of an IIOP on a Domino server on remote interfaces. The class
 * implements the Plugin interface that allows it to be used along with other
 * plugins by the daemon.
 * </P>
 *
 * @author <a href="mailto:jason@opennms.org">Jason</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public final class DominoIIOPPlugin extends AbstractTcpPlugin {

    /**
     * Encapsulates the configuration characteristics unique to a DominoIIOP
     * connection
     * 
     * @author Matt Brozowski
     * 
     */
    public static class DominoConnectionConfig extends ConnectionConfig {

        int m_iorPort;

        /**
         * @param inetAddress
         * @param qualifiers
         * @param defaultPort
         * @param defaultTimeout
         * @param defaultRetries
         */
        public DominoConnectionConfig(InetAddress inetAddress, int port) {
            super(inetAddress, port);

        }

        public int getIorPort() {
            return m_iorPort;
        }

        public void setIorPort(int iorPort) {
            m_iorPort = iorPort;
        }

    }

    /**
     * Default port of where to find the IOR via HTTP
     */
    private static final int DEFAULT_IORPORT = 80;

    /**
     * Default port.
     */
    private static final int DEFAULT_PORT = 63148;

    /**
     * Default number of retries for TCP requests
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for TCP requests
     */
    private static final int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * The protocol supported by the plugin
     */
    private static final String PROTOCOL_NAME = "DominoIIOP";

    /**
     * <p>Constructor for DominoIIOPPlugin.</p>
     */
    public DominoIIOPPlugin() {
        super(PROTOCOL_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.capsd.AbstractTcpPlugin#createProtocol(java.net.Socket,
     *      org.opennms.netmgt.capsd.ConnectonConfig)
     */
    /** {@inheritDoc} */
    @Override
    protected boolean checkProtocol(Socket socket, ConnectionConfig config) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.capsd.AbstractTcpPlugin#createConfig(java.net.InetAddress,
     *      java.util.Map)
     */
    /** {@inheritDoc} */
    @Override
    protected ConnectionConfig createConnectionConfig(InetAddress address, int port) {
        return new DominoConnectionConfig(address, port);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.capsd.AbstractTcpPlugin#populateConnectionConfig(org.opennms.netmgt.capsd.ConnectionConfig,
     *      java.util.Map)
     */
    /** {@inheritDoc} */
    @Override
    protected void populateConnectionConfig(ConnectionConfig connConfig, Map<String, Object> qualifiers) {
        super.populateConnectionConfig(connConfig, qualifiers);

        DominoConnectionConfig config = (DominoConnectionConfig) connConfig;
        config.setIorPort(getKeyedInteger(qualifiers, "ior-port", DEFAULT_IORPORT));

    }

    /** {@inheritDoc} */
    @Override
    protected boolean preconnectCheck(ConnectionConfig tcpConfig) {
        // get a log to send errors
        //
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        DominoConnectionConfig config = (DominoConnectionConfig) tcpConfig;
        // Lets first try to the the IOR via HTTP, if we can't get that then any
        // other process that can
        // do it the right way won't be able to connect anyway
        //
        try {
            retrieveIORText(InetAddressUtils.str(config.getInetAddress()), config.getIorPort());
            return true;
        } catch (FileNotFoundException e) {
            return true;
        } catch (Throwable e) {
            if (log.isDebugEnabled())
                log.debug("DominoIIOPMonitor: failed to get the corba IOR from " + InetAddressUtils.str(config.getInetAddress()));
            return false;
        }
    }

    /**
     * Method used to retrieve the IOR string from the Domino server.
     * 
     * @param host
     *            the host name which has the IOR
     * @param port
     *            the port to find the IOR via HTTP
     */
    private String retrieveIORText(String host, int port) throws IOException {
        String IOR = "";
        java.net.URL u = new java.net.URL("http://" + host + ":" + port + "/diiop_ior.txt");
        java.io.InputStream is = u.openStream();
        java.io.BufferedReader dis = new java.io.BufferedReader(new java.io.InputStreamReader(is));
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

        if (!IOR.startsWith("IOR:"))
            throw new IOException("Invalid IOR: " + IOR);

        return IOR;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.capsd.AbstractTcpPlugin#saveConfig(org.opennms.netmgt.capsd.ConnectionConfig)
     */
    /**
     * <p>saveConfig</p>
     *
     * @param config a {@link org.opennms.netmgt.capsd.ConnectionConfig} object.
     */
    protected void saveConfig(ConnectionConfig config) {
        // override this as this plugin does not save any params
    }
}
