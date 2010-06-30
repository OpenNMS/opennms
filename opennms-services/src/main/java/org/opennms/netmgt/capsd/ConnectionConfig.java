//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/*
 * Created on Apr 27, 2004
 *
 * TODO Need to javadoc this class.
 * 
 */
package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

import org.opennms.netmgt.utils.ParameterMap;

/**
 * <p>ConnectionConfig class.</p>
 *
 * @author brozow
 *
 * TODO Need to javadoc this class
 * @version $Id: $
 */
public class ConnectionConfig {
    InetAddress m_inetAddress;

    Map<String, Object> m_qualifiers;

    int m_port;

    int m_timeout;

    int m_retry;

    /**
     * <p>Constructor for ConnectionConfig.</p>
     *
     * @param qualifiers a {@link java.util.Map} object.
     * @param inetAddress a {@link java.net.InetAddress} object.
     * @param port a int.
     * @param timeout a int.
     * @param retry a int.
     */
    public ConnectionConfig(InetAddress inetAddress, Map<String, Object> qualifiers, int port, int timeout, int retry) {
        m_inetAddress = inetAddress;
        m_qualifiers = qualifiers;
        m_port = port;
        m_timeout = timeout;
        m_retry = retry;
    }

    /**
     * <p>Constructor for ConnectionConfig.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param port a int.
     */
    public ConnectionConfig(InetAddress address, int port) {
        m_inetAddress = address;
        m_port = port;

    }

    /**
     * <p>Constructor for ConnectionConfig.</p>
     *
     * @param inetAddress a {@link java.net.InetAddress} object.
     * @param port a int.
     * @param timeout a int.
     * @param retry a int.
     */
    public ConnectionConfig(InetAddress inetAddress, int port, int timeout, int retry) {
        this(inetAddress, null, port, timeout, retry);
    }

    /**
     * <p>getSocketAddress</p>
     *
     * @return a {@link java.net.InetSocketAddress} object.
     */
    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(getInetAddress(), getPort());
    }
    
    /**
     * <p>setQualifiers</p>
     *
     * @param qualifiers a {@link java.util.Map} object.
     */
    public void setQualifiers(Map<String, Object> qualifiers) {
        m_qualifiers = qualifiers;
    }

    /**
     * <p>getInetAddress</p>
     *
     * @return Returns the address.
     */
    public InetAddress getInetAddress() {
        return m_inetAddress;
    }

    /**
     * <p>setInetAddress</p>
     *
     * @param inetAddress
     *            The inetAddresss to set.
     */
    public void setInetAddress(InetAddress inetAddress) {
        m_inetAddress = inetAddress;
    }

    /**
     * <p>getKeyedInteger</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param defaultVal a int.
     * @return a int.
     */
    public int getKeyedInteger(String key, int defaultVal) {
        if (m_qualifiers == null)
            return defaultVal;
        else
            return ParameterMap.getKeyedInteger(m_qualifiers, key, defaultVal);
    }

    /**
     * <p>getKeyedBoolean</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param defaultVal a boolean.
     * @return a boolean.
     */
    public boolean getKeyedBoolean(String key, boolean defaultVal) {
        if (m_qualifiers == null)
            return defaultVal;
        else
            return ParameterMap.getKeyedBoolean(m_qualifiers, key, defaultVal);
    }

    /**
     * <p>getKeyedString</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param defaultVal a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getKeyedString(String key, String defaultVal) {
        if (m_qualifiers == null)
            return defaultVal;
        else
            return ParameterMap.getKeyedString(m_qualifiers, key, defaultVal);
    }

    /**
     * <p>saveKeyedInteger</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a int.
     */
    public void saveKeyedInteger(String key, int value) {
        if (m_qualifiers != null && !m_qualifiers.containsKey(key))
            m_qualifiers.put(key, new Integer(value));
    }

    /**
     * <p>getPort</p>
     *
     * @return Returns the port.
     */
    public int getPort() {
        return m_port;
    }

    /**
     * <p>getRetry</p>
     *
     * @return Returns the retries.
     */
    public int getRetry() {
        return m_retry;
    }

    /**
     * <p>setRetry</p>
     *
     * @param retry
     *            The retries to set.
     */
    public void setRetry(int retry) {
        m_retry = retry;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return Returns the timeout.
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout
     *            The timeout to set.
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }
}
