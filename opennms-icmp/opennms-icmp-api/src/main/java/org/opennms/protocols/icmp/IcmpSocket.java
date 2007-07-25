/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Jul 25: Move 'main' and related code to a Ping class. Make the code
 *              separable from OpenNMS.
 * 2007 Jun 23: Fix warnings on static members and eliminate warning on
 *              m_rawFd that is only used in native code. - dj@opennms.org
 * 2007 May 21: Improve logging of shared library loading. - dj@opennms.org
 * 2003 Mar 05: Changes to support response times and more platforms.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.protocols.icmp;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * This class provides a bridge between the host operating system so that ICMP
 * messages may be sent and received.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * 
 */
public final class IcmpSocket {
    private static final String LIBRARY_NAME = "jicmp";
    private static final String PROPERTY_NAME = "opennms.library.jicmp";
    private static final String LOGGER_PROPERTY_NAME = "opennms.logger.jicmp";
    
    public interface Logger {
        public void debug(String msg);
        public void info(String msg);
    }

    /**
     * This instance is used by the native code to save and store file
     * descriptor information about the icmp socket. This needs to be
     * constructed prior to calling the init method, preferable in the
     * constructor.
     * 
     * It looks unused, but it is used solely by native code.
     */
    @SuppressWarnings("unused")
    private FileDescriptor m_rawFd;

    /**
     * This method is used to open the initial operating system icmp socket. The
     * descriptor for the socket is stored in the member m_rawFd.
     * 
     * @throws java.io.IOException
     *             This is thrown if an error occurs opening the ICMP socket.
     */
    private native void initSocket() throws IOException;

    /**
     * Constructs a new socket that is able to send and receive ICMP messages.
     * The newly constructed socket will receive all ICMP messages directed at
     * the local machine. The application must be prepared to handle any and
     * discard any non-interesting ICMP messages.
     * 
     * @exception java.io.IOException
     *                This exception is thrown if the socket fails to be opened
     *                correctly.
     */
    public IcmpSocket() throws IOException {
        String property = System.getProperty(PROPERTY_NAME);
        if (property != null) {
            log().debug("System property '" + PROPERTY_NAME + "' set to '" + System.getProperty(PROPERTY_NAME) + ".  Attempting to load " + LIBRARY_NAME + " library from this location.");
            System.load(property);
        } else {
            log().debug("System property '" + PROPERTY_NAME + "' not set.  Attempting to load library using System.loadLibrary(\"" + LIBRARY_NAME + "\").");
            System.loadLibrary(LIBRARY_NAME);
        }
        log().info("Successfully loaded " + LIBRARY_NAME + " library.");

        m_rawFd = new FileDescriptor();
        initSocket();
    }
    
    private Logger log() {
        try {
            if (System.getProperty(LOGGER_PROPERTY_NAME) != null) {
                return (Logger)Class.forName(System.getProperty(LOGGER_PROPERTY_NAME)).newInstance();
            }
        } catch (Exception e) {
            System.err.println("[WARN] Unable to create jicmp logger from property "+LOGGER_PROPERTY_NAME+" with value "+System.getProperty(LOGGER_PROPERTY_NAME)+". "+e);
        }
        return new Logger() {
            public void debug(String msg) {
                System.err.println("[DEBUG] "+msg);
            }
            public void info(String msg) {
                System.err.println("[INFO] "+msg);
            }
        };
    }

    /**
     * This method is used to receive the next ICMP datagram from the operating
     * system. The returned datagram packet's address is set to the sending
     * host's address. The port number is always set to Zero, and the buffer is
     * set to the contents of the raw ICMP message.
     * 
     * @exception java.io.IOException
     *                Thrown if an error occurs reading the next ICMP message.
     * 
     */
    public final native DatagramPacket receive() throws IOException;

    /**
     * This method is used to send the passed datagram using the ICMP transport.
     * The destination of the datagram packet is used as the send to destination
     * for the underlying ICMP socket. The port number of the datagram packet is
     * ignored completely.
     * 
     * @exception java.io.IOException
     *                Thrown if an error occurs sending the datagram to the
     *                remote host.
     * @exception java.net.NoRouteToHostException
     *                Thrown if the destination address is a broadcast address.
     */
    public final native void send(DatagramPacket packet) throws IOException;

    /**
     * This method is used to close and release the resources associated with the
     * instance. The file descriptor is closed at the operating system level and
     * any subsequent calls to this instance should result in exceptions being
     * generated.
     */
    public final native void close();
}
