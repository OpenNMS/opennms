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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.model.discovery;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;

public class IPAddress implements Comparable<IPAddress> {

    long m_ipAddr;
    
    public IPAddress(IPAddress addr) {
        m_ipAddr = addr.m_ipAddr;
    }
    
    public IPAddress(String dottedNotation) {
        m_ipAddr = InetAddressUtils.toIpAddrLong(dottedNotation);
    }
    
    public IPAddress(InetAddress inetAddress) {
        m_ipAddr = InetAddressUtils.toIpAddrLong(inetAddress);
    }
    
    public IPAddress(long ipAddrAs32bitNumber) {
        m_ipAddr = ipAddrAs32bitNumber;
    }

    public IPAddress(byte[] ipAddrOctets) {
        m_ipAddr = InetAddressUtils.toIpAddrLong(ipAddrOctets);
    }
    
    public InetAddress toInetAddress() {
        return InetAddressUtils.getInetAddress(m_ipAddr);
    }
    
    public byte[] toOctets() {
        return InetAddressUtils.toIpAddrBytes(m_ipAddr);
    }
    
    public long toLong() {
        return m_ipAddr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IPAddress) {
            IPAddress other = (IPAddress) obj;
            return m_ipAddr == other.m_ipAddr;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int)m_ipAddr;
    }

    public int compareTo(IPAddress o) {
        IPAddress other = (IPAddress)o;
        long result = m_ipAddr - other.m_ipAddr;
        if (result < 0) return -1;
        if (result > 0) return 1;
        return 0;
    }

    @Override
    public String toString() {
        return InetAddressUtils.toIpAddrString(m_ipAddr);
    }
    
    public IPAddress incr() {
        return new IPAddress(m_ipAddr+1);
    }
    
    public IPAddress decr() {
        return new IPAddress(m_ipAddr-1);
    }
    
    public boolean isPredecessorOf(IPAddress other) {
        return other.decr().equals(this);
    }
    
    public boolean isSuccessorOf(IPAddress other) {
        return other.incr().equals(this);
    }
    
    public boolean isLessThan(IPAddress other) {
        return compareTo(other) < 0;
    }
    
    public boolean isLessThanOrEqualTo(IPAddress other) {
        return compareTo(other) <= 0;
    }
    
    public boolean isGreaterThan(IPAddress other) {
        return compareTo(other) > 0;
    }
    
    public boolean isGreaterThanOrEqualTo(IPAddress other) {
        return compareTo(other) >= 0;
    }

    public static IPAddress min(IPAddress a, IPAddress b) {
        return (a.isLessThan(b) ? a : b);
    }

    public static IPAddress max(IPAddress a, IPAddress b) {
        return (a.isGreaterThan(b) ? a : b);
    }

}
