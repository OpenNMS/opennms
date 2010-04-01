//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 02: Use CastorUtils.unmarshal and add IP address methods. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.eventd.EventdConfiguration;
import org.opennms.netmgt.dao.castor.CastorUtils;

/**
 * @author david
 *
 */
public class EventdConfigManager {

    /**
     * The config class loaded from the config file
     */
    protected EventdConfiguration m_config;

    /**
     * Constructor
     * @throws ValidationException 
     * @throws MarshalException 
     * @throws IOException 
     */
    @Deprecated
    protected EventdConfigManager(Reader reader) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(EventdConfiguration.class, reader);
        reader.close();

    }
    
    protected EventdConfigManager(InputStream stream) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(EventdConfiguration.class, stream);

    }
    
    public EventdConfigManager(String configFile) throws FileNotFoundException, MarshalException, ValidationException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(configFile);
            m_config = CastorUtils.unmarshal(EventdConfiguration.class, stream);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    /**
     * Return the IP address on which eventd listens for TCP connections.
     * 
     * @return the IP address on which eventd listens for TCP connections
     */
    public synchronized String getTCPIpAddress() {
        return m_config.getTCPAddress();
    }
    
    /**
     * Return the port on which eventd listens for TCP connections.
     * 
     * @return the port on which eventd listens for TCP connections
     */
    public synchronized int getTCPPort() {
        return m_config.getTCPPort();
    }

    /**
     * Return the IP address on which eventd listens for UDP packets.
     * 
     * @return the IP address on which eventd listens for UDP packets
     */
    public synchronized String getUDPIpAddress() {
        return m_config.getUDPAddress();
    }

    /**
     * Return the port on which eventd listens for UDP data.
     * 
     * @return the port on which eventd listens for UDP data
     */
    public synchronized int getUDPPort() {
        return m_config.getUDPPort();
    }

    /**
     * Return the number of event receivers to be started.
     * 
     * @return the number of event receivers to be started
     */
    public synchronized int getReceivers() {
        return m_config.getReceivers();
    }

    /**
     * Return string indicating if timeout is to be set on the socket.
     * 
     * @return string indicating if timeout is to be set on the socket
     */
    public synchronized String getSocketSoTimeoutRequired() {
        return m_config.getSocketSoTimeoutRequired();
    }

    /**
     * Return timeout to be set on the socket.
     * 
     * @return timeout is to be set on the socket
     */
    public synchronized int getSocketSoTimeoutPeriod() {
        return m_config.getSocketSoTimeoutPeriod();
    }

    /**
     * Return flag indicating if timeout to be set on the socket is specified.
     * 
     * @return flag indicating if timeout to be set on the socket is specified <
     */
    public synchronized boolean hasSocketSoTimeoutPeriod() {
        return m_config.hasSocketSoTimeoutPeriod();
    }

    /**
     * Return the SQL statemet to get the next event ID.
     * 
     * @return the SQL statemet to get the next event ID
     */
    public synchronized String getGetNextEventID() {
        return m_config.getGetNextEventID();
    }
}
