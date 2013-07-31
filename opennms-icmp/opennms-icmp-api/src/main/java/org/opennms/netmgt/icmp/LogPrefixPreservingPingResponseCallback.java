/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp;

import java.net.InetAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class LogPrefixPreservingPingResponseCallback implements PingResponseCallback {
    private static final Logger LOG = LoggerFactory.getLogger(LogPrefixPreservingPingResponseCallback.class);
	
    private final PingResponseCallback m_cb;
    private final Map m_mdc = getCopyOfContextMap();
    
    public LogPrefixPreservingPingResponseCallback(PingResponseCallback cb) {
        m_cb = cb;
    }
    
    private static Map getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }
    
    private static void setContextMap(Map map) {
        if (map == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(map);
        }
    }

    @Override
    public void handleError(InetAddress address, EchoPacket request, Throwable t) {
    	
    	Map mdc = getCopyOfContextMap();
        try {
            setContextMap(m_mdc);
            m_cb.handleError(address, request, t);
        } finally {
            setContextMap(mdc);
        }
    }

    @Override
    public void handleResponse(InetAddress address, EchoPacket response) {
    	Map mdc = getCopyOfContextMap();
        try {
            setContextMap(m_mdc);
            m_cb.handleResponse(address, response);
        } finally {
            setContextMap(mdc);
        }
    }

    @Override
    public void handleTimeout(InetAddress address, EchoPacket request) {
    	Map mdc = getCopyOfContextMap();
        try {
            setContextMap(m_mdc);
            m_cb.handleTimeout(address, request);
        } finally {
            setContextMap(mdc);
        }
    }
}