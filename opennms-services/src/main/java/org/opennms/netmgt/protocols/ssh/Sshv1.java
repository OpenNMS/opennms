/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Aug 11: Converted to Trilead SSH client
 * 2007 Oct 03: Initial version
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.protocols.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.protocols.InsufficientParametersException;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class Sshv1 extends Ssh {
    private Socket m_socket = null;
    private BufferedReader m_reader = null;
    private OutputStream m_writer = null;

    protected static String m_banner = "SSH-1.5-OpenNMS_1.5";


    public Sshv1() { }
    
    public Sshv1(InetAddress address) {
        setAddress(address);
    }
    
    public Sshv1(InetAddress address, int port) {
        setAddress(address);
        setPort(port);
    }
    
    public Sshv1(InetAddress address, int port, int timeout) {
        setAddress(address);
        setPort(port);
        setTimeout(timeout);
    }

    /**
     * Attempt to connect, based on the parameters which have been set in
     * the object.
     * 
     * @return true if it is able to connect
     * @throws InsufficientParametersException
     */
    protected boolean tryConnect() throws InsufficientParametersException {
        if (getAddress() == null) {
            throw new InsufficientParametersException("you must specify an address");
        }

        try {
            m_socket = new Socket();
            m_socket.setTcpNoDelay(true);
            m_socket.connect(new InetSocketAddress(getAddress(), getPort()), getTimeout());
            
            m_reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
            m_writer = m_socket.getOutputStream();

            // read the banner
            m_serverBanner = m_reader.readLine();

            // write our own
            m_writer.write((m_banner + "\r\n").getBytes());

            // then, disconnect
            disconnect();

            String[] bannerBits = m_serverBanner.split("-");
            Double protocolVersion = Double.parseDouble(bannerBits[1]);
            if (protocolVersion >= 2.0) {
                throw new SshException("SSH server does not support version 1.x!");
            }

            return true;
        } catch (NumberFormatException e) {
            log().debug("unable to parse server version", e);
            setError(e);
            disconnect();
        } catch (Exception e) {
            log().debug("connection failed", e);
            setError(e);
            disconnect();
        }
        return false;
    }

    protected void disconnect() {
        if (m_writer != null) {
            try {
                m_writer.close();
            } catch (IOException e) {
                log().warn("error disconnecting output stream", e);
            }
        }
        if (m_reader != null) {
            try {
                m_reader.close();
            } catch (IOException e) {
                log().warn("error disconnecting input stream", e);
            }
        }
        if (m_socket != null) {
            try {
                m_socket.close();
            } catch (IOException e) {
                log().warn("error disconnecting socket", e);
            }
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
