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

import org.opennms.core.utils.ThreadCategory;

public class LogPrefixPreservingPingResponseCallback implements PingResponseCallback {
    private final PingResponseCallback m_cb;
    private final String m_prefix = ThreadCategory.getPrefix();
    
    public LogPrefixPreservingPingResponseCallback(PingResponseCallback cb) {
        m_cb = cb;
    }

    public void handleError(InetAddress address, EchoPacket request, Throwable t) {
        String oldPrefix = ThreadCategory.getPrefix();
        try {
            ThreadCategory.setPrefix(m_prefix);
            m_cb.handleError(address, request, t);
        } finally {
            ThreadCategory.setPrefix(oldPrefix);
        }
    }

    public void handleResponse(InetAddress address, EchoPacket response) {
        String oldPrefix = ThreadCategory.getPrefix();
        try {
            ThreadCategory.setPrefix(m_prefix);
            m_cb.handleResponse(address, response);
        } finally {
            ThreadCategory.setPrefix(oldPrefix);
        }
    }

    public void handleTimeout(InetAddress address, EchoPacket request) {
        String oldPrefix = ThreadCategory.getPrefix();
        try {
            ThreadCategory.setPrefix(m_prefix);
            m_cb.handleTimeout(address, request);
        } finally {
            ThreadCategory.setPrefix(oldPrefix);
        }
    }
}